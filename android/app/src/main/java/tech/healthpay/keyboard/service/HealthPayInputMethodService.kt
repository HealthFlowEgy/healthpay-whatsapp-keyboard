package tech.healthpay.keyboard.service

import android.content.Intent
import android.inputmethodservice.InputMethodService
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import tech.healthpay.keyboard.HealthPayKeyboardApplication
import tech.healthpay.keyboard.R
import tech.healthpay.keyboard.ui.BiometricAuthActivity
import tech.healthpay.keyboard.ui.QRScannerActivity

/**
 * HealthPayInputMethodService - The core keyboard service.
 * 
 * Provides:
 * - Full QWERTY keyboard
 * - Numbers/Symbols keyboard
 * - HealthPay wallet panel with payment features
 * - Balance display and quick actions
 */
class HealthPayInputMethodService : InputMethodService() {

    companion object {
        private const val TAG = "HealthPayIME"
    }

    private var keyboardView: View? = null
    
    // Keyboard state
    private var isShiftActive = false
    private var isCapsLock = false
    private var isNumbersMode = false
    private var isHealthPayPanelVisible = false

    // UI Elements
    private var healthPayPanel: LinearLayout? = null
    private var keyboardContainer: LinearLayout? = null
    private var numbersKeyboardContainer: LinearLayout? = null
    private var tvBalance: TextView? = null
    private var btnHealthPayToggle: ImageButton? = null

    // Coroutine scope for async operations
    private val serviceScope = CoroutineScope(Dispatchers.Main + Job())

    // Dependencies
    private val apiClient by lazy { HealthPayKeyboardApplication.apiClient }
    private val tokenManager by lazy { HealthPayKeyboardApplication.tokenManager }

    // Handler for shift double-tap detection (caps lock)
    private val handler = Handler(Looper.getMainLooper())
    private var lastShiftTapTime = 0L

    override fun onCreateInputView(): View? {
        Log.d(TAG, "onCreateInputView called")
        
        keyboardView = layoutInflater.inflate(R.layout.keyboard_main, null)
        
        if (keyboardView == null) {
            Log.e(TAG, "Failed to inflate keyboard_main.xml")
            return null
        }

        initViews()
        setupQwertyKeyboard()
        setupNumbersKeyboard()
        setupHealthPayPanel()
        setupSuggestionsBar()

        return keyboardView
    }

    private fun initViews() {
        keyboardView?.let { view ->
            healthPayPanel = view.findViewById(R.id.healthpay_panel)
            keyboardContainer = view.findViewById(R.id.keyboard_container)
            numbersKeyboardContainer = view.findViewById(R.id.numbers_keyboard_container)
            tvBalance = view.findViewById(R.id.tv_balance)
            btnHealthPayToggle = view.findViewById(R.id.btn_healthpay_toggle)
        }
    }

    // ==================== QWERTY KEYBOARD SETUP ====================

