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
import tech.healthpay.keyboard.viewmodel.KeyboardSettingsRepository
import tech.healthpay.keyboard.viewmodel.KeyboardSettingsRepositoryImpl

/**
 * HealthPay Keyboard Application
 * 
 * NO HILT - Uses manual dependency injection via companion object singleton.
 * This approach avoids all Hilt compilation issues while maintaining
 * clean dependency management.
 */
class HealthPayKeyboardApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        instance = this
        initializeDependencies()
        Log.d(TAG, "HealthPayKeyboardApplication initialized")
    }

    private fun initializeDependencies() {
        try {
            // 1. Create encrypted SharedPreferences
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
            Log.d(TAG, "EncryptedSharedPreferences created")

            // 2. Create managers in correct dependency order
            _encryptionManager = EncryptionManager(this)
            _tokenManager = TokenManager(this)
            _biometricHelper = BiometricHelper(this)
            
            // 3. Create API client (depends on TokenManager)
            _apiClient = HealthPayApiClient(this, _tokenManager!!)
            
            // 4. Create AuthenticationManager (depends on TokenManager and ApiClient)
            _authenticationManager = AuthenticationManager(
                this,
                _tokenManager!!,
                _apiClient!!
            )
            
            // 5. Create settings repository
            _keyboardSettingsRepository = KeyboardSettingsRepositoryImpl(_sharedPreferences!!)
            
            Log.d(TAG, "All dependencies initialized successfully")
            
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize dependencies", e)
            // Fallback to non-encrypted preferences if encryption fails
            _sharedPreferences = getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            initializeFallbackDependencies()
        }
    }

    private fun initializeFallbackDependencies() {
        _encryptionManager = EncryptionManager(this)
        _tokenManager = TokenManager(this)
        _biometricHelper = BiometricHelper(this)
        _apiClient = HealthPayApiClient(this, _tokenManager!!)
        _authenticationManager = AuthenticationManager(this, _tokenManager!!, _apiClient!!)
        _keyboardSettingsRepository = KeyboardSettingsRepositoryImpl(_sharedPreferences!!)
    }

    companion object {
        private const val TAG = "HealthPayApp"
        private const val PREFS_NAME = "healthpay_secure_prefs"

        @Volatile
        private var instance: HealthPayKeyboardApplication? = null

        // Dependency singletons
        private var _sharedPreferences: SharedPreferences? = null
        private var _encryptionManager: EncryptionManager? = null
        private var _tokenManager: TokenManager? = null
        private var _biometricHelper: BiometricHelper? = null
        private var _apiClient: HealthPayApiClient? = null
        private var _authenticationManager: AuthenticationManager? = null
        private var _keyboardSettingsRepository: KeyboardSettingsRepository? = null

        /**
         * Get application instance
         */
        @JvmStatic
        fun getInstance(): HealthPayKeyboardApplication {
            return instance ?: throw IllegalStateException(
                "HealthPayKeyboardApplication not initialized. " +
                "Make sure the Application class is set in AndroidManifest.xml"
            )
        }

        /**
         * Get application context
         */
        @JvmStatic
        fun getAppContext(): Context = getInstance().applicationContext

        /**
         * Encrypted SharedPreferences
         */
        @JvmStatic
        val sharedPreferences: SharedPreferences
            get() = _sharedPreferences ?: throw notInitialized("SharedPreferences")

        /**
         * Encryption manager for secure data storage
         */
        @JvmStatic
        val encryptionManager: EncryptionManager
            get() = _encryptionManager ?: throw notInitialized("EncryptionManager")

        /**
         * Token manager for authentication tokens
         */
        @JvmStatic
        val tokenManager: TokenManager
            get() = _tokenManager ?: throw notInitialized("TokenManager")

        /**
         * Biometric authentication helper
         */
        @JvmStatic
        val biometricHelper: BiometricHelper
            get() = _biometricHelper ?: throw notInitialized("BiometricHelper")

        /**
         * HealthPay API client
         */
        @JvmStatic
        val apiClient: HealthPayApiClient
            get() = _apiClient ?: throw notInitialized("HealthPayApiClient")

        /**
         * Authentication manager
         * Note: This is also available as 'authManager' for compatibility
         */
        @JvmStatic
        val authenticationManager: AuthenticationManager
            get() = _authenticationManager ?: throw notInitialized("AuthenticationManager")

        /**
         * Alias for authenticationManager (for code that uses AuthManager)
         */
        @JvmStatic
        val authManager: AuthenticationManager
            get() = authenticationManager

        /**
         * Keyboard settings repository
         */
        @JvmStatic
        val keyboardSettingsRepository: KeyboardSettingsRepository
            get() = _keyboardSettingsRepository ?: throw notInitialized("KeyboardSettingsRepository")

        /**
         * Check if user is logged in
         */
        @JvmStatic
        fun isLoggedIn(): Boolean {
            return try {
                tokenManager.hasValidToken()
            } catch (e: Exception) {
                false
            }
        }

        /**
         * Log out user
         */
        @JvmStatic
        fun logout() {
            try {
                tokenManager.clearTokens()
                apiClient.clearAuth()
            } catch (e: Exception) {
                Log.e(TAG, "Error during logout", e)
            }
        }

        private fun notInitialized(name: String): IllegalStateException {
            return IllegalStateException(
                "$name not initialized. Ensure HealthPayKeyboardApplication.onCreate() has been called."
            )
        }
    }
}

/**
 * Type alias for backward compatibility with code that references AuthManager
 */
typealias AuthManager = AuthenticationManager
