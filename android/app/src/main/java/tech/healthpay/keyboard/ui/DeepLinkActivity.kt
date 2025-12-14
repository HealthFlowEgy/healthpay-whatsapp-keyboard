package tech.healthpay.keyboard.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import tech.healthpay.keyboard.HealthPayKeyboardApplication

/**
 * DeepLinkActivity handles payment deep links.
 * 
 * Supported formats:
 * - https://pay.healthpay.tech/send?phone=xxx&amount=xxx
 * - healthpay://pay?phone=xxx&amount=xxx
 */
class DeepLinkActivity : AppCompatActivity() {

    companion object {
        private const val TAG = "DeepLinkActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        val uri = intent.data
        if (uri == null) {
            Log.w(TAG, "No URI in intent")
            finish()
            return
        }

        Log.d(TAG, "Handling deep link: $uri")

        // Check if user is logged in
        if (!HealthPayKeyboardApplication.isLoggedIn()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_LONG).show()
            // Launch main activity for login
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Parse the deep link
        when (uri.host) {
            "pay.healthpay.tech", "pay" -> handlePaymentLink(uri)
            else -> {
                Log.w(TAG, "Unknown deep link host: ${uri.host}")
                finish()
            }
        }
    }

    private fun handlePaymentLink(uri: Uri) {
        val phone = uri.getQueryParameter("phone")
        val amount = uri.getQueryParameter("amount")
        val note = uri.getQueryParameter("note")

        if (phone.isNullOrBlank()) {
            Toast.makeText(this, "Invalid payment link", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        // TODO: Launch payment confirmation dialog
        Log.d(TAG, "Payment link - Phone: $phone, Amount: $amount, Note: $note")
        Toast.makeText(this, "Payment to $phone for $amount", Toast.LENGTH_LONG).show()
        
        finish()
    }
}
