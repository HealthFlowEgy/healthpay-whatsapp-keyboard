package tech.healthpay.keyboard.ui

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.widget.FrameLayout
import android.widget.GridLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.PopupWindow
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.progressindicator.CircularProgressIndicator
import tech.healthpay.keyboard.R
import tech.healthpay.keyboard.model.Transaction
import tech.healthpay.keyboard.model.WalletBalance

/**
 * Custom Keyboard View for HealthPay
 * 
 * Provides:
 * - QWERTY keyboard with Arabic support
 * - Payment toolbar with quick actions
 * - Balance display
 * - Animations and haptic feedback
 */
class HealthPayKeyboardView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : LinearLayout(context, attrs, defStyleAttr) {

    // ==================== Interface ====================

    interface KeyboardListener {
        fun onKeyPressed(keyCode: Int, keyText: String)
        fun onPayButtonClicked()
        fun onRequestPaymentClicked()
        fun onBalanceClicked()
        fun onQRScanClicked()
        fun onSettingsClicked()
    }

    // ==================== Views ====================

    private lateinit var toolbarContainer: LinearLayout
    private lateinit var payButton: View
    private lateinit var requestButton: View
    private lateinit var balanceCard: CardView
    private lateinit var balanceText: TextView
    private lateinit var qrButton: View
    private lateinit var settingsButton: View
    private lateinit var keyboardContainer: GridLayout
    private lateinit var loadingIndicator: CircularProgressIndicator
    private lateinit var authPromptContainer: FrameLayout

    // ==================== State ====================

    private var listener: KeyboardListener? = null
    private var isShiftActive = false
    private var isCapsLock = false
    private var isArabic = false
    private var isWhatsAppMode = false
    private var isAuthenticated = false
    private var currentBalance: WalletBalance? = null

