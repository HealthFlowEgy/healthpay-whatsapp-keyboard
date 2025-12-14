package tech.healthpay.keyboard.security

import android.content.Context
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators

/**
 * Biometric Helper - Handles biometric authentication availability checks
 * 
 * NO HILT - Instantiated manually in Application class
 */
class BiometricHelper(private val context: Context) {

    private val biometricManager: BiometricManager by lazy {
        BiometricManager.from(context)
    }

    /**
     * Check if any biometric authentication is available
     */
    fun isBiometricAvailable(): Boolean {
        return when (biometricManager.canAuthenticate(BIOMETRIC_AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Check if strong biometric (fingerprint with TEE, face with hardware security) is available
     */
    fun isStrongBiometricAvailable(): Boolean {
        return when (biometricManager.canAuthenticate(Authenticators.BIOMETRIC_STRONG)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Check if device credential (PIN/pattern/password) is available
     */
    fun isDeviceCredentialAvailable(): Boolean {
        return when (biometricManager.canAuthenticate(Authenticators.DEVICE_CREDENTIAL)) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Get detailed biometric status
     */
    fun getBiometricStatus(): BiometricStatus {
        return when (biometricManager.canAuthenticate(BIOMETRIC_AUTHENTICATORS)) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.HARDWARE_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NONE_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricStatus.SECURITY_UPDATE_REQUIRED
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricStatus.UNSUPPORTED
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricStatus.UNKNOWN
            else -> BiometricStatus.UNKNOWN
        }
    }

    /**
     * Get human-readable status message
     */
    fun getBiometricStatusMessage(): String {
        return when (getBiometricStatus()) {
            BiometricStatus.AVAILABLE -> "Biometric authentication is available"
            BiometricStatus.NO_HARDWARE -> "This device does not have biometric hardware"
            BiometricStatus.HARDWARE_UNAVAILABLE -> "Biometric hardware is currently unavailable"
            BiometricStatus.NONE_ENROLLED -> "No biometric credentials enrolled. Please set up fingerprint or face recognition in device settings."
            BiometricStatus.SECURITY_UPDATE_REQUIRED -> "A security update is required to use biometrics"
            BiometricStatus.UNSUPPORTED -> "Biometric authentication is not supported"
            BiometricStatus.UNKNOWN -> "Unable to determine biometric availability"
        }
    }

    /**
     * Get recommended authenticators for BiometricPrompt
     */
    fun getRecommendedAuthenticators(): Int {
        return when {
            isStrongBiometricAvailable() -> Authenticators.BIOMETRIC_STRONG
            isBiometricAvailable() -> BIOMETRIC_AUTHENTICATORS
            isDeviceCredentialAvailable() -> Authenticators.DEVICE_CREDENTIAL
            else -> BIOMETRIC_AUTHENTICATORS
        }
    }

    /**
     * Biometric availability status
     */
    enum class BiometricStatus {
        AVAILABLE,
        NO_HARDWARE,
        HARDWARE_UNAVAILABLE,
        NONE_ENROLLED,
        SECURITY_UPDATE_REQUIRED,
        UNSUPPORTED,
        UNKNOWN
    }

    companion object {
        private const val BIOMETRIC_AUTHENTICATORS = 
            Authenticators.BIOMETRIC_STRONG or Authenticators.BIOMETRIC_WEAK
    }
}
