package tech.healthpay.keyboard.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import tech.healthpay.keyboard.HealthPayKeyboardApplication
import tech.healthpay.keyboard.R
import java.util.concurrent.Executor

/**
 * BiometricAuthActivity handles biometric authentication.
 * 
 * Modes:
 * - MODE_SETUP: Initial biometric setup after first login
 * - MODE_LOGIN: Re-authentication using biometric
 * - MODE_TRANSACTION: Transaction confirmation
 */
class BiometricAuthActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "BiometricAuthActivity"
        
        const val EXTRA_AUTH_MODE = "auth_mode"
        const val MODE_SETUP = "setup"
        const val MODE_LOGIN = "login"
        const val MODE_TRANSACTION = "transaction"
        
        const val EXTRA_TRANSACTION_AMOUNT = "transaction_amount"
        const val EXTRA_TRANSACTION_RECIPIENT = "transaction_recipient"
    }

    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    // UI Elements
    private lateinit var ivBiometricIcon: ImageView
    private lateinit var tvTitle: TextView
    private lateinit var tvDescription: TextView
    private lateinit var btnAuthenticate: Button
    private lateinit var btnSkip: Button
    private lateinit var progressBar: ProgressBar

    private var authMode: String = MODE_LOGIN

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_biometric_auth)

        authMode = intent.getStringExtra(EXTRA_AUTH_MODE) ?: MODE_LOGIN

        initViews()
        setupBiometric()
        updateUIForMode()

        // Auto-trigger biometric prompt for login mode
        if (authMode == MODE_LOGIN || authMode == MODE_TRANSACTION) {
            showBiometricPrompt()
        }
    }

    private fun initViews() {
        ivBiometricIcon = findViewById(R.id.iv_biometric_icon)
        tvTitle = findViewById(R.id.tv_title)
        tvDescription = findViewById(R.id.tv_description)
        btnAuthenticate = findViewById(R.id.btn_authenticate)
        btnSkip = findViewById(R.id.btn_skip)
        progressBar = findViewById(R.id.progress_bar)

        btnAuthenticate.setOnClickListener {
            showBiometricPrompt()
        }

        btnSkip.setOnClickListener {
            handleSkip()
        }
    }

    private fun setupBiometric() {
        executor = ContextCompat.getMainExecutor(this)

        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)
                    Log.e(TAG, "Authentication error: $errString (code: $errorCode)")
                    
                    when (errorCode) {
                        BiometricPrompt.ERROR_USER_CANCELED,
                        BiometricPrompt.ERROR_NEGATIVE_BUTTON -> {
                            // User cancelled - do nothing for setup, fail for login
                            if (authMode != MODE_SETUP) {
                                Toast.makeText(
                                    this@BiometricAuthActivity,
                                    "Authentication cancelled",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        BiometricPrompt.ERROR_LOCKOUT,
                        BiometricPrompt.ERROR_LOCKOUT_PERMANENT -> {
                            Toast.makeText(
                                this@BiometricAuthActivity,
                                "Too many attempts. Please try again later.",
                                Toast.LENGTH_LONG
                            ).show()
                            if (authMode != MODE_SETUP) {
                                setResult(RESULT_CANCELED)
                                finish()
                            }
                        }
                        else -> {
                            Toast.makeText(
                                this@BiometricAuthActivity,
                                "Authentication error: $errString",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    Log.d(TAG, "Authentication succeeded")
                    handleAuthenticationSuccess()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Log.w(TAG, "Authentication failed")
                    Toast.makeText(
                        this@BiometricAuthActivity,
                        "Authentication failed. Please try again.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            })

        val promptTitle = when (authMode) {
            MODE_SETUP -> getString(R.string.biometric_setup_title)
            MODE_TRANSACTION -> "Confirm Payment"
            else -> getString(R.string.biometric_title)
        }

        val promptSubtitle = when (authMode) {
            MODE_TRANSACTION -> {
                val amount = intent.getStringExtra(EXTRA_TRANSACTION_AMOUNT) ?: ""
                val recipient = intent.getStringExtra(EXTRA_TRANSACTION_RECIPIENT) ?: ""
                "Pay $amount to $recipient"
            }
            else -> getString(R.string.biometric_subtitle)
        }

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle(promptTitle)
            .setSubtitle(promptSubtitle)
            .setNegativeButtonText(getString(R.string.cancel))
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG or
                BiometricManager.Authenticators.BIOMETRIC_WEAK
            )
            .build()
    }

    private fun updateUIForMode() {
        when (authMode) {
            MODE_SETUP -> {
                tvTitle.text = getString(R.string.biometric_setup_title)
                tvDescription.text = getString(R.string.biometric_setup_description)
                btnAuthenticate.text = getString(R.string.enable_biometric)
                btnSkip.visibility = View.VISIBLE
            }
            MODE_LOGIN -> {
                tvTitle.text = getString(R.string.biometric_title)
                tvDescription.text = getString(R.string.biometric_subtitle)
                btnAuthenticate.text = "Authenticate"
                btnSkip.visibility = View.GONE
            }
            MODE_TRANSACTION -> {
                val amount = intent.getStringExtra(EXTRA_TRANSACTION_AMOUNT) ?: ""
                tvTitle.text = "Confirm Payment"
                tvDescription.text = "Authenticate to send $amount"
                btnAuthenticate.text = "Confirm"
                btnSkip.visibility = View.GONE
            }
        }
    }

    private fun showBiometricPrompt() {
        // Check if biometric is available
        val biometricManager = BiometricManager.from(this)
        when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG or
            BiometricManager.Authenticators.BIOMETRIC_WEAK
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> {
                biometricPrompt.authenticate(promptInfo)
            }
            BiometricManager.BIOMETRIC_ERROR_NO_HARDWARE -> {
                Toast.makeText(this, "No biometric hardware available", Toast.LENGTH_LONG).show()
                if (authMode == MODE_SETUP) {
                    handleSkip()
                } else {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }
            BiometricManager.BIOMETRIC_ERROR_HW_UNAVAILABLE -> {
                Toast.makeText(this, "Biometric hardware unavailable", Toast.LENGTH_LONG).show()
                if (authMode == MODE_SETUP) {
                    handleSkip()
                }
            }
            BiometricManager.BIOMETRIC_ERROR_NONE_ENROLLED -> {
                Toast.makeText(this, "No biometric credentials enrolled", Toast.LENGTH_LONG).show()
                if (authMode == MODE_SETUP) {
                    handleSkip()
                } else {
                    setResult(RESULT_CANCELED)
                    finish()
                }
            }
            else -> {
                Toast.makeText(this, "Biometric authentication not available", Toast.LENGTH_LONG).show()
                if (authMode == MODE_SETUP) {
                    handleSkip()
                }
            }
        }
    }

    private fun handleAuthenticationSuccess() {
        when (authMode) {
            MODE_SETUP -> {
                // Enable biometric for future logins
                HealthPayKeyboardApplication.setBiometricEnabled(true)
                Toast.makeText(this, "Biometric authentication enabled!", Toast.LENGTH_SHORT).show()
                setResult(RESULT_OK)
                finish()
            }
            MODE_LOGIN -> {
                // Mark as logged in
                HealthPayKeyboardApplication.setLoggedIn(true)
                setResult(RESULT_OK)
                finish()
            }
            MODE_TRANSACTION -> {
                // Return success for transaction confirmation
                setResult(RESULT_OK)
                finish()
            }
        }
    }

    private fun handleSkip() {
        when (authMode) {
            MODE_SETUP -> {
                // User skipped biometric setup - that's fine
                HealthPayKeyboardApplication.setBiometricEnabled(false)
                setResult(RESULT_OK)
                finish()
            }
            else -> {
                setResult(RESULT_CANCELED)
                finish()
            }
        }
    }

    override fun onBackPressed() {
        if (authMode == MODE_SETUP) {
            handleSkip()
        } else {
            setResult(RESULT_CANCELED)
            super.onBackPressed()
        }
    }
}
