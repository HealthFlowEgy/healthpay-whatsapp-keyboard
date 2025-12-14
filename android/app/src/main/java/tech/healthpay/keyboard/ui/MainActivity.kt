package tech.healthpay.keyboard.ui

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import tech.healthpay.keyboard.HealthPayKeyboardApplication
import tech.healthpay.keyboard.R
import tech.healthpay.keyboard.api.ApiCallback
import tech.healthpay.keyboard.api.ApiError
import tech.healthpay.keyboard.api.WalletBalance

/**
 * Main Activity - Dashboard for HealthPay Keyboard app
 */
class MainActivity : AppCompatActivity() {

    private lateinit var welcomeText: TextView
    private lateinit var balanceText: TextView
    private lateinit var enableKeyboardButton: Button
    private lateinit var selectKeyboardButton: Button
    private lateinit var settingsButton: Button
    private lateinit var logoutButton: Button

    private val apiClient by lazy { HealthPayKeyboardApplication.apiClient }
    private val tokenManager by lazy { HealthPayKeyboardApplication.tokenManager }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Check if user is logged in
        if (!HealthPayKeyboardApplication.instance.isUserLoggedIn()) {
            navigateToLogin()
            return
        }
        
        setContentView(R.layout.activity_main)
        initViews()
        setupListeners()
        loadWalletBalance()
    }

    override fun onResume() {
        super.onResume()
        updateKeyboardStatus()
    }

    private fun initViews() {
        welcomeText = findViewById(R.id.welcome_text)
        balanceText = findViewById(R.id.balance_text)
        enableKeyboardButton = findViewById(R.id.btn_enable_keyboard)
        selectKeyboardButton = findViewById(R.id.btn_select_keyboard)
        settingsButton = findViewById(R.id.btn_settings)
        logoutButton = findViewById(R.id.btn_logout)

        tokenManager.getUserMobile()?.let { mobile ->
            welcomeText.text = getString(R.string.welcome_user, maskMobile(mobile))
        }
    }

    private fun setupListeners() {
        enableKeyboardButton.setOnClickListener {
            startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
        }

        selectKeyboardButton.setOnClickListener {
            val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
            imm.showInputMethodPicker()
        }

        settingsButton.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }

        logoutButton.setOnClickListener {
            HealthPayKeyboardApplication.instance.logout()
            navigateToLogin()
        }
    }

    private fun loadWalletBalance() {
        balanceText.text = getString(R.string.balance_loading)
        
        apiClient.getWalletBalance(object : ApiCallback<WalletBalance> {
            override fun onSuccess(response: WalletBalance) {
                runOnUiThread {
                    balanceText.text = getString(R.string.balance_format, response.currency, response.balance)
                }
            }

            override fun onError(error: ApiError) {
                runOnUiThread {
                    balanceText.text = getString(R.string.balance_error)
                }
            }
        })
    }

    private fun updateKeyboardStatus() {
        val isEnabled = isKeyboardEnabled()
        enableKeyboardButton.isEnabled = !isEnabled
        selectKeyboardButton.isEnabled = isEnabled
        
        if (isEnabled) {
            enableKeyboardButton.text = getString(R.string.keyboard_enabled)
        } else {
            enableKeyboardButton.text = getString(R.string.enable_keyboard)
        }
    }

    private fun isKeyboardEnabled(): Boolean {
        val enabledMethods = Settings.Secure.getString(contentResolver, Settings.Secure.ENABLED_INPUT_METHODS)
        return enabledMethods?.contains(packageName) == true
    }

    private fun maskMobile(mobile: String): String {
        return if (mobile.length >= 4) "****${mobile.takeLast(4)}" else mobile
    }

    private fun navigateToLogin() {
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        })
        finish()
    }
}
