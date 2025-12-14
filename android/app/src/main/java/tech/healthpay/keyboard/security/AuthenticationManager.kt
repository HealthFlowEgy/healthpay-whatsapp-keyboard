package tech.healthpay.keyboard.security

import android.util.Log
import tech.healthpay.keyboard.api.HealthPayApiClient

/**
 * AuthenticationManager coordinates the authentication flow
 * and manages user session state.
 */
class AuthenticationManager(
    private val tokenManager: TokenManager,
    private val biometricHelper: BiometricHelper,
    private val apiClient: HealthPayApiClient
) {
    companion object {
        private const val TAG = "AuthenticationManager"
    }

    /**
     * Check if user is authenticated with valid tokens.
     */
    fun isAuthenticated(): Boolean {
        return tokenManager.hasValidTokens()
    }

    /**
     * Check if biometric authentication is available and should be used.
     */
    fun shouldUseBiometric(): Boolean {
        return biometricHelper.isBiometricAvailable()
    }

    /**
     * Get the authentication method available.
     */
    fun getAuthMethod(): AuthMethod {
        return when {
            biometricHelper.hasStrongBiometric() -> AuthMethod.BIOMETRIC_STRONG
            biometricHelper.isBiometricAvailable() -> AuthMethod.BIOMETRIC_WEAK
            else -> AuthMethod.PIN_OR_PASSWORD
        }
    }

    /**
     * Clear authentication state (logout).
     */
    fun logout() {
        tokenManager.clearTokens()
        Log.d(TAG, "User logged out")
    }

    /**
     * Authentication methods available.
     */
    enum class AuthMethod {
        BIOMETRIC_STRONG,
        BIOMETRIC_WEAK,
        PIN_OR_PASSWORD
    }
}
