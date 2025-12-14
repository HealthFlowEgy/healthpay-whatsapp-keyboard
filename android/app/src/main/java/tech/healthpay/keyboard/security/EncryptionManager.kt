package tech.healthpay.keyboard.security

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import android.util.Log
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * EncryptionManager handles encryption/decryption of sensitive data
 * using Android Keystore.
 */
class EncryptionManager(private val context: Context) {

    companion object {
        private const val TAG = "EncryptionManager"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "HealthPayKeyboardKey"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
    }

    private val keyStore: KeyStore = KeyStore.getInstance(ANDROID_KEYSTORE).apply {
        load(null)
    }

    init {
        if (!keyStore.containsAlias(KEY_ALIAS)) {
            generateKey()
        }
    }

    /**
     * Generate a new encryption key.
     */
    private fun generateKey() {
        try {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )

            val keyGenParameterSpec = KeyGenParameterSpec.Builder(
                KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .build()

            keyGenerator.init(keyGenParameterSpec)
            keyGenerator.generateKey()
            Log.d(TAG, "Encryption key generated")
        } catch (e: Exception) {
            Log.e(TAG, "Error generating key", e)
        }
    }

    /**
     * Get the secret key from keystore.
     */
    private fun getSecretKey(): SecretKey? {
        return try {
            val entry = keyStore.getEntry(KEY_ALIAS, null) as? KeyStore.SecretKeyEntry
            entry?.secretKey
        } catch (e: Exception) {
            Log.e(TAG, "Error getting secret key", e)
            null
        }
    }

    /**
     * Encrypt a string value.
     * Returns Base64 encoded string containing IV + encrypted data.
     */
    fun encrypt(plainText: String): String? {
        return try {
            val secretKey = getSecretKey() ?: return null
            val cipher = Cipher.getInstance(TRANSFORMATION)
            cipher.init(Cipher.ENCRYPT_MODE, secretKey)

            val iv = cipher.iv
            val encryptedBytes = cipher.doFinal(plainText.toByteArray(Charsets.UTF_8))

            // Combine IV and encrypted data
            val combined = ByteArray(iv.size + encryptedBytes.size)
            System.arraycopy(iv, 0, combined, 0, iv.size)
            System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)

            Base64.encodeToString(combined, Base64.NO_WRAP)
        } catch (e: Exception) {
            Log.e(TAG, "Encryption error", e)
            null
        }
    }

    /**
     * Decrypt a string value.
     * Expects Base64 encoded string containing IV + encrypted data.
     */
    fun decrypt(encryptedText: String): String? {
        return try {
            val secretKey = getSecretKey() ?: return null
            val combined = Base64.decode(encryptedText, Base64.NO_WRAP)

            // Extract IV (first 12 bytes for GCM)
            val iv = combined.copyOfRange(0, 12)
            val encryptedBytes = combined.copyOfRange(12, combined.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption error", e)
            null
        }
    }

    /**
     * Delete the encryption key.
     */
    fun deleteKey() {
        try {
            if (keyStore.containsAlias(KEY_ALIAS)) {
                keyStore.deleteEntry(KEY_ALIAS)
                Log.d(TAG, "Encryption key deleted")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error deleting key", e)
        }
    }
}
