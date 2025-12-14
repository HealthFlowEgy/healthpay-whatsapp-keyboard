package tech.healthpay.keyboard.ui

import android.os.Bundle
import android.widget.Switch
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
import tech.healthpay.keyboard.HealthPayKeyboardApplication
import tech.healthpay.keyboard.R

/**
 * Settings Activity - User preferences for the keyboard
 */
class SettingsActivity : AppCompatActivity() {

    private lateinit var biometricSwitch: SwitchCompat
    private lateinit var biometricStatus: TextView
    private lateinit var versionText: TextView

    private val authManager by lazy { HealthPayKeyboardApplication.authenticationManager }
    private val biometricHelper by lazy { HealthPayKeyboardApplication.biometricHelper }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.settings_title)
        
        initViews()
        setupListeners()
        updateBiometricStatus()
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }

    private fun initViews() {
        biometricSwitch = findViewById(R.id.switch_biometric)
        biometricStatus = findViewById(R.id.biometric_status)
        versionText = findViewById(R.id.version_text)

        try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            versionText.text = getString(R.string.version_format, packageInfo.versionName)
        } catch (e: Exception) {
            versionText.text = getString(R.string.version_format, "1.2.1")
        }
    }

    private fun setupListeners() {
        biometricSwitch.setOnCheckedChangeListener { _, isChecked ->
            if (biometricHelper.canAuthenticate()) {
                authManager.setBiometricEnabled(isChecked)
            } else {
                biometricSwitch.isChecked = false
            }
            updateBiometricStatus()
        }
    }

    private fun updateBiometricStatus() {
        val canAuthenticate = biometricHelper.canAuthenticate()
        biometricSwitch.isEnabled = canAuthenticate
        biometricSwitch.isChecked = authManager.isBiometricEnabled()

        biometricStatus.text = when (biometricHelper.getBiometricStatus()) {
            tech.healthpay.keyboard.security.BiometricHelper.BiometricStatus.AVAILABLE -> 
                getString(R.string.biometric_available)
            tech.healthpay.keyboard.security.BiometricHelper.BiometricStatus.NOT_ENROLLED -> 
                getString(R.string.biometric_not_enrolled)
            tech.healthpay.keyboard.security.BiometricHelper.BiometricStatus.NO_HARDWARE -> 
                getString(R.string.biometric_no_hardware)
            else -> getString(R.string.biometric_unavailable)
        }
    }
}
