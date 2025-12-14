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
 * Encryption Manager - Handles data encryption using Android Keystore
 * 
 * Uses AES-256-GCM for encryption with hardware-backed key storage
 * 
 * NO HILT - Instantiated manually in Application class
 */
class EncryptionManager(private val context: Context) {

    private val keyStore: KeyStore by lazy {
        KeyStore.getInstance(ANDROID_KEYSTORE).apply {
            load(null)
        }
    }

    init {
        createKeyIfNeeded()
    }

    private fun createKeyIfNeeded() {
        try {
            if (!keyStore.containsAlias(KEY_ALIAS)) {
                val keyGenerator = KeyGenerator.getInstance(
                    KeyProperties.KEY_ALGORITHM_AES,
                    ANDROID_KEYSTORE
                )

                val keySpec = KeyGenParameterSpec.Builder(
                    KEY_ALIAS,
                    KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
                )
                    .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                    .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                    .setKeySize(256)
                    .setRandomizedEncryptionRequired(true)
                    .build()

                keyGenerator.init(keySpec)
                keyGenerator.generateKey()
                Log.d(TAG, "Encryption key created")
            }
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create encryption key", e)
        }
    }

    private fun getSecretKey(): SecretKey? {
        return try {
            keyStore.getKey(KEY_ALIAS, null) as? SecretKey
        } catch (e: Exception) {
            Log.e(TAG, "Failed to get secret key", e)
            null
        }
    }

    /**
     * Encrypt plaintext data
     * @return Base64 encoded encrypted data (IV + ciphertext) or null on failure
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
            Log.e(TAG, "Encryption failed", e)
            null
        }
    }

    /**
     * Decrypt encrypted data
     * @param encryptedData Base64 encoded encrypted data (IV + ciphertext)
     * @return Decrypted plaintext or null on failure
     */
    fun decrypt(encryptedData: String): String? {
        return try {
            val secretKey = getSecretKey() ?: return null

            val combined = Base64.decode(encryptedData, Base64.NO_WRAP)

            // Extract IV (first 12 bytes for GCM)
            val iv = combined.copyOfRange(0, GCM_IV_LENGTH)
            val encryptedBytes = combined.copyOfRange(GCM_IV_LENGTH, combined.size)

            val cipher = Cipher.getInstance(TRANSFORMATION)
            val spec = GCMParameterSpec(GCM_TAG_LENGTH * 8, iv)
            cipher.init(Cipher.DECRYPT_MODE, secretKey, spec)

            val decryptedBytes = cipher.doFinal(encryptedBytes)
            String(decryptedBytes, Charsets.UTF_8)
        } catch (e: Exception) {
            Log.e(TAG, "Decryption failed", e)
            null
        }
    }

    /**
     * Check if encryption is available
     */
    fun isEncryptionAvailable(): Boolean {
        return getSecretKey() != null
    }

    /**
     * Delete the encryption key (WARNING: encrypted data will be unrecoverable)
     */
    fun deleteKey() {
        try {
            keyStore.deleteEntry(KEY_ALIAS)
            Log.d(TAG, "Encryption key deleted")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to delete key", e)
        }
    }

    companion object {
        private const val TAG = "EncryptionManager"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val KEY_ALIAS = "healthpay_encryption_key"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_IV_LENGTH = 12
        private const val GCM_TAG_LENGTH = 16
    }
}
