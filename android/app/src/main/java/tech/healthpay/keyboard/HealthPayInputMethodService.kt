package tech.healthpay.keyboard

import android.inputmethodservice.InputMethodService
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

/**
 * HealthPay Input Method Service
 * 
 * Custom keyboard that provides standard text input plus HealthPay wallet functionality.
 * 
 * NO HILT - Uses Application singleton for dependencies
 */
class HealthPayInputMethodService : InputMethodService() {

    // Dependencies from Application singleton
    private val tokenManager get() = HealthPayKeyboardApplication.tokenManager
    private val apiClient get() = HealthPayKeyboardApplication.apiClient
    private val settingsRepository get() = HealthPayKeyboardApplication.keyboardSettingsRepository

    // Coroutine scope for async operations
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    // Views
    private var keyboardView: View? = null
    private var healthPayPanel: LinearLayout? = null
    private var balanceTextView: TextView? = null
    private var isPanelVisible = false

    override fun onCreate() {
        super.onCreate()
        Log.d(TAG, "HealthPayInputMethodService created")
    }

    override fun onCreateInputView(): View {
        Log.d(TAG, "Creating input view")
        
        keyboardView = layoutInflater.inflate(R.layout.keyboard_main, null)
        
        setupKeyboard()
        setupHealthPayPanel()
        
        return keyboardView!!
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        Log.d(TAG, "Input started, restarting=$restarting")
    }

    override fun onStartInputView(info: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(info, restarting)
        updateState()
    }

    override fun onFinishInput() {
        super.onFinishInput()
        Log.d(TAG, "Input finished")
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        keyboardView = null
        Log.d(TAG, "HealthPayInputMethodService destroyed")
    }

    private fun setupKeyboard() {
        // Setup HealthPay toggle button
        keyboardView?.findViewById<ImageButton>(R.id.btn_healthpay_toggle)?.setOnClickListener {
            toggleHealthPayPanel()
        }

        // Setup spacebar
        keyboardView?.findViewById<Button>(R.id.btn_space)?.setOnClickListener {
            commitText(" ")
        }

        // Setup enter key
        keyboardView?.findViewById<ImageButton>(R.id.btn_enter)?.setOnClickListener {
            sendDefaultEditorAction(true)
        }

        // Setup quick amount buttons
        keyboardView?.findViewById<Button>(R.id.btn_amount_10)?.setOnClickListener {
            commitText("10")
        }
        keyboardView?.findViewById<Button>(R.id.btn_amount_50)?.setOnClickListener {
            commitText("50")
        }
        keyboardView?.findViewById<Button>(R.id.btn_amount_100)?.setOnClickListener {
            commitText("100")
        }
        keyboardView?.findViewById<Button>(R.id.btn_amount_500)?.setOnClickListener {
            commitText("500")
        }
    }

    private fun setupHealthPayPanel() {
        healthPayPanel = keyboardView?.findViewById(R.id.healthpay_panel)
        balanceTextView = keyboardView?.findViewById(R.id.tv_balance_value)

        // Send money button
        keyboardView?.findViewById<LinearLayout>(R.id.btn_send)?.setOnClickListener {
            // TODO: Open send payment dialog
            Log.d(TAG, "Send money clicked")
        }

        // Request money button
        keyboardView?.findViewById<LinearLayout>(R.id.btn_request)?.setOnClickListener {
            // TODO: Open request payment dialog
            Log.d(TAG, "Request money clicked")
        }

        // Scan QR button
        keyboardView?.findViewById<LinearLayout>(R.id.btn_scan)?.setOnClickListener {
            // TODO: Open QR scanner
            Log.d(TAG, "Scan QR clicked")
        }

        // Share link button
        keyboardView?.findViewById<LinearLayout>(R.id.btn_share)?.setOnClickListener {
            // TODO: Generate and share payment link
            Log.d(TAG, "Share link clicked")
        }
    }

    private fun updateState() {
        val isLoggedIn = HealthPayKeyboardApplication.isLoggedIn()
        val showBalance = settingsRepository.isShowBalance()

        // Update balance display
        if (isLoggedIn && showBalance && isPanelVisible) {
            loadBalance()
        } else {
            balanceTextView?.text = "---"
        }
    }

    private fun toggleHealthPayPanel() {
        isPanelVisible = !isPanelVisible
        healthPayPanel?.visibility = if (isPanelVisible) View.VISIBLE else View.GONE

        if (isPanelVisible && HealthPayKeyboardApplication.isLoggedIn()) {
            loadBalance()
        }
    }

    private fun loadBalance() {
        if (!settingsRepository.isShowBalance()) {
            balanceTextView?.text = "•••••"
            return
        }

        serviceScope.launch {
            try {
                val response = apiClient.getWalletBalance()
                if (response.success && response.balance != null) {
                    val formatted = response.formattedBalance ?: "${response.currency} ${response.balance}"
                    balanceTextView?.text = formatted
                } else {
                    balanceTextView?.text = "---"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed to load balance", e)
                balanceTextView?.text = "---"
            }
        }
    }

    /**
     * Commit text to the current input connection
     */
    private fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
        
        // Play sound/vibration feedback if enabled
        if (settingsRepository.isSoundEnabled()) {
            // TODO: Play key click sound
        }
        if (settingsRepository.isVibrationEnabled()) {
            // TODO: Vibrate
        }
    }

    /**
     * Delete text before cursor
     */
    private fun deleteText() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    /**
     * Insert payment link into current input
     */
    fun insertPaymentLink(link: String) {
        currentInputConnection?.commitText(link, 1)
    }

    companion object {
        private const val TAG = "HealthPayKeyboard"
    }
}

/**
 * Alias for backward compatibility
 */
typealias HealthPayKeyboardService = HealthPayInputMethodService
