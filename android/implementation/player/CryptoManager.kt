import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

class CryptoManager {
    private val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
    private val keyAlias = "file_key"

    private fun getSecretKey(): SecretKey {
        val existingKey = keyStore.getEntry(keyAlias, null) as? KeyStore.SecretKeyEntry
        return existingKey?.secretKey ?: createKey()
    }

    private fun createKey(): SecretKey {
        val keyGenerator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        keyGenerator.init(
            KeyGenParameterSpec.Builder(keyAlias, KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT)
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .build()
        )
        return keyGenerator.generateKey()
    }

    fun encrypt(data: ByteArray): ByteArray {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        val iv = cipher.iv // 復号に必要
        val encrypted = cipher.doFinal(data)
        return iv + encrypted // IVを先頭に付与して保存
    }

    fun decrypt(encryptedDataWithIv: ByteArray): ByteArray {
        val iv = encryptedDataWithIv.copyOfRange(0, 12) // GCMの基本IVサイズ
        val encrypted = encryptedDataWithIv.copyOfRange(12, encryptedDataWithIv.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), GCMParameterSpec(128, iv))
        return cipher.doFinal(encrypted)
    }
}