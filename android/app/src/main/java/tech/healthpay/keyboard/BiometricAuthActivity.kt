package tech.healthpay.keyboard

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat

/**
 * Biometric Authentication Activity
 * 
 * Handles biometric prompts for login and transaction confirmation.
 * Transparent activity that shows only the biometric prompt.
 * 
 * NO HILT - Uses Application singleton for dependencies
 */
class BiometricAuthActivity : AppCompatActivity() {

    private val biometricHelper get() = HealthPayKeyboardApplication.biometricHelper
    private val tokenManager get() = HealthPayKeyboardApplication.tokenManager

    private lateinit var biometricPrompt: BiometricPrompt
    private var authMode: String = MODE_LOGIN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No layout - transparent activity

        authMode = intent.getStringExtra(EXTRA_AUTH_MODE) ?: MODE_LOGIN

        if (!biometricHelper.isBiometricAvailable()) {
            handleBiometricUnavailable()
            return
        }

        setupBiometricPrompt()
        showPrompt()
    }

    private fun handleBiometricUnavailable() {
        Toast.makeText(this, biometricHelper.getBiometricStatusMessage(), Toast.LENGTH_LONG).show()
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    private fun setupBiometricPrompt() {
        val executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    handleError(errorCode, errString.toString())
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    handleSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(
                        this@BiometricAuthActivity,
                        R.string.biometric_not_recognized,
                        Toast.LENGTH_SHORT
                    ).show()
                    // Don't finish - let user retry
                }
            })
    }

    private fun showPrompt() {
        val title = when (authMode) {
            MODE_LOGIN -> getString(R.string.biometric_login_title)
            MODE_TRANSACTION -> getString(R.string.biometric_transaction_title)
            else -> getString(R.string.biometric_auth_title)
        }

        val subtitle = when (authMode) {
            MODE_LOGIN -> getString(R.string.biometric_login_subtitle)
            MODE_TRANSACTION -> intent.getStringExtra(EXTRA_TRANSACTION_INFO)
                ?: getString(R.string.biometric_transaction_subtitle)
            else -> getString(R.string.biometric_auth_subtitle)
        }

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(title)
            .setSubtitle(subtitle)
            .setNegativeButtonText(getString(R.string.cancel))
            .setAllowedAuthenticators(biometricHelper.getRecommendedAuthenticators())
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun handleSuccess() {
        when (authMode) {
            MODE_LOGIN -> {
                tokenManager.setLastAuthTime(System.currentTimeMillis())
            }
            MODE_TRANSACTION -> {
                // Transaction confirmed
            }
        }

        setResult(Activity.RESULT_OK, Intent().apply {
            putExtra(EXTRA_AUTH_MODE, authMode)
            putExtra(EXTRA_AUTH_SUCCESS, true)
        })
        finish()
    }

    private fun handleError(errorCode: Int, message: String) {
        val isCancelled = errorCode == BiometricPrompt.ERROR_USER_CANCELED ||
                errorCode == BiometricPrompt.ERROR_NEGATIVE_BUTTON ||
                errorCode == BiometricPrompt.ERROR_CANCELED

        if (!isCancelled) {
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
        }

        setResult(Activity.RESULT_CANCELED, Intent().apply {
            putExtra(EXTRA_AUTH_MODE, authMode)
            putExtra(EXTRA_AUTH_SUCCESS, false)
            putExtra(EXTRA_ERROR_CODE, errorCode)
            putExtra(EXTRA_ERROR_MESSAGE, message)
        })
        finish()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        super.onBackPressed()
    }

    companion object {
        const val EXTRA_AUTH_MODE = "auth_mode"
        const val EXTRA_TRANSACTION_INFO = "transaction_info"
        const val EXTRA_AUTH_SUCCESS = "auth_success"
        const val EXTRA_ERROR_CODE = "error_code"
        const val EXTRA_ERROR_MESSAGE = "error_message"

        const val MODE_LOGIN = "login"
        const val MODE_TRANSACTION = "transaction"
    }
}
