package tech.healthpay.keyboard.ui

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import tech.healthpay.keyboard.R

/**
 * SettingsActivity for keyboard and app settings.
 */
class SettingsActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        supportActionBar?.apply {
            setDisplayHomeAsUpEnabled(true)
            title = "Settings"
        }
    }

    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
