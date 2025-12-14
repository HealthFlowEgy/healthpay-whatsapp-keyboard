package tech.healthpay.keyboard.security

import android.content.SharedPreferences
import android.util.Log

/**
 * Token Manager - Handles secure storage and retrieval of auth tokens
 */
class TokenManager(private val prefs: SharedPreferences) {

    companion object {
        private const val TAG = "TokenManager"
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_TOKEN_EXPIRY = "token_expiry"
        private const val KEY_USER_ID = "user_id"
        private const val KEY_MOBILE = "user_mobile"
    }

    fun saveTokens(accessToken: String, refreshToken: String, expiresIn: Int = 3600) {
        val expiryTime = System.currentTimeMillis() + (expiresIn * 1000L)
        prefs.edit().apply {
            putString(KEY_ACCESS_TOKEN, accessToken)
            putString(KEY_REFRESH_TOKEN, refreshToken)
            putLong(KEY_TOKEN_EXPIRY, expiryTime)
            apply()
        }
        Log.d(TAG, "Tokens saved, expires in ${expiresIn}s")
    }

    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun hasValidToken(): Boolean {
        val token = getAccessToken()
        val expiry = prefs.getLong(KEY_TOKEN_EXPIRY, 0)
        val isValid = !token.isNullOrEmpty() && System.currentTimeMillis() < expiry
        Log.d(TAG, "Token valid: $isValid")
        return isValid
    }

    fun isTokenExpired(): Boolean {
        val expiry = prefs.getLong(KEY_TOKEN_EXPIRY, 0)
        return System.currentTimeMillis() >= expiry
    }

    fun saveUserInfo(userId: String, mobile: String) {
        prefs.edit().apply {
            putString(KEY_USER_ID, userId)
            putString(KEY_MOBILE, mobile)
            apply()
        }
    }

    fun getUserId(): String? {
        return prefs.getString(KEY_USER_ID, null)
    }

    fun getUserMobile(): String? {
        return prefs.getString(KEY_MOBILE, null)
    }

    fun clearTokens() {
        prefs.edit().apply {
            remove(KEY_ACCESS_TOKEN)
            remove(KEY_REFRESH_TOKEN)
            remove(KEY_TOKEN_EXPIRY)
            remove(KEY_USER_ID)
            remove(KEY_MOBILE)
            apply()
        }
        Log.d(TAG, "Tokens cleared")
    }
}
