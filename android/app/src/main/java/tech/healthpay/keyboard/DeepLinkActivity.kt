package tech.healthpay.keyboard

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

/**
 * Deep Link Activity
 * 
 * Handles incoming deep links for payments and other actions.
 * 
 * NO HILT - Uses Application singleton for dependencies
 */
class DeepLinkActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // No layout - just processes deep link and routes
        handleIntent(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        intent?.let { handleIntent(it) }
    }

    private fun handleIntent(intent: Intent) {
        val data = intent.data

        if (data == null) {
            Log.w(TAG, "Deep link with no data")
            routeToMain()
            return
        }

        Log.d(TAG, "Deep link: $data")

        when {
            isPaymentLink(data) -> handlePaymentLink(data)
            isRequestLink(data) -> handleRequestLink(data)
            isQRLink(data) -> handleQRLink()
            isWalletLink(data) -> handleWalletLink(data)
            else -> {
                Log.w(TAG, "Unknown deep link: $data")
                routeToMain()
            }
        }
    }

    private fun isPaymentLink(uri: Uri): Boolean {
        return uri.host == "pay" ||
                uri.path?.startsWith("/pay") == true ||
                uri.getQueryParameter("action") == "pay"
    }

    private fun isRequestLink(uri: Uri): Boolean {
        return uri.host == "request" ||
                uri.path?.startsWith("/request") == true ||
                uri.getQueryParameter("action") == "request"
    }

    private fun isQRLink(uri: Uri): Boolean {
        return uri.host == "qr" ||
                uri.path?.startsWith("/qr") == true ||
                uri.getQueryParameter("action") == "qr"
    }

    private fun isWalletLink(uri: Uri): Boolean {
        return uri.host == "wallet" ||
                uri.path?.startsWith("/wallet") == true
    }

    private fun handlePaymentLink(uri: Uri) {
        if (!HealthPayKeyboardApplication.isLoggedIn()) {
            Toast.makeText(this, R.string.login_required_for_payment, Toast.LENGTH_SHORT).show()
            routeToMain()
            return
        }

        val amount = uri.getQueryParameter("amount")?.toDoubleOrNull()
        val recipient = uri.getQueryParameter("to")
            ?: uri.getQueryParameter("recipient")
        val currency = uri.getQueryParameter("currency") ?: "EGP"
        val note = uri.getQueryParameter("note")

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ACTION, ACTION_PAYMENT)
            amount?.let { putExtra(EXTRA_AMOUNT, it) }
            recipient?.let { putExtra(EXTRA_RECIPIENT, it) }
            putExtra(EXTRA_CURRENCY, currency)
            note?.let { putExtra(EXTRA_NOTE, it) }
        }
        startActivity(mainIntent)
        finish()
    }

    private fun handleRequestLink(uri: Uri) {
        if (!HealthPayKeyboardApplication.isLoggedIn()) {
            Toast.makeText(this, R.string.login_required_for_request, Toast.LENGTH_SHORT).show()
            routeToMain()
            return
        }

        val amount = uri.getQueryParameter("amount")?.toDoubleOrNull()
        val currency = uri.getQueryParameter("currency") ?: "EGP"
        val note = uri.getQueryParameter("note")

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ACTION, ACTION_REQUEST)
            amount?.let { putExtra(EXTRA_AMOUNT, it) }
            putExtra(EXTRA_CURRENCY, currency)
            note?.let { putExtra(EXTRA_NOTE, it) }
        }
        startActivity(mainIntent)
        finish()
    }

    private fun handleQRLink() {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ACTION, ACTION_QR_SCAN)
        }
        startActivity(mainIntent)
        finish()
    }

    private fun handleWalletLink(uri: Uri) {
        if (!HealthPayKeyboardApplication.isLoggedIn()) {
            Toast.makeText(this, R.string.login_required_for_wallet, Toast.LENGTH_SHORT).show()
            routeToMain()
            return
        }

        val view = uri.getQueryParameter("view") ?: "balance"

        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra(EXTRA_ACTION, ACTION_WALLET)
            putExtra(EXTRA_WALLET_VIEW, view)
        }
        startActivity(mainIntent)
        finish()
    }

    private fun routeToMain() {
        val mainIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        startActivity(mainIntent)
        finish()
    }

    companion object {
        private const val TAG = "DeepLinkActivity"

        const val EXTRA_ACTION = "deep_link_action"
        const val ACTION_PAYMENT = "payment"
        const val ACTION_REQUEST = "request"
        const val ACTION_QR_SCAN = "qr_scan"
        const val ACTION_WALLET = "wallet"

        const val EXTRA_AMOUNT = "amount"
        const val EXTRA_RECIPIENT = "recipient"
        const val EXTRA_CURRENCY = "currency"
        const val EXTRA_NOTE = "note"
        const val EXTRA_WALLET_VIEW = "wallet_view"
    }
}
