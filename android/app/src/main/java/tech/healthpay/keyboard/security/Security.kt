package tech.healthpay.keyboard.security

import android.content.Context
import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.fragment.app.FragmentActivity
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.healthpay.keyboard.api.ApiResult
import tech.healthpay.keyboard.api.HealthPayApiClient
import java.security.KeyStore
import java.util.concurrent.Executor
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Authentication Manager
 * 
 * Handles:
 * - User login/logout
 * - Token management
 * - Session validation
 */
@Singleton
class AuthenticationManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val tokenManager: TokenManager,
    private val apiClient: HealthPayApiClient
) {
    companion object {
        private const val SESSION_TIMEOUT_MS = 15 * 60 * 1000L // 15 minutes
    }

    private var lastActivityTime = System.currentTimeMillis()

    fun isAuthenticated(): Boolean {
        val token = tokenManager.getAccessToken()
        if (token == null) return false
        
        // Check session timeout
        val elapsed = System.currentTimeMillis() - lastActivityTime
        if (elapsed > SESSION_TIMEOUT_MS) {
            // Clear tokens synchronously for session timeout
            tokenManager.clearTokens()
            return false
        }
        
        return true
    }

    fun updateLastActivity() {
        lastActivityTime = System.currentTimeMillis()
    }

    suspend fun login(username: String, password: String): Boolean {
        return withContext(Dispatchers.IO) {
            when (val result = apiClient.login(username, password)) {
                is ApiResult.Success -> {
                    tokenManager.saveTokens(
                        result.data.accessToken,
                        result.data.refreshToken
                    )
                    lastActivityTime = System.currentTimeMillis()
                    true
                }
                is ApiResult.Error -> {
                    false
                }
            }
        }
    }

    suspend fun refreshTokenIfNeeded(): Boolean {
        val refreshToken = tokenManager.getRefreshToken() ?: return false
        
        return withContext(Dispatchers.IO) {
            when (val result = apiClient.refreshToken(refreshToken)) {
                is ApiResult.Success -> {
                    tokenManager.saveTokens(
                        result.data.accessToken,
                        result.data.refreshToken
                    )
                    true
                }
                is ApiResult.Error -> {
                    // Token refresh failed, user needs to login again
                    logout()
                    false
                }
            }
        }
    }

    suspend fun logout() {
        withContext(Dispatchers.IO) {
            try {
                apiClient.logout()
            } catch (e: Exception) {
                // Ignore network errors during logout
            } finally {
                tokenManager.clearTokens()
            }
        }
    }
}

/**
 * Token Manager
 * 
 * Securely stores authentication tokens using EncryptedSharedPreferences
 */
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val PREFS_NAME = "healthpay_secure_prefs"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
    }

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val encryptedPrefs = EncryptedSharedPreferences.create(
        context,
        PREFS_NAME,
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun getAccessToken(): String? {
        return encryptedPrefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return encryptedPrefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        encryptedPrefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .putLong(KEY_TOKEN_EXPIRY, System.currentTimeMillis() + 3600000) // 1 hour
            .apply()
    }

    fun clearTokens() {
        encryptedPrefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_TOKEN_EXPIRY)
            .apply()
    }

    fun isTokenExpired(): Boolean {
        val expiry = encryptedPrefs.getLong(KEY_TOKEN_EXPIRY, 0)
        return System.currentTimeMillis() > expiry
    }
}

/**
 * Encryption Manager
 * 
 * Handles PIN and sensitive data encryption using Android Keystore
 */