    private fun setupQwertyKeyboard() {
        val view = keyboardView ?: return

        // Row 1: Q W E R T Y U I O P
        setupCharKey(view, R.id.btn_q, 'q')
        setupCharKey(view, R.id.btn_w, 'w')
        setupCharKey(view, R.id.btn_e, 'e')
        setupCharKey(view, R.id.btn_r, 'r')
        setupCharKey(view, R.id.btn_t, 't')
        setupCharKey(view, R.id.btn_y, 'y')
        setupCharKey(view, R.id.btn_u, 'u')
        setupCharKey(view, R.id.btn_i, 'i')
        setupCharKey(view, R.id.btn_o, 'o')
        setupCharKey(view, R.id.btn_p, 'p')

        // Row 2: A S D F G H J K L
        setupCharKey(view, R.id.btn_a, 'a')
        setupCharKey(view, R.id.btn_s, 's')
        setupCharKey(view, R.id.btn_d, 'd')
        setupCharKey(view, R.id.btn_f, 'f')
        setupCharKey(view, R.id.btn_g, 'g')
        setupCharKey(view, R.id.btn_h, 'h')
        setupCharKey(view, R.id.btn_j, 'j')
        setupCharKey(view, R.id.btn_k, 'k')
        setupCharKey(view, R.id.btn_l, 'l')

        // Row 3: Shift Z X C V B N M Backspace
        setupCharKey(view, R.id.btn_z, 'z')
        setupCharKey(view, R.id.btn_x, 'x')
        setupCharKey(view, R.id.btn_c, 'c')
        setupCharKey(view, R.id.btn_v, 'v')
        setupCharKey(view, R.id.btn_b, 'b')
        setupCharKey(view, R.id.btn_n, 'n')
        setupCharKey(view, R.id.btn_m, 'm')

        // Shift key
        view.findViewById<ImageButton>(R.id.btn_shift)?.setOnClickListener {
            handleShiftKey()
        }

        // Backspace key
        view.findViewById<ImageButton>(R.id.btn_backspace)?.apply {
            setOnClickListener { handleBackspace() }
            setOnLongClickListener { 
                handleBackspaceLongPress()
                true
            }
        }

        // Row 4: 123 Emoji Space . Enter
        view.findViewById<Button>(R.id.btn_numbers)?.setOnClickListener {
            toggleNumbersMode(true)
        }

        view.findViewById<ImageButton>(R.id.btn_emoji)?.setOnClickListener {
            // TODO: Show emoji picker
            Toast.makeText(this, "Emoji picker coming soon", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<Button>(R.id.btn_comma)?.setOnClickListener {
            inputChar(',')
        }

        view.findViewById<Button>(R.id.btn_space)?.setOnClickListener {
            inputChar(' ')
        }

        view.findViewById<Button>(R.id.btn_period)?.setOnClickListener {
            inputChar('.')
        }

        view.findViewById<ImageButton>(R.id.btn_enter)?.setOnClickListener {
            handleEnter()
        }
    }

    // ==================== NUMBERS KEYBOARD SETUP ====================

    private fun setupNumbersKeyboard() {
        val view = keyboardView ?: return

        // Row 1: Numbers
        setupSymbolKey(view, R.id.btn_1, "1")
        setupSymbolKey(view, R.id.btn_2, "2")
        setupSymbolKey(view, R.id.btn_3, "3")
        setupSymbolKey(view, R.id.btn_4, "4")
        setupSymbolKey(view, R.id.btn_5, "5")
        setupSymbolKey(view, R.id.btn_6, "6")
        setupSymbolKey(view, R.id.btn_7, "7")
        setupSymbolKey(view, R.id.btn_8, "8")
        setupSymbolKey(view, R.id.btn_9, "9")
        setupSymbolKey(view, R.id.btn_0, "0")

        // Row 2: Symbols
        setupSymbolKey(view, R.id.btn_at, "@")
        setupSymbolKey(view, R.id.btn_hash, "#")
        setupSymbolKey(view, R.id.btn_dollar, "$")
        setupSymbolKey(view, R.id.btn_underscore, "_")
        setupSymbolKey(view, R.id.btn_ampersand, "&")
        setupSymbolKey(view, R.id.btn_minus, "-")
        setupSymbolKey(view, R.id.btn_plus, "+")
        setupSymbolKey(view, R.id.btn_open_paren, "(")
        setupSymbolKey(view, R.id.btn_close_paren, ")")
        setupSymbolKey(view, R.id.btn_slash, "/")

        // Row 3: More Symbols
        setupSymbolKey(view, R.id.btn_asterisk, "*")
        setupSymbolKey(view, R.id.btn_quote_double, "\"")
        setupSymbolKey(view, R.id.btn_quote_single, "'")
        setupSymbolKey(view, R.id.btn_colon, ":")
        setupSymbolKey(view, R.id.btn_semicolon, ";")
        setupSymbolKey(view, R.id.btn_exclamation, "!")
        setupSymbolKey(view, R.id.btn_question, "?")

        view.findViewById<Button>(R.id.btn_symbols)?.setOnClickListener {
            // TODO: Show more symbols
        }

        view.findViewById<ImageButton>(R.id.btn_backspace_num)?.apply {
            setOnClickListener { handleBackspace() }
            setOnLongClickListener {
                handleBackspaceLongPress()
                true
            }
        }

        // Row 4: ABC toggle, space, enter
        view.findViewById<Button>(R.id.btn_abc)?.setOnClickListener {
            toggleNumbersMode(false)
        }

        view.findViewById<ImageButton>(R.id.btn_emoji_num)?.setOnClickListener {
            Toast.makeText(this, "Emoji picker coming soon", Toast.LENGTH_SHORT).show()
        }

        view.findViewById<Button>(R.id.btn_comma_num)?.setOnClickListener {
            inputChar(',')
        }

        view.findViewById<Button>(R.id.btn_space_num)?.setOnClickListener {
            inputChar(' ')
        }

        view.findViewById<Button>(R.id.btn_period_num)?.setOnClickListener {
            inputChar('.')
        }

        view.findViewById<ImageButton>(R.id.btn_enter_num)?.setOnClickListener {
            handleEnter()
        }
    }

    // ==================== HEALTHPAY PANEL SETUP ====================

    private fun setupHealthPayPanel() {
        val view = keyboardView ?: return

        // Refresh balance button
        view.findViewById<ImageButton>(R.id.btn_refresh_balance)?.setOnClickListener {
            refreshBalance()
        }

        // Quick actions
        view.findViewById<LinearLayout>(R.id.btn_send_money)?.setOnClickListener {
            handleSendMoney()
        }

        view.findViewById<LinearLayout>(R.id.btn_request_money)?.setOnClickListener {
            handleRequestMoney()
        }

        view.findViewById<LinearLayout>(R.id.btn_qr_code)?.setOnClickListener {
            handleQRCode()
        }

        view.findViewById<LinearLayout>(R.id.btn_history)?.setOnClickListener {
            handleHistory()
        }

        // Quick amounts
        view.findViewById<Button>(R.id.btn_amount_50)?.setOnClickListener {
            insertQuickAmount(50)
        }

        view.findViewById<Button>(R.id.btn_amount_100)?.setOnClickListener {
            insertQuickAmount(100)
        }

        view.findViewById<Button>(R.id.btn_amount_200)?.setOnClickListener {
            insertQuickAmount(200)
        }

        view.findViewById<Button>(R.id.btn_amount_500)?.setOnClickListener {
            insertQuickAmount(500)
        }
    }

    private fun setupSuggestionsBar() {
        val view = keyboardView ?: return

        // HealthPay toggle button
        btnHealthPayToggle?.setOnClickListener {
            toggleHealthPayPanel()
        }

        // Keyboard settings
        view.findViewById<ImageButton>(R.id.btn_keyboard_settings)?.setOnClickListener {
            // TODO: Open keyboard settings
            Toast.makeText(this, "Keyboard settings", Toast.LENGTH_SHORT).show()
        }
    }

    // ==================== KEY HANDLERS ====================

    private fun setupCharKey(view: View, buttonId: Int, char: Char) {
        view.findViewById<Button>(buttonId)?.setOnClickListener {
            val charToInput = if (isShiftActive || isCapsLock) {
                char.uppercaseChar()
            } else {
                char
            }
            inputChar(charToInput)

            // Turn off shift after inputting a character (unless caps lock)
            if (isShiftActive && !isCapsLock) {
                isShiftActive = false
                updateShiftKeyUI()
            }
        }
    }

    private fun setupSymbolKey(view: View, buttonId: Int, symbol: String) {
        view.findViewById<Button>(buttonId)?.setOnClickListener {
            inputText(symbol)
        }
    }

    private fun inputChar(char: Char) {
        currentInputConnection?.commitText(char.toString(), 1)
    }

    private fun inputText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    private fun handleShiftKey() {
        val currentTime = System.currentTimeMillis()
        
        if (currentTime - lastShiftTapTime < 300) {
            // Double tap - toggle caps lock
            isCapsLock = !isCapsLock
            isShiftActive = isCapsLock
        } else {
            // Single tap - toggle shift
            if (isCapsLock) {
                isCapsLock = false
                isShiftActive = false
            } else {
                isShiftActive = !isShiftActive
            }
        }
        
        lastShiftTapTime = currentTime
        updateShiftKeyUI()
        updateKeyLabels()
    }

    private fun handleBackspace() {
        currentInputConnection?.deleteSurroundingText(1, 0)
    }

    private fun handleBackspaceLongPress() {
        // Delete word
        val selectedText = currentInputConnection?.getTextBeforeCursor(50, 0)
        if (selectedText != null) {
            val lastSpace = selectedText.lastIndexOf(' ')
            val deleteCount = if (lastSpace >= 0) {
                selectedText.length - lastSpace
            } else {
                selectedText.length
            }
            currentInputConnection?.deleteSurroundingText(deleteCount, 0)
        }
    }

    private fun handleEnter() {
        val editorInfo = currentInputEditorInfo
        val imeOptions = editorInfo?.imeOptions ?: 0
        
        when (imeOptions and EditorInfo.IME_MASK_ACTION) {
            EditorInfo.IME_ACTION_SEARCH -> {
                currentInputConnection?.performEditorAction(EditorInfo.IME_ACTION_SEARCH)
            }
            EditorInfo.IME_ACTION_GO -> {
                currentInputConnection?.performEditorAction(EditorInfo.IME_ACTION_GO)
            }
            EditorInfo.IME_ACTION_SEND -> {
                currentInputConnection?.performEditorAction(EditorInfo.IME_ACTION_SEND)
            }
            EditorInfo.IME_ACTION_NEXT -> {
                currentInputConnection?.performEditorAction(EditorInfo.IME_ACTION_NEXT)
            }
            EditorInfo.IME_ACTION_DONE -> {
                currentInputConnection?.performEditorAction(EditorInfo.IME_ACTION_DONE)
            }
            else -> {
                // Default: insert newline
                inputChar('\n')
            }
        }
    }

    // ==================== UI UPDATES ====================

    private fun toggleNumbersMode(showNumbers: Boolean) {
        isNumbersMode = showNumbers
        keyboardContainer?.visibility = if (showNumbers) View.GONE else View.VISIBLE
        numbersKeyboardContainer?.visibility = if (showNumbers) View.VISIBLE else View.GONE
    }

    private fun toggleHealthPayPanel() {
        isHealthPayPanelVisible = !isHealthPayPanelVisible
        healthPayPanel?.visibility = if (isHealthPayPanelVisible) View.VISIBLE else View.GONE
        
        // Update toggle button appearance
        btnHealthPayToggle?.setBackgroundResource(
            if (isHealthPayPanelVisible) R.drawable.bg_key_active else R.drawable.bg_key_special
        )

        // Refresh balance when panel is shown
        if (isHealthPayPanelVisible && HealthPayKeyboardApplication.isLoggedIn()) {
            refreshBalance()
        }
    }

    private fun updateShiftKeyUI() {
        val shiftButton = keyboardView?.findViewById<ImageButton>(R.id.btn_shift)
        
        when {
            isCapsLock -> {
                shiftButton?.setImageResource(R.drawable.ic_shift_locked)
                shiftButton?.setBackgroundResource(R.drawable.bg_key_active)
            }
            isShiftActive -> {
                shiftButton?.setImageResource(R.drawable.ic_shift_active)
                shiftButton?.setBackgroundResource(R.drawable.bg_key_active)
            }
            else -> {
                shiftButton?.setImageResource(R.drawable.ic_shift)
                shiftButton?.setBackgroundResource(R.drawable.bg_key_special)
            }
        }
    }

    private fun updateKeyLabels() {
        val view = keyboardView ?: return
        val uppercase = isShiftActive || isCapsLock

        // Update all character keys
        val charKeys = listOf(
            R.id.btn_q to "q", R.id.btn_w to "w", R.id.btn_e to "e", R.id.btn_r to "r",
            R.id.btn_t to "t", R.id.btn_y to "y", R.id.btn_u to "u", R.id.btn_i to "i",
            R.id.btn_o to "o", R.id.btn_p to "p", R.id.btn_a to "a", R.id.btn_s to "s",
            R.id.btn_d to "d", R.id.btn_f to "f", R.id.btn_g to "g", R.id.btn_h to "h",
            R.id.btn_j to "j", R.id.btn_k to "k", R.id.btn_l to "l", R.id.btn_z to "z",
            R.id.btn_x to "x", R.id.btn_c to "c", R.id.btn_v to "v", R.id.btn_b to "b",
            R.id.btn_n to "n", R.id.btn_m to "m"
        )

        charKeys.forEach { (id, char) ->
            view.findViewById<Button>(id)?.text = if (uppercase) char.uppercase() else char
        }
    }

    // ==================== HEALTHPAY ACTIONS ====================

    private fun refreshBalance() {
        if (!HealthPayKeyboardApplication.isLoggedIn()) {
            tvBalance?.text = "Not logged in"
            return
        }

        tvBalance?.text = "Loading..."
        
        serviceScope.launch {
            try {
                val result = withContext(Dispatchers.IO) {
                    apiClient.getWalletBalance()
                }

                result.fold(
                    onSuccess = { balance ->
                        tvBalance?.text = String.format("EGP %.2f", balance.available)
                    },
                    onFailure = { error ->
                        Log.e(TAG, "Failed to refresh balance", error)
                        tvBalance?.text = "Error"
                    }
                )
            } catch (e: Exception) {
                Log.e(TAG, "Error refreshing balance", e)
                tvBalance?.text = "Error"
            }
        }
    }

    private fun handleSendMoney() {
        if (!checkAuthentication()) return
        
        // TODO: Show send money dialog
        Toast.makeText(this, "Send money feature", Toast.LENGTH_SHORT).show()
    }

    private fun handleRequestMoney() {
        if (!checkAuthentication()) return
        
        // TODO: Show request money dialog
        Toast.makeText(this, "Request money feature", Toast.LENGTH_SHORT).show()
    }

    private fun handleQRCode() {
        if (!checkAuthentication()) return

        val intent = Intent(this, QRScannerActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun handleHistory() {
        if (!checkAuthentication()) return
        
        // TODO: Show transaction history
        Toast.makeText(this, "Transaction history", Toast.LENGTH_SHORT).show()
    }

    private fun insertQuickAmount(amount: Int) {
        inputText("EGP $amount")
    }

    private fun checkAuthentication(): Boolean {
        if (!HealthPayKeyboardApplication.isLoggedIn()) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return false
        }
        return true
    }

    // ==================== LIFECYCLE ====================

    override fun onStartInputView(editorInfo: EditorInfo?, restarting: Boolean) {
        super.onStartInputView(editorInfo, restarting)
        
        // Reset keyboard state
        isShiftActive = false
        isCapsLock = false
        isNumbersMode = false
        
        updateShiftKeyUI()
        updateKeyLabels()
        toggleNumbersMode(false)
        
        // Check if we should auto-capitalize
        if (editorInfo?.inputType?.and(android.text.InputType.TYPE_TEXT_FLAG_CAP_SENTENCES) != 0) {
            isShiftActive = true
            updateShiftKeyUI()
            updateKeyLabels()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(TAG, "HealthPayInputMethodService destroyed")
    }
}