    // Keyboard layouts
    private val englishKeys = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("q", "w", "e", "r", "t", "y", "u", "i", "o", "p"),
        listOf("a", "s", "d", "f", "g", "h", "j", "k", "l"),
        listOf("⇧", "z", "x", "c", "v", "b", "n", "m", "⌫"),
        listOf("123", "🌐", "💳", " ", ".", "↵")
    )

    private val englishKeysShift = listOf(
        listOf("!", "@", "#", "$", "%", "^", "&", "*", "(", ")"),
        listOf("Q", "W", "E", "R", "T", "Y", "U", "I", "O", "P"),
        listOf("A", "S", "D", "F", "G", "H", "J", "K", "L"),
        listOf("⇧", "Z", "X", "C", "V", "B", "N", "M", "⌫"),
        listOf("123", "🌐", "💳", " ", ".", "↵")
    )

    private val arabicKeys = listOf(
        listOf("١", "٢", "٣", "٤", "٥", "٦", "٧", "٨", "٩", "٠"),
        listOf("ض", "ص", "ث", "ق", "ف", "غ", "ع", "ه", "خ", "ح"),
        listOf("ش", "س", "ي", "ب", "ل", "ا", "ت", "ن", "م"),
        listOf("⇧", "ئ", "ء", "ؤ", "ر", "لا", "ى", "ة", "⌫"),
        listOf("123", "🌐", "💳", " ", ".", "↵")
    )

    private val symbolKeys = listOf(
        listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "0"),
        listOf("@", "#", "$", "%", "&", "-", "+", "(", ")"),
        listOf("*", "\"", "'", ":", ";", "!", "?"),
        listOf("ABC", ",", ".", "/", "\\", "⌫"),
        listOf("ABC", "🌐", "💳", " ", ".", "↵")
    )

    // ==================== Initialization ====================

    init {
        orientation = VERTICAL
        setBackgroundColor(ContextCompat.getColor(context, R.color.keyboard_background))
        
        initToolbar()
        initKeyboard()
        initAuthPrompt()
        initLoadingIndicator()
    }

    private fun initToolbar() {
        toolbarContainer = LinearLayout(context).apply {
            orientation = HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(8.dp, 8.dp, 8.dp, 8.dp)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, 56.dp)
            setBackgroundColor(ContextCompat.getColor(context, R.color.toolbar_background))
        }

        // Pay Button
        payButton = createToolbarButton("💳", "Pay") {
            listener?.onPayButtonClicked()
        }

        // Request Button
        requestButton = createToolbarButton("💰", "Request") {
            listener?.onRequestPaymentClicked()
        }

        // Balance Card
        balanceCard = CardView(context).apply {
            layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f).apply {
                marginStart = 8.dp
                marginEnd = 8.dp
            }
            radius = 8.dp.toFloat()
            cardElevation = 2.dp.toFloat()
            setCardBackgroundColor(ContextCompat.getColor(context, R.color.balance_card_background))
            
            addView(LinearLayout(context).apply {
                orientation = HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(12.dp, 8.dp, 12.dp, 8.dp)

                addView(ImageView(context).apply {
                    setImageResource(R.drawable.ic_wallet)
                    layoutParams = LinearLayout.LayoutParams(20.dp, 20.dp)
                })

                balanceText = TextView(context).apply {
                    text = "Login"
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                    layoutParams = LinearLayout.LayoutParams(
                        LayoutParams.WRAP_CONTENT, 
                        LayoutParams.WRAP_CONTENT
                    ).apply { marginStart = 8.dp }
                }
                addView(balanceText)
            })
            
            setOnClickListener { listener?.onBalanceClicked() }
        }

        // QR Button
        qrButton = createToolbarButton("🔍", "QR") {
            listener?.onQRScanClicked()
        }

        // Settings Button
        settingsButton = createToolbarButton("⚙️", null) {
            listener?.onSettingsClicked()
        }

        toolbarContainer.addView(payButton)
        toolbarContainer.addView(requestButton)
        toolbarContainer.addView(balanceCard)
        toolbarContainer.addView(qrButton)
        toolbarContainer.addView(settingsButton)

        addView(toolbarContainer)
    }

    private fun createToolbarButton(
        emoji: String, 
        label: String?, 
        onClick: () -> Unit
    ): LinearLayout {
        return LinearLayout(context).apply {
            orientation = VERTICAL
            gravity = Gravity.CENTER
            setPadding(12.dp, 4.dp, 12.dp, 4.dp)
            
            background = createRippleDrawable()
            
            addView(TextView(context).apply {
                text = emoji
                textSize = 20f
            })
            
            label?.let {
                addView(TextView(context).apply {
                    text = it
                    textSize = 10f
                    setTextColor(ContextCompat.getColor(context, R.color.text_secondary))
                })
            }
            
            setOnClickListener { onClick() }
        }
    }

    private fun initKeyboard() {
        keyboardContainer = GridLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            setPadding(4.dp, 4.dp, 4.dp, 4.dp)
        }
        
        buildKeyboard(englishKeys)
        addView(keyboardContainer)
    }

    private fun buildKeyboard(layout: List<List<String>>) {
        keyboardContainer.removeAllViews()
        keyboardContainer.columnCount = 10

        layout.forEachIndexed { rowIndex, row ->
            row.forEachIndexed { colIndex, key ->
                val keyView = createKeyView(key, rowIndex, colIndex, row.size)
                keyboardContainer.addView(keyView)
            }
        }
    }

    private fun createKeyView(
        key: String, 
        row: Int, 
        col: Int, 
        rowSize: Int
    ): View {
        val isSpecialKey = key in listOf("⇧", "⌫", "↵", "123", "🌐", "💳", " ", "ABC")
        val keyWidth = when {
            key == " " -> 4 // Space bar spans 4 columns
            isSpecialKey && row == 3 -> 1
            else -> 1
        }
        
        return TextView(context).apply {
            text = key
            textSize = when {
                key == " " -> 14f
                isSpecialKey -> 16f
                else -> 18f
            }
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, 
                if (isSpecialKey) R.color.key_special_text else R.color.key_text
            ))
            
            background = createKeyBackground(isSpecialKey)
            
            layoutParams = GridLayout.LayoutParams().apply {
                width = 0
                height = 48.dp
                columnSpec = GridLayout.spec(GridLayout.UNDEFINED, keyWidth, 1f)
                setMargins(2.dp, 2.dp, 2.dp, 2.dp)
            }
            
            setOnTouchListener { v, event ->
                when (event.action) {
                    MotionEvent.ACTION_DOWN -> {
                        animateKeyPress(v, true)
                        showKeyPopup(v, key)
                    }
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                        animateKeyPress(v, false)
                        hideKeyPopup()
                        handleKeyPress(key)
                    }
                }
                true
            }
        }
    }

    private fun createKeyBackground(isSpecial: Boolean): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 8.dp.toFloat()
            setColor(ContextCompat.getColor(context, 
                if (isSpecial) R.color.key_special_background else R.color.key_background
            ))
        }
    }

    private fun createRippleDrawable(): GradientDrawable {
        return GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 8.dp.toFloat()
            setColor(ContextCompat.getColor(context, R.color.ripple_color))
        }
    }

    private var keyPopup: PopupWindow? = null

    private fun showKeyPopup(anchor: View, key: String) {
        if (key in listOf("⇧", "⌫", "↵", "123", "🌐", "💳", " ", "ABC")) return
        
        val popupView = TextView(context).apply {
            text = key.uppercase()
            textSize = 24f
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, R.color.key_text))
            setBackgroundResource(R.drawable.key_popup_background)
            setPadding(16.dp, 12.dp, 16.dp, 12.dp)
        }
        
        keyPopup = PopupWindow(popupView, LayoutParams.WRAP_CONTENT, LayoutParams.WRAP_CONTENT).apply {
            showAsDropDown(anchor, 0, -anchor.height * 2)
        }
    }

    private fun hideKeyPopup() {
        keyPopup?.dismiss()
        keyPopup = null
    }

    private fun animateKeyPress(view: View, pressed: Boolean) {
        val scale = if (pressed) 0.95f else 1.0f
        AnimatorSet().apply {
            playTogether(
                ObjectAnimator.ofFloat(view, View.SCALE_X, scale),
                ObjectAnimator.ofFloat(view, View.SCALE_Y, scale)
            )
            duration = 50
            start()
        }
    }

    private fun handleKeyPress(key: String) {
        val keyCode = when (key) {
            "⌫" -> -1 // DELETE
            "↵" -> -2 // ENTER
            " " -> -3 // SPACE
            "⇧" -> -4 // SHIFT
            "🌐" -> -5 // LANGUAGE
            "💳" -> -100 // PAY
            "123", "ABC" -> -6 // SYMBOLS
            else -> 0
        }
        
        when (keyCode) {
            -4 -> toggleShift()
            -5 -> switchLanguage()
            -6 -> toggleSymbols()
            -100 -> listener?.onPayButtonClicked()
            else -> {
                val textToCommit = if (isShiftActive && !isCapsLock) {
                    isShiftActive = false
                    updateKeyboardLayout()
                    key.uppercase()
                } else if (isShiftActive) {
                    key.uppercase()
                } else {
                    key
                }
                listener?.onKeyPressed(keyCode, if (keyCode == 0) textToCommit else key)
            }
        }
    }

    // ==================== Public Methods ====================

    fun setKeyboardListener(listener: KeyboardListener) {
        this.listener = listener
    }

    fun setWhatsAppMode(enabled: Boolean) {
        isWhatsAppMode = enabled
        updateToolbarVisibility()
    }

    fun setAuthenticationState(authenticated: Boolean) {
        isAuthenticated = authenticated
        updateToolbarState()
        authPromptContainer.isVisible = !authenticated && isWhatsAppMode
    }

    fun updatePaymentButtonVisibility(visible: Boolean) {
        payButton.isVisible = visible
        requestButton.isVisible = visible
    }

    fun updateBalance(balance: WalletBalance?) {
        currentBalance = balance
        balanceText.text = if (balance != null) {
            "EGP ${String.format("%,.0f", balance.available)}"
        } else {
            "Login"
        }
    }

    fun toggleShift() {
        when {
            !isShiftActive -> {
                isShiftActive = true
                isCapsLock = false
            }
            isShiftActive && !isCapsLock -> {
                isCapsLock = true
            }
            else -> {
                isShiftActive = false
                isCapsLock = false
            }
        }
        updateKeyboardLayout()
    }

    fun switchLanguage() {
        isArabic = !isArabic
        updateKeyboardLayout()
    }

    private fun toggleSymbols() {
        // Toggle between letters and symbols
        buildKeyboard(if (isArabic) arabicKeys else englishKeys)
    }

    private fun updateKeyboardLayout() {
        val layout = when {
            isArabic -> arabicKeys
            isShiftActive -> englishKeysShift
            else -> englishKeys
        }
        buildKeyboard(layout)
    }

    private fun updateToolbarVisibility() {
        toolbarContainer.isVisible = isWhatsAppMode || isAuthenticated
    }

    private fun updateToolbarState() {
        payButton.alpha = if (isAuthenticated) 1f else 0.5f
        requestButton.alpha = if (isAuthenticated) 1f else 0.5f
        qrButton.alpha = if (isAuthenticated) 1f else 0.5f
        
        payButton.isEnabled = isAuthenticated
        requestButton.isEnabled = isAuthenticated
        qrButton.isEnabled = isAuthenticated
    }

    private fun initAuthPrompt() {
        authPromptContainer = FrameLayout(context).apply {
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT)
            isVisible = false
            setBackgroundColor(ContextCompat.getColor(context, R.color.auth_prompt_background))
            
            addView(LinearLayout(context).apply {
                orientation = HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(16.dp, 12.dp, 16.dp, 12.dp)
                
                addView(TextView(context).apply {
                    text = "Login to use HealthPay payments"
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(context, R.color.text_primary))
                    layoutParams = LinearLayout.LayoutParams(0, LayoutParams.WRAP_CONTENT, 1f)
                })
                
                addView(TextView(context).apply {
                    text = "Login"
                    textSize = 14f
                    setTextColor(ContextCompat.getColor(context, R.color.accent_color))
                    setPadding(16.dp, 8.dp, 16.dp, 8.dp)
                    background = createRippleDrawable()
                    setOnClickListener {
                        // Show login
                    }
                })
            })
        }
        addView(authPromptContainer, 1) // Add after toolbar
    }

    private fun initLoadingIndicator() {
        loadingIndicator = CircularProgressIndicator(context).apply {
            isIndeterminate = true
            isVisible = false
        }
    }

    fun showLoading(show: Boolean) {
        loadingIndicator.isVisible = show
        keyboardContainer.alpha = if (show) 0.5f else 1f
    }

    fun showError(message: String) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    fun showSuccess(message: String) {
        Toast.makeText(context, "✅ $message", Toast.LENGTH_SHORT).show()
    }

    fun showAuthPrompt(onLoginClicked: () -> Unit) {
        authPromptContainer.isVisible = true
    }

    fun showBalanceCard(balance: WalletBalance) {
        // Show balance in a popup or card
    }

    fun showQROptions(onScan: () -> Unit, onGenerate: () -> Unit) {
        // Show QR options bottom sheet
    }

    fun showQRCode(qrData: String) {
        // Display QR code image
    }

    fun showTransactionHistory(transactions: List<Transaction>) {
        // Show transaction history bottom sheet
    }

    fun showSettings(
        isAuthenticated: Boolean,
        onLogin: () -> Unit,
        onLogout: () -> Unit,
        onToggleNotifications: (Boolean) -> Unit
    ) {
        // Show settings bottom sheet
    }

    fun showLoginSheet(onCredentialsSubmit: (String, String) -> Unit) {
        // Show login bottom sheet
    }

    fun clearWalletData() {
        currentBalance = null
        balanceText.text = "Login"
    }

    // ==================== Extensions ====================

    private val Int.dp: Int
        get() = (this * context.resources.displayMetrics.density).toInt()
}
