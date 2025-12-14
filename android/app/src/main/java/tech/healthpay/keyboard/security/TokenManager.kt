package tech.healthpay.keyboard.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Token Manager - Securely stores and manages authentication tokens
 * 
 * NO HILT - Instantiated manually in Application class
 */
class TokenManager(private val context: Context) {

    private val prefs: SharedPreferences by lazy {
        try {
            val masterKey = MasterKey.Builder(context)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            EncryptedSharedPreferences.create(
                context,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )
        } catch (e: Exception) {
            Log.e(TAG, "Failed to create encrypted prefs, using standard prefs", e)
            context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        }
    }

    // Access Token
    fun setAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
    }

    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    // Refresh Token
    fun setRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
    }

    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    // Token Expiry
    fun setTokenExpiry(expiryTime: Long) {
        prefs.edit().putLong(KEY_TOKEN_EXPIRY, expiryTime).apply()
    }

    fun getTokenExpiry(): Long {
        return prefs.getLong(KEY_TOKEN_EXPIRY, 0L)
    }

    // User ID
    fun setUserId(userId: String) {
        prefs.edit().putString(KEY_USER_ID, userId).apply()
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    // Phone Number
    fun setPhoneNumber(phone: String) {
        prefs.edit().putString(KEY_PHONE_NUMBER, phone).apply()
    }

    fun getPhoneNumber(): String? {
        return prefs.getString(KEY_PHONE_NUMBER, null)
    }

    // Last Auth Time (for biometric session management)
    fun setLastAuthTime(timestamp: Long) {
        prefs.edit().putLong(KEY_LAST_AUTH_TIME, timestamp).apply()
    }

    fun getLastAuthTime(): Long {
        return prefs.getLong(KEY_LAST_AUTH_TIME, 0L)
    }

    /**
     * Check if user has a valid (non-expired) access token
     */
    fun hasValidToken(): Boolean {
        val token = getAccessToken()
        if (token.isNullOrBlank()) return false

        val expiry = getTokenExpiry()
        if (expiry > 0 && System.currentTimeMillis() > expiry) {
            return false
        }

        return true
    }

    /**
     * Check if the biometric session is still valid
     */
    fun isBiometricSessionValid(): Boolean {
        val lastAuth = getLastAuthTime()
        if (lastAuth == 0L) return false
        
        val sessionAge = System.currentTimeMillis() - lastAuth
        return sessionAge < BIOMETRIC_SESSION_TIMEOUT
    }

    /**
     * Check if token needs refresh (within 5 minutes of expiry)
     */
    fun needsRefresh(): Boolean {
        val expiry = getTokenExpiry()
        if (expiry == 0L) return false
        
        val timeToExpiry = expiry - System.currentTimeMillis()
        return timeToExpiry in 0..REFRESH_THRESHOLD
    }

    /**
     * Clear all authentication tokens
     */
    fun clearTokens() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_TOKEN_EXPIRY)
            .remove(KEY_USER_ID)
            .remove(KEY_LAST_AUTH_TIME)
            // Keep phone number for convenience on re-login
            .apply()
        Log.d(TAG, "Tokens cleared")
    }

    /**
     * Clear all stored data including phone number
     */
    fun clearAll() {
        prefs.edit().clear().apply()
        Log.d(TAG, "All data cleared")
    }

    /**
     * Store complete auth response
     */
    fun storeAuthResponse(
        accessToken: String,
        refreshToken: String?,
        expiresIn: Long?,
        userId: String?
    ) {
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            refreshToken?.let { putString(KEY_REFRESH_TOKEN, it) }
            expiresIn?.let { putLong(KEY_TOKEN_EXPIRY, System.currentTimeMillis() + (it * 1000)) }
            userId?.let { putString(KEY_USER_ID, it) }
            putLong(KEY_LAST_AUTH_TIME, System.currentTimeMillis())
            apply()
        }
    }

    companion object {
        private const val TAG = "TokenManager"
        private const val PREFS_NAME = "healthpay_tokens"

        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_PHONE_NUMBER = "phone_number"
        private const val KEY_LAST_AUTH_TIME = "last_auth_time"

        // Session timeout: 15 minutes
        private const val BIOMETRIC_SESSION_TIMEOUT = 15 * 60 * 1000L
        
        // Refresh threshold: 5 minutes before expiry
        private const val REFRESH_THRESHOLD = 5 * 60 * 1000L
    }
}
