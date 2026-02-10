import android.content.Context
import android.media.AudioAttributes
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log
import androidx.annotation.RawRes
import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import java.io.File

/**
 * 音と振動を制御するクラス
 * * 特徴:
 * - マナーモード時でもアラーム音量(STREAM_ALARM)を使用して強制的に音を鳴らす
 * - 再生前後でアラーム音量をバックアップ・復元し、他のアプリの音楽(STREAM_MUSIC)を邪魔しない
 * - ライフサイクルを自己監視し、Activity破棄時にリソースを自動解放する
 * - 二重再生防止、オーディオフォーカス(Ducking)、バイブレーション機能を搭載
 */
class ForcedSoundPlayer(
    private val context: Context,
    lifecycleOwner: LifecycleOwner
) {
    private val TAG = "ForcedSoundPlayer"
    private var mediaPlayer: MediaPlayer? = null
    private val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as AudioManager
    private val cryptoManager = CryptoManager()
    
    private val vibrator: Vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
        val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
        manager.defaultVibrator
    } else {
        @Suppress("DEPRECATION")
        context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
    }

    private val targetStream = AudioManager.STREAM_ALARM
    private var originalAlarmVolume: Int = -1
    private var focusRequest: AudioFocusRequest? = null
    private var isProcessing: Boolean = false

    init {
        lifecycleOwner.lifecycle.addObserver(object : DefaultLifecycleObserver {
            override fun onStop(owner: LifecycleOwner) {
                cleanup()
            }
            override fun onDestroy(owner: LifecycleOwner) {
                cleanup()
                owner.lifecycle.removeObserver(this)
            }
        })
    }

    fun play(@RawRes resId: Int, volumePercent: Float = 0.7f) {
        val uri = Uri.parse("android.resource://${context.packageName}/$resId")
        prepareAndPlay(uri, volumePercent)
    }

    fun play(file: File, volumePercent: Float = 0.7f) {
        if (!file.exists()) return
        prepareAndPlay(Uri.fromFile(file), volumePercent)
    }

    fun playEncryptedFile(file: File, volumePercent: Float = 0.7f) {
        if (!file.exists()) return
    
        runCatching {
            // 1. ファイルから暗号化データを読み込み
            val encryptedData = file.readBytes()
            
            // 2. メモリ上で復号
            val decryptedData = cryptoManager.decrypt(encryptedData)
            
            // 3. メモリ内のバイト配列をDataSourceとしてセット
            val dataSource = MemoryMediaDataSource(decryptedData)
            
            prepareAndPlayWithDataSource(dataSource, volumePercent)
        }.onFailure {
            Log.e(TAG, "Decryption or Playback failed", it)
        }
    }

    private fun prepareAndPlay(uri: Uri, volumePercent: Float) {
        // 二重再生防止ガード
        if (isProcessing || mediaPlayer?.isPlaying == true) return
        isProcessing = true

        runCatching {
            // 1. 現在の音量を退避
            if (originalAlarmVolume == -1) {
                originalAlarmVolume = audioManager.getStreamVolume(targetStream)
            }
            
            // 2. 規定の音量に引き上げ
            val maxVol = audioManager.getStreamMaxVolume(targetStream)
            val targetVol = (maxVol * volumePercent).toInt().coerceAtLeast(1)
            audioManager.setStreamVolume(targetStream, targetVol, 0)

            // 3. 他アプリの音を一時的に下げる(Ducking)要求
            requestAudioFocus()

            // 4. バイブレーション（決済成功の2回振動）
            vibrateSuccess()

            // 5. MediaPlayer構築
            mediaPlayer = MediaPlayer().apply {
                setDataSource(context, uri)
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM) // スピーカー出力を優先
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setOnErrorListener { _, what, extra ->
                    Log.e(TAG, "Error during playback: $what, $extra")
                    cleanup()
                    true
                }
                setOnPreparedListener { start() }
                setOnCompletionListener { cleanup() }
                prepareAsync()
            }
        }.onFailure { e ->
            Log.e(TAG, "Failed to play sound", e)
            cleanup()
        }
    }

    private fun prepareAndPlayWithDataSource(dataSource: MemoryMediaDataSource, volumePercent: Float) {
        if (isProcessing) return
        isProcessing = true
        
        runCatching {
            // 音量・フォーカス・バイブ設定 (以前のロジックと同じ)
            setupAudioAndVibration(volumePercent)

            mediaPlayer = MediaPlayer().apply {
                setDataSource(dataSource) // バイト配列を直接指定
                setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                        .build()
                )
                setOnPreparedListener { start() }
                setOnCompletionListener { cleanup() }
                setOnErrorListener { _, _, _ -> cleanup(); true }
                prepareAsync()
            }
        }.onFailure { cleanup() }
    }

    private fun requestAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
                .setAudioAttributes(
                    AudioAttributes.Builder()
                        .setUsage(AudioAttributes.USAGE_ALARM)
                        .build()
                )
                .build()
            focusRequest?.let { audioManager.requestAudioFocus(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.requestAudioFocus(null, targetStream, AudioManager.AUDIOFOCUS_GAIN_TRANSIENT_MAY_DUCK)
        }
    }

    private fun vibrateSuccess() {
        runCatching {
            if (!vibrator.hasVibrator()) return@runCatching
            val pattern = longArrayOf(0, 100, 50, 100) // 待機, 振, 待機, 振
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createWaveform(pattern, -1))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(pattern, -1)
            }
        }
    }

    private fun cleanup() {
        restoreVolume()
        abandonAudioFocus()
        stopAndRelease()
        vibrator.cancel()
        isProcessing = false
    }

    private fun restoreVolume() {
        if (originalAlarmVolume != -1) {
            audioManager.setStreamVolume(targetStream, originalAlarmVolume, 0)
            originalAlarmVolume = -1
        }
    }

    private fun abandonAudioFocus() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            focusRequest?.let { audioManager.abandonAudioFocusRequest(it) }
        } else {
            @Suppress("DEPRECATION")
            audioManager.abandonAudioFocus(null)
        }
    }

    private fun stopAndRelease() {
        mediaPlayer?.runCatching {
            if (isPlaying) stop()
            release()
        }
        mediaPlayer = null
    }
}