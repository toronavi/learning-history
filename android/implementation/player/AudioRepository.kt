import android.content.Context
import android.util.Log
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.IOException

class AudioRepository(private val context: Context) {
    private val client = OkHttpClient()
    private val cryptoManager = CryptoManager()
    private val TAG = "AudioRepository"

    // 保存先ファイル名（内部ストレージ）
    private val fileName = "encrypted_payment_sound.wav"
    val audioFile: File get() = File(context.filesDir, fileName)

    /**
     * 音源をプリフェッチ（事前取得）する
     */
    suspend fun prefetchAudio(url: String): Boolean {
        // すでにファイルが存在する場合はダウンロードをスキップ
        if (audioFile.exists() && audioFile.length() > 0) {
            Log.d(TAG, "Audio already exists. Skipping download.")
            return true
        }

        return downloadAndEncrypt(url)
    }

    /**
     * 音源の強制更新（URLが変わった場合など）
     */
    suspend fun downloadAndEncrypt(url: String): Boolean {
        val request = Request.Builder().url(url).build()

        return try {
            client.newCall(request).execute().use { response ->
                if (!response.isSuccessful) throw IOException("Unexpected code $response")

                val rawBytes = response.body?.bytes() ?: return false
                
                // 暗号化して保存
                val encryptedBytes = cryptoManager.encrypt(rawBytes)
                audioFile.writeBytes(encryptedBytes)
                
                Log.d(TAG, "Download and Encryption successful.")
                true
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to download/encrypt audio", e)
            false
        }
    }
}