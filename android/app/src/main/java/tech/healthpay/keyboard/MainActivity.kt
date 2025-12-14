package tech.healthpay.keyboard

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat

/**
 * Main Activity - Keyboard Setup & Login
 * 
 * NO HILT - Uses Application singleton for dependencies
 */
class MainActivity : AppCompatActivity() {

    // Dependencies from Application singleton
    private val authManager get() = HealthPayKeyboardApplication.authManager
    private val tokenManager get() = HealthPayKeyboardApplication.tokenManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        setupViews()
        handleDeepLinkIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleDeepLinkIntent(it) }
    }

    override fun onResume() {
        super.onResume()
        updateUI()
    }

    private fun setupViews() {
        // Keyboard setup buttons
        findViewById<Button>(R.id.btn_enable_keyboard)?.setOnClickListener {
            openInputMethodSettings()
        }

        findViewById<Button>(R.id.btn_select_keyboard)?.setOnClickListener {
            showInputMethodPicker()
        }

        // Login/Logout button
        findViewById<Button>(R.id.btn_login)?.setOnClickListener {
            handleLoginLogout()
        }

        // Settings button
        findViewById<Button>(R.id.btn_settings)?.setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
    }

    private fun updateUI() {
        updateKeyboardStatus()
        updateLoginStatus()
    }

    private fun updateKeyboardStatus() {
        val isEnabled = isKeyboardEnabled()
        val isSelected = isKeyboardSelected()

        val tvStatus = findViewById<TextView>(R.id.tv_keyboard_status)
        val btnEnable = findViewById<Button>(R.id.btn_enable_keyboard)
        val btnSelect = findViewById<Button>(R.id.btn_select_keyboard)

        when {
            !isEnabled -> {
                tvStatus?.text = getString(R.string.keyboard_not_enabled)
                tvStatus?.setTextColor(ContextCompat.getColor(this, R.color.status_error))
                btnEnable?.visibility = View.VISIBLE
                btnSelect?.visibility = View.GONE
            }
            !isSelected -> {
                tvStatus?.text = getString(R.string.keyboard_enabled_not_selected)
                tvStatus?.setTextColor(ContextCompat.getColor(this, R.color.status_warning))
                btnEnable?.visibility = View.GONE
                btnSelect?.visibility = View.VISIBLE
            }
            else -> {
                tvStatus?.text = getString(R.string.keyboard_ready)
                tvStatus?.setTextColor(ContextCompat.getColor(this, R.color.status_success))
                btnEnable?.visibility = View.GONE
                btnSelect?.visibility = View.GONE
            }
        }
    }

    private fun updateLoginStatus() {
        val isLoggedIn = HealthPayKeyboardApplication.isLoggedIn()

        val tvStatus = findViewById<TextView>(R.id.tv_login_status)
        val btnLogin = findViewById<Button>(R.id.btn_login)
        val btnSettings = findViewById<Button>(R.id.btn_settings)

        if (isLoggedIn) {
            tvStatus?.text = getString(R.string.logged_in)
            tvStatus?.setTextColor(ContextCompat.getColor(this, R.color.status_success))
            btnLogin?.text = getString(R.string.logout)
        } else {
            tvStatus?.text = getString(R.string.not_logged_in)
            tvStatus?.setTextColor(ContextCompat.getColor(this, R.color.status_warning))
            btnLogin?.text = getString(R.string.login)
        }

        btnSettings?.isEnabled = isLoggedIn
        btnSettings?.alpha = if (isLoggedIn) 1.0f else 0.5f
    }

    private fun handleLoginLogout() {
        if (HealthPayKeyboardApplication.isLoggedIn()) {
            HealthPayKeyboardApplication.logout()
            Toast.makeText(this, R.string.logged_out_success, Toast.LENGTH_SHORT).show()
            updateLoginStatus()
        } else {
            // Start biometric auth for login
            startActivityForResult(
                Intent(this, BiometricAuthActivity::class.java).apply {
                    putExtra(BiometricAuthActivity.EXTRA_AUTH_MODE, BiometricAuthActivity.MODE_LOGIN)
                },
                REQUEST_LOGIN
            )
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_LOGIN) {
            if (resultCode == RESULT_OK) {
                Toast.makeText(this, R.string.login_success, Toast.LENGTH_SHORT).show()
                updateLoginStatus()
            }
        }
    }

    private fun handleDeepLinkIntent(intent: Intent) {
        val action = intent.getStringExtra(DeepLinkActivity.EXTRA_ACTION)
        when (action) {
            DeepLinkActivity.ACTION_PAYMENT -> {
                // Handle payment deep link
                val amount = intent.getDoubleExtra(DeepLinkActivity.EXTRA_AMOUNT, 0.0)
                val recipient = intent.getStringExtra(DeepLinkActivity.EXTRA_RECIPIENT)
                // TODO: Show payment confirmation dialog
            }
            DeepLinkActivity.ACTION_QR_SCAN -> {
                // Launch QR scanner
                startActivity(Intent(this, QRScannerActivity::class.java))
            }
        }
    }

    private fun openInputMethodSettings() {
        startActivity(Intent(Settings.ACTION_INPUT_METHOD_SETTINGS))
    }

    private fun showInputMethodPicker() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.showInputMethodPicker()
    }

    private fun isKeyboardEnabled(): Boolean {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        val enabledMethods = imm.enabledInputMethodList
        
        val packageName = packageName
        return enabledMethods.any { it.packageName == packageName }
    }

    private fun isKeyboardSelected(): Boolean {
        val defaultIme = Settings.Secure.getString(
            contentResolver,
            Settings.Secure.DEFAULT_INPUT_METHOD
        ) ?: return false

        return defaultIme.contains(packageName)
    }

    companion object {
        private const val REQUEST_LOGIN = 1001
    }
}
