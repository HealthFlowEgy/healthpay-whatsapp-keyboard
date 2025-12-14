package tech.healthpay.keyboard.security

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Authentication Manager - Handles authentication state and session management
 */
class AuthenticationManager(
    private val context: Context,
    private val tokenManager: TokenManager,
    private val biometricHelper: BiometricHelper
) {

    companion object {
        private const val TAG = "AuthenticationManager"
        private const val PREFS_NAME = "healthpay_auth_prefs"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_LAST_AUTH_TIME = "last_auth_time"
        private const val KEY_SESSION_TIMEOUT = "session_timeout"
        private const val DEFAULT_SESSION_TIMEOUT = 15 * 60 * 1000L // 15 minutes
    }

    private val authPrefs: SharedPreferences by lazy {
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
    }

    fun isAuthenticated(): Boolean {
        if (!tokenManager.hasValidToken()) {
            Log.d(TAG, "No valid token")
            return false
        }

        val lastAuthTime = authPrefs.getLong(KEY_LAST_AUTH_TIME, 0)
        val sessionTimeout = authPrefs.getLong(KEY_SESSION_TIMEOUT, DEFAULT_SESSION_TIMEOUT)
        val isSessionValid = System.currentTimeMillis() - lastAuthTime < sessionTimeout

        Log.d(TAG, "Session valid: $isSessionValid")
        return isSessionValid
    }

    fun updateAuthTime() {
        authPrefs.edit().putLong(KEY_LAST_AUTH_TIME, System.currentTimeMillis()).apply()
        Log.d(TAG, "Auth time updated")
    }

    fun isBiometricEnabled(): Boolean {
        return authPrefs.getBoolean(KEY_BIOMETRIC_ENABLED, false) && biometricHelper.canAuthenticate()
    }

    fun setBiometricEnabled(enabled: Boolean) {
        authPrefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
        Log.d(TAG, "Biometric enabled: $enabled")
    }

    fun setSessionTimeout(timeoutMs: Long) {
        authPrefs.edit().putLong(KEY_SESSION_TIMEOUT, timeoutMs).apply()
    }

    fun getSessionTimeout(): Long {
        return authPrefs.getLong(KEY_SESSION_TIMEOUT, DEFAULT_SESSION_TIMEOUT)
    }

    fun requiresReauthentication(): Boolean {
        if (!tokenManager.hasValidToken()) return true
        if (!isAuthenticated()) return true
        return false
    }

    fun clearSession() {
        authPrefs.edit().apply {
            remove(KEY_LAST_AUTH_TIME)
            apply()
        }
        Log.d(TAG, "Session cleared")
    }

    fun logout() {
        tokenManager.clearTokens()
        clearSession()
        Log.d(TAG, "Logged out completely")
    }
}
