package tech.healthpay.keyboard.security

import android.content.Context
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tech.healthpay.keyboard.api.HealthPayApiClient

/**
 * Authentication Manager - Handles all authentication flows
 * 
 * Manages OTP login, token refresh, session management, and logout.
 * 
 * NO HILT - Instantiated manually in Application class
 */
class AuthenticationManager(
    private val context: Context,
    private val tokenManager: TokenManager,
    private val apiClient: HealthPayApiClient
) {

    /**
     * Check if user is currently authenticated
     */
    fun isAuthenticated(): Boolean {
        return tokenManager.hasValidToken()
    }

    /**
     * Check if biometric session is still valid
     */
    fun isBiometricSessionValid(): Boolean {
        return tokenManager.isBiometricSessionValid()
    }

    /**
     * Check if token needs refresh
     */
    fun needsTokenRefresh(): Boolean {
        return tokenManager.needsRefresh()
    }

    /**
     * Request OTP for phone number
     */
    suspend fun requestOtp(phoneNumber: String): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Requesting OTP for: ${phoneNumber.takeLast(4)}")
                
                val response = apiClient.requestOtp(phoneNumber)
                
                if (response.success) {
                    tokenManager.setPhoneNumber(phoneNumber)
                    AuthResult.Success(response.message ?: "OTP sent successfully")
                } else {
                    AuthResult.Error(response.message ?: "Failed to send OTP")
                }
            } catch (e: Exception) {
                Log.e(TAG, "OTP request failed", e)
                AuthResult.Error(e.message ?: "Network error")
            }
        }
    }

    /**
     * Verify OTP and complete login
     */
    suspend fun verifyOtp(phoneNumber: String, otp: String): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                Log.d(TAG, "Verifying OTP for: ${phoneNumber.takeLast(4)}")
                
                val response = apiClient.verifyOtp(phoneNumber, otp)
                
                if (response.success && response.accessToken != null) {
                    // Store tokens
                    tokenManager.storeAuthResponse(
                        accessToken = response.accessToken,
                        refreshToken = response.refreshToken,
                        expiresIn = response.expiresIn,
                        userId = response.userId
                    )
                    
                    Log.d(TAG, "Login successful")
                    AuthResult.Success("Login successful")
                } else {
                    AuthResult.Error(response.message ?: "Invalid OTP")
                }
            } catch (e: Exception) {
                Log.e(TAG, "OTP verification failed", e)
                AuthResult.Error(e.message ?: "Network error")
            }
        }
    }

    /**
     * Refresh authentication tokens
     */
    suspend fun refreshToken(): AuthResult {
        return withContext(Dispatchers.IO) {
            try {
                val refreshToken = tokenManager.getRefreshToken()
                    ?: return@withContext AuthResult.Error("No refresh token available")

                Log.d(TAG, "Refreshing token")
                
                val response = apiClient.refreshToken(refreshToken)
                
                if (response.success && response.accessToken != null) {
                    tokenManager.storeAuthResponse(
                        accessToken = response.accessToken,
                        refreshToken = response.refreshToken ?: refreshToken,
                        expiresIn = response.expiresIn,
                        userId = response.userId
                    )
                    
                    Log.d(TAG, "Token refreshed")
                    AuthResult.Success("Token refreshed")
                } else {
                    // Refresh failed - session expired
                    logout()
                    AuthResult.Error("Session expired, please login again")
                }
            } catch (e: Exception) {
                Log.e(TAG, "Token refresh failed", e)
                AuthResult.Error(e.message ?: "Network error")
            }
        }
    }

    /**
     * Update biometric session timestamp
     */
    fun updateBiometricSession() {
        tokenManager.setLastAuthTime(System.currentTimeMillis())
    }

    /**
     * Log out user and clear all tokens
     */
    fun logout() {
        Log.d(TAG, "Logging out")
        tokenManager.clearTokens()
        apiClient.clearAuth()
    }

    /**
     * Clear all data including phone number
     */
    fun clearAllData() {
        Log.d(TAG, "Clearing all data")
        tokenManager.clearAll()
        apiClient.clearAuth()
    }

    /**
     * Get current user ID
     */
    fun getUserId(): String? = tokenManager.getUserId()

    /**
     * Get current phone number
     */
    fun getPhoneNumber(): String? = tokenManager.getPhoneNumber()

    /**
     * Get current access token (for API calls)
     */
    fun getAccessToken(): String? = tokenManager.getAccessToken()

    /**
     * Authentication result sealed class
     */
    sealed class AuthResult {
        data class Success(val message: String) : AuthResult()
        data class Error(val message: String) : AuthResult()
        
        val isSuccess: Boolean get() = this is Success
        val isError: Boolean get() = this is Error
    }

    companion object {
        private const val TAG = "AuthenticationManager"
    }
}
