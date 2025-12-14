package tech.healthpay.keyboard.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import tech.healthpay.keyboard.HealthPayKeyboardApplication
import tech.healthpay.keyboard.R

/**
 * MainActivity - Entry point for the HealthPay Keyboard app.
 * 
 * Handles:
 * - Login/Logout flow (properly routes to LoginActivity for first-time login)
 * - Keyboard setup instructions
 * - Keyboard enablement status
 */
class MainActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "MainActivity"
        private const val REQUEST_LOGIN = 1001
        private const val REQUEST_BIOMETRIC_AUTH = 1002
    }

    // UI Elements
    private lateinit var cardLoginStatus: CardView
    private lateinit var ivLoginStatus: ImageView
    private lateinit var tvLoginStatus: TextView
    private lateinit var tvLoginDescription: TextView
    private lateinit var btnLoginLogout: Button

    private lateinit var cardKeyboardStatus: CardView
    private lateinit var ivKeyboardEnabled: ImageView
    private lateinit var tvKeyboardStatus: TextView
    private lateinit var tvKeyboardDescription: TextView
    private lateinit var btnEnableKeyboard: Button
    private lateinit var btnSelectKeyboard: Button

    private lateinit var layoutSetupSteps: LinearLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        initViews()
        setupClickListeners()
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun initViews() {
        // Login Status Card
        cardLoginStatus = findViewById(R.id.card_login_status)
        ivLoginStatus = findViewById(R.id.iv_login_status)
        tvLoginStatus = findViewById(R.id.tv_login_status)
        tvLoginDescription = findViewById(R.id.tv_login_description)
        btnLoginLogout = findViewById(R.id.btn_login_logout)

        // Keyboard Status Card
        cardKeyboardStatus = findViewById(R.id.card_keyboard_status)
        ivKeyboardEnabled = findViewById(R.id.iv_keyboard_enabled)
        tvKeyboardStatus = findViewById(R.id.tv_keyboard_status)
        tvKeyboardDescription = findViewById(R.id.tv_keyboard_description)
        btnEnableKeyboard = findViewById(R.id.btn_enable_keyboard)
        btnSelectKeyboard = findViewById(R.id.btn_select_keyboard)

        // Setup Steps
        layoutSetupSteps = findViewById(R.id.layout_setup_steps)
    }

    private fun setupClickListeners() {
        btnLoginLogout.setOnClickListener {
            handleLoginLogout()
        }

        btnEnableKeyboard.setOnClickListener {
            openKeyboardSettings()
        }

        btnSelectKeyboard.setOnClickListener {
            showInputMethodPicker()
        }
    }

    private fun updateUI() {
        val isLoggedIn = HealthPayKeyboardApplication.isLoggedIn()
        val isKeyboardEnabled = isKeyboardEnabled()
        val isKeyboardSelected = isKeyboardSelected()

        // Update Login Status Card
        if (isLoggedIn) {
            ivLoginStatus.setImageResource(R.drawable.ic_check_circle)
            ivLoginStatus.setColorFilter(getColor(R.color.success))
            tvLoginStatus.text = getString(R.string.logged_in)
            tvLoginDescription.text = getString(R.string.logged_in_description, 
                HealthPayKeyboardApplication.getPhoneNumber() ?: "")
            btnLoginLogout.text = getString(R.string.logout)
            btnLoginLogout.setBackgroundResource(R.drawable.bg_button_secondary)
        } else {
            ivLoginStatus.setImageResource(R.drawable.ic_account_circle)
            ivLoginStatus.setColorFilter(getColor(R.color.text_secondary))
            tvLoginStatus.text = getString(R.string.not_logged_in)
            tvLoginDescription.text = getString(R.string.not_logged_in_description)
            btnLoginLogout.text = getString(R.string.login)
            btnLoginLogout.setBackgroundResource(R.drawable.bg_button_primary)
        }

        // Update Keyboard Status Card
        if (isKeyboardEnabled && isKeyboardSelected) {
            ivKeyboardEnabled.setImageResource(R.drawable.ic_keyboard)
            ivKeyboardEnabled.setColorFilter(getColor(R.color.success))
            tvKeyboardStatus.text = getString(R.string.keyboard_ready)
            tvKeyboardDescription.text = getString(R.string.keyboard_ready_description)
            btnEnableKeyboard.visibility = View.GONE
            btnSelectKeyboard.visibility = View.GONE
            layoutSetupSteps.visibility = View.GONE
        } else if (isKeyboardEnabled) {
            ivKeyboardEnabled.setImageResource(R.drawable.ic_keyboard)
            ivKeyboardEnabled.setColorFilter(getColor(R.color.warning))
            tvKeyboardStatus.text = getString(R.string.keyboard_enabled)
            tvKeyboardDescription.text = getString(R.string.keyboard_select_description)
            btnEnableKeyboard.visibility = View.GONE
            btnSelectKeyboard.visibility = View.VISIBLE
            layoutSetupSteps.visibility = View.VISIBLE
        } else {
            ivKeyboardEnabled.setImageResource(R.drawable.ic_keyboard)
            ivKeyboardEnabled.setColorFilter(getColor(R.color.text_secondary))
            tvKeyboardStatus.text = getString(R.string.keyboard_not_enabled)
            tvKeyboardDescription.text = getString(R.string.keyboard_enable_description)
            btnEnableKeyboard.visibility = View.VISIBLE
            btnSelectKeyboard.visibility = View.GONE
            layoutSetupSteps.visibility = View.VISIBLE
        }
    }

    /**
     * FIXED: Properly handle login/logout flow.
     * 
     * For LOGIN (first time):
     *   1. Launch LoginActivity for mobile number + OTP verification
     *   2. After successful OTP verification, LoginActivity handles biometric setup
     * 
     * For subsequent logins (already authenticated once):
     *   1. Launch BiometricAuthActivity for quick biometric re-authentication
     * 
     * For LOGOUT:
     *   1. Show confirmation dialog
     *   2. Clear tokens and session
     */
    private fun handleLoginLogout() {
        if (HealthPayKeyboardApplication.isLoggedIn()) {
            // User is logged in - show logout confirmation
            showLogoutConfirmation()
        } else {
            // User is not logged in - check if this is first time or re-authentication
            if (HealthPayKeyboardApplication.hasCompletedInitialLogin()) {
                // User has previously logged in, use biometric for quick re-auth
                startActivityForResult(
                    Intent(this, BiometricAuthActivity::class.java).apply {
                        putExtra(BiometricAuthActivity.EXTRA_AUTH_MODE, BiometricAuthActivity.MODE_LOGIN)
                    },
                    REQUEST_BIOMETRIC_AUTH
                )
            } else {
                // FIRST TIME LOGIN - Launch LoginActivity for phone + OTP flow
                startActivityForResult(
                    Intent(this, LoginActivity::class.java),
                    REQUEST_LOGIN
                )
            }
        }
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(R.string.logout_confirmation_title)
            .setMessage(R.string.logout_confirmation_message)
            .setPositiveButton(R.string.logout) { _, _ ->
                performLogout()
            }
            .setNegativeButton(R.string.cancel, null)
            .show()
    }

    private fun performLogout() {
        HealthPayKeyboardApplication.logout()
        Toast.makeText(this, R.string.logged_out_successfully, Toast.LENGTH_SHORT).show()
        updateUI()
    }

    private fun isKeyboardEnabled(): Boolean {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledInputMethods = imm.enabledInputMethodList
        return enabledInputMethods.any { 
            it.packageName == packageName 
        }
    }

    private fun isKeyboardSelected(): Boolean {
        val currentInputMethod = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        )
        return currentInputMethod?.contains(packageName) == true
    }

    private fun openKeyboardSettings() {
        try {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        } catch (e: Exception) {
            Toast.makeText(
                this, 
                R.string.error_opening_settings, 
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun showInputMethodPicker() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showInputMethodPicker()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            REQUEST_LOGIN -> {
                when (resultCode) {
                    LoginActivity.RESULT_LOGIN_SUCCESS -> {
                        Toast.makeText(this, R.string.login_successful, Toast.LENGTH_SHORT).show()
                        updateUI()
                    }
                    LoginActivity.RESULT_LOGIN_CANCELLED -> {
                        // User cancelled login - do nothing
                    }
                }
            }
            REQUEST_BIOMETRIC_AUTH -> {
                if (resultCode == RESULT_OK) {
                    Toast.makeText(this, R.string.authentication_successful, Toast.LENGTH_SHORT).show()
                    updateUI()
                }
            }
        }
    }
}
