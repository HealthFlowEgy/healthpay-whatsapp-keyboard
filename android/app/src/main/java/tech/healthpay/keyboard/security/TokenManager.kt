package tech.healthpay.keyboard.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * TokenManager handles secure storage and retrieval of authentication tokens.
 */
class TokenManager(context: Context) {

    companion object {
        private const val TAG = "TokenManager"
        private const val PREFS_NAME = "healthpay_tokens"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
    }

    private val prefs: SharedPreferences

    init {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()

        prefs = EncryptedSharedPreferences.create(
            context,
            PREFS_NAME,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    /**
     * Save access token.
     */
    fun saveAccessToken(token: String) {
        prefs.edit().putString(KEY_ACCESS_TOKEN, token).apply()
        Log.d(TAG, "Access token saved")
    }

    /**
     * Get access token.
     */
    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    /**
     * Save refresh token.
     */
    fun saveRefreshToken(token: String) {
        prefs.edit().putString(KEY_REFRESH_TOKEN, token).apply()
        Log.d(TAG, "Refresh token saved")
    }

    /**
     * Get refresh token.
     */
    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    /**
     * Save token expiry timestamp.
     */
    fun saveTokenExpiry(expiryTimestamp: Long) {
        prefs.edit().putLong(KEY_TOKEN_EXPIRY, expiryTimestamp).apply()
    }

    /**
     * Check if token is expired.
     */
    fun isTokenExpired(): Boolean {
        val expiry = prefs.getLong(KEY_TOKEN_EXPIRY, 0)
        return System.currentTimeMillis() > expiry
    }

    /**
     * Check if user has valid tokens.
     */
    fun hasValidTokens(): Boolean {
        val accessToken = getAccessToken()
        return !accessToken.isNullOrBlank() && !isTokenExpired()
    }

    /**
     * Clear all tokens.
     */
    fun clearTokens() {
        prefs.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_TOKEN_EXPIRY)
            apply()
        }
        Log.d(TAG, "All tokens cleared")
    }
}
