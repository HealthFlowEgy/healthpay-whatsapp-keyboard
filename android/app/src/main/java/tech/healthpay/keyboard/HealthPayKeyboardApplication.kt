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
 * v1.2.1 - Complete implementation with manual DI
 */
class HealthPayKeyboardApplication : Application() {

    companion object {
        private const val TAG = "HealthPayApp"
        private const val PREFS_NAME = "healthpay_secure_prefs"
        
        @Volatile
        private var _instance: HealthPayKeyboardApplication? = null
        val instance: HealthPayKeyboardApplication
            get() = _instance ?: throw IllegalStateException("Application not initialized")

        // Singleton dependencies
        private var _sharedPreferences: SharedPreferences? = null
        val sharedPreferences: SharedPreferences
            get() = _sharedPreferences ?: throw IllegalStateException("SharedPreferences not initialized")

        private var _encryptionManager: EncryptionManager? = null
        val encryptionManager: EncryptionManager
            get() = _encryptionManager ?: throw IllegalStateException("EncryptionManager not initialized")

        private var _tokenManager: TokenManager? = null
        val tokenManager: TokenManager
            get() = _tokenManager ?: throw IllegalStateException("TokenManager not initialized")

        private var _biometricHelper: BiometricHelper? = null
        val biometricHelper: BiometricHelper
            get() = _biometricHelper ?: throw IllegalStateException("BiometricHelper not initialized")

        private var _apiClient: HealthPayApiClient? = null
        val apiClient: HealthPayApiClient
            get() = _apiClient ?: throw IllegalStateException("ApiClient not initialized")

        private var _authenticationManager: AuthenticationManager? = null
        val authenticationManager: AuthenticationManager
            get() = _authenticationManager ?: throw IllegalStateException("AuthenticationManager not initialized")
    }

    override fun onCreate() {
        super.onCreate()
        _instance = this
        initializeDependencies()
        Log.d(TAG, "Application initialized")
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

            // Initialize managers in order
            _encryptionManager = EncryptionManager(this)
            _tokenManager = TokenManager(_sharedPreferences!!)
            _biometricHelper = BiometricHelper(this)
            _apiClient = HealthPayApiClient(_tokenManager!!)
            _authenticationManager = AuthenticationManager(
                context = this,
                tokenManager = _tokenManager!!,
                biometricHelper = _biometricHelper!!
            )

            Log.d(TAG, "All dependencies initialized successfully")
        } catch (e: Exception) {
            Log.e(TAG, "Failed to initialize dependencies", e)
            throw e
        }
    }

    fun isUserLoggedIn(): Boolean {
        return _tokenManager?.hasValidToken() == true
    }

    fun logout() {
        _tokenManager?.clearTokens()
        _authenticationManager?.clearSession()
        Log.d(TAG, "User logged out")
    }
}
