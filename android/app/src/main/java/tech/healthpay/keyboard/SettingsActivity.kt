package tech.healthpay.keyboard

import android.os.Bundle
import android.view.MenuItem
import android.widget.Switch
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar

/**
 * Settings Activity - Keyboard Preferences
 * 
 * NO HILT - Uses Application singleton for dependencies
 */
class SettingsActivity : AppCompatActivity() {

    private val settingsRepository get() = HealthPayKeyboardApplication.keyboardSettingsRepository
    private val biometricHelper get() = HealthPayKeyboardApplication.biometricHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setupToolbar()
        setupSwitches()
        loadSettings()
    }

    private fun setupToolbar() {
        findViewById<Toolbar>(R.id.toolbar)?.let { toolbar ->
            setSupportActionBar(toolbar)
            supportActionBar?.setDisplayHomeAsUpEnabled(true)
            supportActionBar?.setDisplayShowHomeEnabled(true)
            supportActionBar?.title = getString(R.string.settings)
        }
    }

    private fun setupSwitches() {
        // Biometric
        findViewById<Switch>(R.id.switch_biometric)?.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked && !biometricHelper.isBiometricAvailable()) {
                Toast.makeText(this, biometricHelper.getBiometricStatusMessage(), Toast.LENGTH_LONG).show()
                (buttonView as Switch).isChecked = false
                return@setOnCheckedChangeListener
            }
            settingsRepository.setBiometricEnabled(isChecked)
        }

        // Sound
        findViewById<Switch>(R.id.switch_sound)?.setOnCheckedChangeListener { _, isChecked ->
            settingsRepository.setSoundEnabled(isChecked)
        }

        // Vibration
        findViewById<Switch>(R.id.switch_vibration)?.setOnCheckedChangeListener { _, isChecked ->
            settingsRepository.setVibrationEnabled(isChecked)
        }

        // Show Balance
        findViewById<Switch>(R.id.switch_show_balance)?.setOnCheckedChangeListener { _, isChecked ->
            settingsRepository.setShowBalance(isChecked)
            if (!isChecked) {
                Toast.makeText(this, R.string.balance_hidden_warning, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun loadSettings() {
        val settings = settingsRepository.getSettings()

        findViewById<Switch>(R.id.switch_biometric)?.apply {
            isChecked = settings.biometricEnabled
            isEnabled = biometricHelper.isBiometricAvailable()
        }
        findViewById<Switch>(R.id.switch_sound)?.isChecked = settings.soundEnabled
        findViewById<Switch>(R.id.switch_vibration)?.isChecked = settings.vibrationEnabled
        findViewById<Switch>(R.id.switch_show_balance)?.isChecked = settings.showBalance

        // Version info
        try {
            val versionName = packageManager.getPackageInfo(packageName, 0).versionName
            findViewById<TextView>(R.id.tv_version)?.text = getString(R.string.version_format, versionName)
        } catch (e: Exception) {
            findViewById<TextView>(R.id.tv_version)?.text = getString(R.string.version_format, "1.0.0")
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}
