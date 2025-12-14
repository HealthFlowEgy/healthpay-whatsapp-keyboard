package tech.healthpay.keyboard.security

import android.content.Context
import android.util.Log
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricManager.Authenticators

/**
 * BiometricHelper provides utilities for checking biometric availability
 * and capabilities.
 */
class BiometricHelper(private val context: Context) {

    companion object {
        private const val TAG = "BiometricHelper"
    }

    private val biometricManager: BiometricManager = BiometricManager.from(context)

    /**
     * Check if biometric authentication is available on this device.
     */
    fun isBiometricAvailable(): Boolean {
        return when (biometricManager.canAuthenticate(
            Authenticators.BIOMETRIC_STRONG or Authenticators.BIOMETRIC_WEAK
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> true
            else -> false
        }
    }

    /**
     * Get the biometric availability status.
     */
    fun getBiometricStatus(): BiometricStatus {
        return when (biometricManager.canAuthenticate(
            Authenticators.BIOMETRIC_STRONG or Authenticators.BIOMETRIC_WEAK
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> BiometricStatus.AVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> BiometricStatus.NO_HARDWARE
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> BiometricStatus.HARDWARE_UNAVAILABLE
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> BiometricStatus.NOT_ENROLLED
            BiometricManager.BIOMETRIC_ERROR_SECURITY_UPDATE_REQUIRED -> BiometricStatus.SECURITY_UPDATE_REQUIRED
            BiometricManager.BIOMETRIC_ERROR_UNSUPPORTED -> BiometricStatus.UNSUPPORTED
            BiometricManager.BIOMETRIC_STATUS_UNKNOWN -> BiometricStatus.UNKNOWN
            else -> BiometricStatus.UNKNOWN
        }
    }

    /**
     * Get a user-friendly message for the biometric status.
     */
    fun getBiometricStatusMessage(): String {
        return when (getBiometricStatus()) {
            BiometricStatus.AVAILABLE -> "Biometric authentication is available"
            BiometricStatus.NO_HARDWARE -> "This device doesn't have biometric hardware"
            BiometricStatus.HARDWARE_UNAVAILABLE -> "Biometric hardware is currently unavailable"
            BiometricStatus.NOT_ENROLLED -> "No biometric credentials are enrolled. Please set up fingerprint or face recognition in device settings."
            BiometricStatus.SECURITY_UPDATE_REQUIRED -> "A security update is required"
            BiometricStatus.UNSUPPORTED -> "Biometric authentication is not supported"
            BiometricStatus.UNKNOWN -> "Biometric status unknown"
        }
    }

    /**
     * Check if device has strong biometric capability.
     */
    fun hasStrongBiometric(): Boolean {
        return biometricManager.canAuthenticate(Authenticators.BIOMETRIC_STRONG) == 
            BiometricManager.BIOMETRIC_SUCCESS
    }

    /**
     * Biometric availability status.
     */
    enum class BiometricStatus {
        AVAILABLE,
        NO_HARDWARE,
        HARDWARE_UNAVAILABLE,
        NOT_ENROLLED,
        SECURITY_UPDATE_REQUIRED,
        UNSUPPORTED,
        UNKNOWN
    }
}