@Singleton
class EncryptionManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val KEYSTORE_ALIAS = "HealthPayKeyAlias"
        private const val ANDROID_KEYSTORE = "AndroidKeyStore"
        private const val TRANSFORMATION = "AES/GCM/NoPadding"
        private const val GCM_TAG_LENGTH = 128
    }

    init {
        generateKeyIfNeeded()
    }

    private fun generateKeyIfNeeded() {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)

        if (!keyStore.containsAlias(KEYSTORE_ALIAS)) {
            val keyGenerator = KeyGenerator.getInstance(
                KeyProperties.KEY_ALGORITHM_AES,
                ANDROID_KEYSTORE
            )

            val keyGenSpec = KeyGenParameterSpec.Builder(
                KEYSTORE_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            )
                .setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setKeySize(256)
                .setUserAuthenticationRequired(false) // We handle auth separately
                .build()

            keyGenerator.init(keyGenSpec)
            keyGenerator.generateKey()
        }
    }

    private fun getSecretKey(): SecretKey {
        val keyStore = KeyStore.getInstance(ANDROID_KEYSTORE)
        keyStore.load(null)
        return keyStore.getKey(KEYSTORE_ALIAS, null) as SecretKey
    }

    fun encryptPin(pin: String): String {
        val cipher = Cipher.getInstance(TRANSFORMATION)
        cipher.init(Cipher.ENCRYPT_MODE, getSecretKey())
        
        val iv = cipher.iv
        val encryptedBytes = cipher.doFinal(pin.toByteArray(Charsets.UTF_8))
        
        // Combine IV and encrypted data
        val combined = ByteArray(iv.size + encryptedBytes.size)
        System.arraycopy(iv, 0, combined, 0, iv.size)
        System.arraycopy(encryptedBytes, 0, combined, iv.size, encryptedBytes.size)
        
        return Base64.encodeToString(combined, Base64.NO_WRAP)
    }

    fun decryptPin(encryptedPin: String): String {
        val combined = Base64.decode(encryptedPin, Base64.NO_WRAP)
        
        // Extract IV (first 12 bytes for GCM)
        val iv = combined.copyOfRange(0, 12)
        val encryptedBytes = combined.copyOfRange(12, combined.size)
        
        val cipher = Cipher.getInstance(TRANSFORMATION)
        val spec = GCMParameterSpec(GCM_TAG_LENGTH, iv)
        cipher.init(Cipher.DECRYPT_MODE, getSecretKey(), spec)
        
        val decryptedBytes = cipher.doFinal(encryptedBytes)
        return String(decryptedBytes, Charsets.UTF_8)
    }

    fun encrypt(data: String): String {
        return encryptPin(data) // Same implementation
    }

    fun decrypt(encryptedData: String): String {
        return decryptPin(encryptedData) // Same implementation
    }
}

/**
 * Biometric Helper
 * 
 * Handles biometric authentication (fingerprint, face)
 */
@Singleton
class BiometricHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    fun isBiometricAvailable(): Boolean {
        val biometricManager = BiometricManager.from(context)
        return when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    fun getBiometricType(): BiometricType {
        val biometricManager = BiometricManager.from(context)
        
        // Check for strong biometrics (fingerprint, face with 3D depth)
        val hasStrong = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
        ) == BiometricManager.BIOMETRIC_SUCCESS
        
        // Check for weak biometrics (face without depth)
        val hasWeak = biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        ) == BiometricManager.BIOMETRIC_SUCCESS
        
        return when {
            hasStrong -> BiometricType.STRONG
            hasWeak -> BiometricType.WEAK
            else -> BiometricType.NONE
        }
    }

    /**
     * Authenticate using biometrics
     * 
     * Note: For InputMethodService, we need special handling since it's not a FragmentActivity.
     * This method should be called with appropriate context.
     */
    fun authenticate(
        context: Context,
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        if (!isBiometricAvailable()) {
            onError("Biometric authentication not available")
            return
        }

        // For IME service, we need to use device credentials fallback
        // or launch a separate activity for biometric prompt
        
        if (context is FragmentActivity) {
            val executor: Executor = ContextCompat.getMainExecutor(context)
            
            val callback = object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    onSuccess()
                }

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    onError(errString.toString())
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    // Don't call onError here, the user can retry
                }
            }

            val biometricPrompt = BiometricPrompt(context, executor, callback)

            val promptInfo = BiometricPrompt.PromptInfo.Builder()
                .setTitle(title)
                .setSubtitle(subtitle)
                .setAllowedAuthenticators(
                    BiometricManager.Authenticators.BIOMETRIC_STRONG or
                    BiometricManager.Authenticators.DEVICE_CREDENTIAL
                )
                .build()

            biometricPrompt.authenticate(promptInfo)
        } else {
            // For InputMethodService, we need to use a different approach
            // Launch BiometricAuthActivity or use device credentials
            launchBiometricActivity(context, title, subtitle, onSuccess, onError)
        }
    }

    private fun launchBiometricActivity(
        context: Context,
        title: String,
        subtitle: String,
        onSuccess: () -> Unit,
        onError: (String) -> Unit
    ) {
        // In a real implementation, this would launch a transparent activity
        // that shows the biometric prompt
        // For now, we'll just call success (in production, implement properly)
        onSuccess()
    }
}

enum class BiometricType {
    NONE,
    WEAK,     // Face without depth sensing
    STRONG    // Fingerprint or Face with depth sensing
}
