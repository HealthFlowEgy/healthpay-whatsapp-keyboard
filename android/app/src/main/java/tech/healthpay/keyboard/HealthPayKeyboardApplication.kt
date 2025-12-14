package tech.healthpay.keyboard

import android.app.Application
import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import tech.healthpay.keyboard.api.HealthPayApiClient
import tech.healthpay.keyboard.security.AuthenticationManager
import tech.healthpay.keyboard.security.BiometricHelper
import tech.healthpay.keyboard.security.EncryptionManager
import tech.healthpay.keyboard.security.TokenManager

/**
 * HealthPay Keyboard Application
 * 
 * Uses manual dependency injection via companion object singleton.
 * This approach avoids all Hilt compilation issues while maintaining
 * clean dependency management.
 */
class HealthPayKeyboardApplication : Application() {

    companion object {
        private const val TAG = "HealthPayApp"
        private const val PREFS_NAME = "healthpay_keyboard_prefs"
        
        // Preference Keys
        private const val KEY_IS_LOGGED_IN = "is_logged_in"
        private const val KEY_PHONE_NUMBER = "phone_number"
        private const val KEY_COMPLETED_INITIAL_LOGIN = "completed_initial_login"
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"

        // Singleton instance
        private lateinit var instance: HealthPayKeyboardApplication

        // Dependencies (lazily initialized)
        private var _sharedPreferences: SharedPreferences? = null
        private var _encryptionManager: EncryptionManager? = null
        private var _tokenManager: TokenManager? = null
        private var _biometricHelper: BiometricHelper? = null
        private var _apiClient: HealthPayApiClient? = null
        private var _authManager: AuthenticationManager? = null

        // ==================== DEPENDENCY ACCESSORS ====================

        val sharedPreferences: SharedPreferences
            get() = _sharedPreferences ?: throw IllegalStateException("SharedPreferences not initialized")

        val encryptionManager: EncryptionManager
            get() = _encryptionManager ?: throw IllegalStateException("EncryptionManager not initialized")

        val tokenManager: TokenManager
            get() = _tokenManager ?: throw IllegalStateException("TokenManager not initialized")

        val biometricHelper: BiometricHelper
            get() = _biometricHelper ?: throw IllegalStateException("BiometricHelper not initialized")

        val apiClient: HealthPayApiClient
            get() = _apiClient ?: throw IllegalStateException("HealthPayApiClient not initialized")

        val authManager: AuthenticationManager
            get() = _authManager ?: throw IllegalStateException("AuthenticationManager not initialized")

        val context: Context
            get() = instance.applicationContext

        // ==================== LOGIN STATE MANAGEMENT ====================

        /**
         * Check if user is currently logged in with a valid session.
         */
        fun isLoggedIn(): Boolean {
            return _sharedPreferences?.getBoolean(KEY_IS_LOGGED_IN, false) ?: false
        }

        /**
         * Set the login state.
         */
        fun setLoggedIn(loggedIn: Boolean) {
            _sharedPreferences?.edit()?.apply {
                putBoolean(KEY_IS_LOGGED_IN, loggedIn)
                if (loggedIn) {
                    putBoolean(KEY_COMPLETED_INITIAL_LOGIN, true)
                }
                apply()
            }
            Log.d(TAG, "Login state set to: $loggedIn")
        }

        /**
         * Check if user has ever completed the initial login (phone + OTP).
         * Used to determine if we should show LoginActivity or BiometricAuthActivity.
         */
        fun hasCompletedInitialLogin(): Boolean {
            return _sharedPreferences?.getBoolean(KEY_COMPLETED_INITIAL_LOGIN, false) ?: false
        }

        /**
         * Get the stored phone number.
         */
        fun getPhoneNumber(): String? {
            return _sharedPreferences?.getString(KEY_PHONE_NUMBER, null)
        }

        /**
         * Save the user's phone number.
         */
        fun setPhoneNumber(phoneNumber: String) {
            _sharedPreferences?.edit()?.apply {
                putString(KEY_PHONE_NUMBER, phoneNumber)
                apply()
            }
        }

        /**
         * Check if biometric authentication is enabled.
         */
        fun isBiometricEnabled(): Boolean {
            return _sharedPreferences?.getBoolean(KEY_BIOMETRIC_ENABLED, false) ?: false
        }

        /**
         * Enable or disable biometric authentication.
         */
        fun setBiometricEnabled(enabled: Boolean) {
            _sharedPreferences?.edit()?.apply {
                putBoolean(KEY_BIOMETRIC_ENABLED, enabled)
                apply()
            }
        }

        /**
         * Perform logout - clear all session data but preserve phone number for re-login.
         */
        fun logout() {
            Log.d(TAG, "Performing logout")
            
            // Clear tokens
            _tokenManager?.clearTokens()
            
            // Update login state
            _sharedPreferences?.edit()?.apply {
                putBoolean(KEY_IS_LOGGED_IN, false)
                // Keep phone number and completed_initial_login for easier re-authentication
                apply()
            }
        }

        /**
         * Perform full logout - clear all data including phone number.
         */
        fun fullLogout() {
            Log.d(TAG, "Performing full logout")
            
            // Clear tokens
            _tokenManager?.clearTokens()
            
            // Clear all preferences
            _sharedPreferences?.edit()?.apply {
                clear()
                apply()
            }
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
        initializeDependencies()
        Log.d(TAG, "HealthPayKeyboardApplication initialized")
    }

    private fun initializeDependencies() {
        try {
            // Initialize encrypted shared preferences
            val masterKey = MasterKey.Builder(this)
                .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
                .build()

            _sharedPreferences = EncryptedSharedPreferences.create(
                this,
                PREFS_NAME,
                masterKey,
                EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            )

            // Initialize managers in dependency order
            _encryptionManager = EncryptionManager(this)
            _tokenManager = TokenManager(this)
            _biometricHelper = BiometricHelper(this)
            _apiClient = HealthPayApiClient(_tokenManager!!)
            _authManager = AuthenticationManager(
                tokenManager = _tokenManager!!,
                biometricHelper = _biometricHelper!!,
                apiClient = _apiClient!!
            )

            Log.d(TAG, "All dependencies initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing dependencies", e)
            throw RuntimeException("Failed to initialize HealthPay dependencies", e)
        }
    }
}
