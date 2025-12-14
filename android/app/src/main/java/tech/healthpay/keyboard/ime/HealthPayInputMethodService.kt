package tech.healthpay.keyboard.ime

import android.content.Context
import android.inputmethodservice.InputMethodService
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.view.KeyEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.lifecycleScope
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import tech.healthpay.keyboard.api.HealthPayApiClient
import tech.healthpay.keyboard.model.PaymentResult
import tech.healthpay.keyboard.model.Transaction
import tech.healthpay.keyboard.security.AuthenticationManager
import tech.healthpay.keyboard.security.BiometricHelper
import tech.healthpay.keyboard.ui.HealthPayKeyboardView
import tech.healthpay.keyboard.ui.PaymentBottomSheet
import tech.healthpay.keyboard.ui.QuickPayDialog
import tech.healthpay.keyboard.viewmodel.KeyboardViewModel
import tech.healthpay.keyboard.viewmodel.WalletViewModel
import javax.inject.Inject

/**
 * HealthPay Custom Input Method Service
 * 
 * This service provides a custom keyboard with integrated HealthPay wallet
 * functionality for WhatsApp and other messaging apps.
 * 
 * Key Features:
 * - Standard QWERTY keyboard with Arabic support
 * - Quick payment buttons in toolbar
 * - Biometric authentication for payments
 * - Balance display and transaction history
 */
@AndroidEntryPoint
class HealthPayInputMethodService : InputMethodService(), 
    LifecycleOwner, 
    ViewModelStoreOwner,
    HealthPayKeyboardView.KeyboardListener {

    // ==================== Injected Dependencies ====================
    
    @Inject lateinit var authManager: AuthenticationManager
    @Inject lateinit var apiClient: HealthPayApiClient
    @Inject lateinit var biometricHelper: BiometricHelper
    @Inject lateinit var walletViewModel: WalletViewModel
    @Inject lateinit var keyboardViewModel: KeyboardViewModel

    // ==================== Lifecycle Management ====================
    
    private val lifecycleRegistry = LifecycleRegistry(this)
    private val viewModelStoreOwner = ViewModelStore()
    
    override val lifecycle: Lifecycle get() = lifecycleRegistry
    override val viewModelStore: ViewModelStore get() = viewModelStoreOwner

    // ==================== Views & State ====================
    
    private var keyboardView: HealthPayKeyboardView? = null
    private var currentInputConnection: InputConnection? = null
    private var currentEditorInfo: EditorInfo? = null
    private var isWhatsAppContext = false
    private var vibrator: Vibrator? = null

    // ==================== Lifecycle Methods ====================

    override fun onCreate() {
        super.onCreate()
        lifecycleRegistry.currentState = Lifecycle.State.CREATED
        initializeVibrator()
        observeViewModels()
    }

    override fun onCreateInputView(): View {
        lifecycleRegistry.currentState = Lifecycle.State.STARTED
        
        keyboardView = HealthPayKeyboardView(this).apply {
            setKeyboardListener(this@HealthPayInputMethodService)
            setAuthenticationState(authManager.isAuthenticated())
        }
        
        // Load wallet data if authenticated
        if (authManager.isAuthenticated()) {
            loadWalletData()
        }
        
        return keyboardView!!
    }

    override fun onStartInput(attribute: EditorInfo?, restarting: Boolean) {
        super.onStartInput(attribute, restarting)
        currentEditorInfo = attribute
        currentInputConnection = currentInputBinding?.connection
        
        // Detect if we're in WhatsApp
        isWhatsAppContext = attribute?.packageName?.let { packageName ->
            packageName.contains("whatsapp", ignoreCase = true) ||
            packageName == "com.whatsapp" ||
            packageName == "com.whatsapp.w4b"
        } ?: false
        
        // Update keyboard state based on context
        keyboardView?.apply {
            setWhatsAppMode(isWhatsAppContext)
            updatePaymentButtonVisibility(isWhatsAppContext && authManager.isAuthenticated())
        }
    }

    override fun onFinishInput() {
        super.onFinishInput()
        currentInputConnection = null
    }

    override fun onDestroy() {
        lifecycleRegistry.currentState = Lifecycle.State.DESTROYED
        viewModelStoreOwner.clear()
        super.onDestroy()
    }

    // ==================== ViewModel Observers ====================

    private fun observeViewModels() {
        lifecycleScope.launch {
            // Observe wallet balance
            walletViewModel.balance.collectLatest { balance ->
                keyboardView?.updateBalance(balance)
            }
        }
        
        lifecycleScope.launch {
            // Observe authentication state
            walletViewModel.authState.collectLatest { isAuth ->
                keyboardView?.setAuthenticationState(isAuth)
                if (isAuth) loadWalletData()
            }
        }
        
        lifecycleScope.launch {
            // Observe payment results
            walletViewModel.paymentResult.collectLatest { result ->
                handlePaymentResult(result)
            }
        }
    }

    private fun loadWalletData() {
        lifecycleScope.launch {
            walletViewModel.refreshBalance()
            walletViewModel.loadRecentTransactions(limit = 5)
        }
    }

    // ==================== Keyboard Listener Implementation ====================

    override fun onKeyPressed(keyCode: Int, keyText: String) {
        vibrate()
        
        when (keyCode) {
            KeyboardKeys.DELETE -> handleDelete()
            KeyboardKeys.ENTER -> handleEnter()
            KeyboardKeys.SPACE -> handleSpace()
            KeyboardKeys.SHIFT -> handleShift()
            KeyboardKeys.LANGUAGE -> handleLanguageSwitch()
            KeyboardKeys.PAY -> handlePayAction()
            KeyboardKeys.WALLET -> handleWalletAction()
            KeyboardKeys.QR -> handleQRAction()
            KeyboardKeys.HISTORY -> handleHistoryAction()
            else -> commitText(keyText)
        }
    }

    override fun onPayButtonClicked() {
        handlePayAction()
    }

    override fun onRequestPaymentClicked() {
        handleRequestPayment()
    }

    override fun onBalanceClicked() {
        handleWalletAction()
    }

    override fun onQRScanClicked() {
        handleQRAction()
    }

    override fun onSettingsClicked() {
        handleSettingsAction()
    }

    // ==================== Text Input Handlers ====================

    private fun commitText(text: String) {
        currentInputConnection?.commitText(text, 1)
    }

    private fun handleDelete() {
        currentInputConnection?.apply {
            val selectedText = getSelectedText(0)
            if (selectedText.isNullOrEmpty()) {
                deleteSurroundingText(1, 0)
            } else {
                commitText("")
            }
        }
    }

    private fun handleEnter() {
        val imeAction = currentEditorInfo?.imeOptions?.and(EditorInfo.IME_MASK_ACTION)
        when (imeAction) {
            EditorInfo.IME_ACTION_SEND -> {
                currentInputConnection?.performEditorAction(EditorInfo.IME_ACTION_SEND)
            }
            EditorInfo.IME_ACTION_SEARCH -> {
                currentInputConnection?.performEditorAction(EditorInfo.IME_ACTION_SEARCH)
            }
            EditorInfo.IME_ACTION_GO -> {
                currentInputConnection?.performEditorAction(EditorInfo.IME_ACTION_GO)
            }
            else -> {
                currentInputConnection?.sendKeyEvent(
                    KeyEvent(KeyEvent.ACTION_DOWN, KeyEvent.KEYCODE_ENTER)
                )
            }
        }
    }

    private fun handleSpace() {
        commitText(" ")
    }

    private fun handleShift() {
        keyboardView?.toggleShift()
    }

    private fun handleLanguageSwitch() {
        keyboardView?.switchLanguage()
    }

    // ==================== Payment Action Handlers ====================

    private fun handlePayAction() {
        if (!authManager.isAuthenticated()) {
            showAuthenticationRequired()
            return
        }
        
        // Require biometric authentication before payment
        biometricHelper.authenticate(
            context = this,
            title = "Authenticate to Pay",
            subtitle = "Confirm your identity for HealthPay",
            onSuccess = { showPaymentDialog() },
            onError = { errorMessage ->
                keyboardView?.showError("Authentication failed: $errorMessage")
            }
        )
    }

    private fun showPaymentDialog() {
        val dialog = QuickPayDialog(this).apply {
            onPaymentSubmit = { amount, recipientPhone, note ->
                processPayment(amount, recipientPhone, note)
            }
            onDismiss = { /* Optional cleanup */ }
        }
        dialog.show()
    }

    private fun processPayment(amount: Double, recipientPhone: String, note: String?) {
        lifecycleScope.launch {
            keyboardView?.showLoading(true)
            
            try {
                val result = walletViewModel.sendPayment(
                    amount = amount,
                    recipientPhone = recipientPhone,
                    description = note ?: "Payment via WhatsApp"
                )
                
                if (result.isSuccess) {
                    insertPaymentConfirmation(result.transaction!!)
                } else {
                    keyboardView?.showError(result.errorMessage ?: "Payment failed")
                }
            } catch (e: Exception) {
                keyboardView?.showError("Error: ${e.message}")
            } finally {
                keyboardView?.showLoading(false)
            }
        }
    }

    private fun handlePaymentResult(result: PaymentResult?) {
        result ?: return
        
        if (result.isSuccess && result.transaction != null) {
            insertPaymentConfirmation(result.transaction)
        }
    }

    private fun insertPaymentConfirmation(transaction: Transaction) {
        val message = buildPaymentMessage(transaction)
        currentInputConnection?.commitText(message, 1)
        
        // Show success feedback
        keyboardView?.showSuccess("Payment sent successfully!")
    }

    private fun buildPaymentMessage(transaction: Transaction): String {
        return buildString {
            appendLine("💳 *HealthPay Payment Sent*")
            appendLine("━━━━━━━━━━━━━━━━")
            appendLine("Amount: EGP ${String.format("%,.2f", transaction.amount)}")
            appendLine("To: ${transaction.recipientPhone}")
            transaction.description?.let { appendLine("Note: $it") }
            appendLine("Ref: ${transaction.referenceNumber}")
            appendLine("Time: ${transaction.formattedDate}")
            appendLine("━━━━━━━━━━━━━━━━")
            appendLine("✅ Powered by HealthPay Wallet")
        }
    }

    private fun handleRequestPayment() {
        if (!authManager.isAuthenticated()) {
            showAuthenticationRequired()
            return
        }
        
        val dialog = PaymentBottomSheet(this, PaymentBottomSheet.Mode.REQUEST).apply {
            onRequestSubmit = { amount, note ->
                generatePaymentRequest(amount, note)
            }
        }
        dialog.show()
    }

    private fun generatePaymentRequest(amount: Double, note: String?) {
        lifecycleScope.launch {
            try {
                val link = walletViewModel.generatePaymentLink(amount, note)
                insertPaymentRequest(amount, link, note)
            } catch (e: Exception) {
                keyboardView?.showError("Failed to generate payment link")
            }
        }
    }

    private fun insertPaymentRequest(amount: Double, link: String, note: String?) {
        val message = buildString {
            appendLine("💰 *Payment Request*")
            appendLine("━━━━━━━━━━━━━━━━")
            appendLine("Amount: EGP ${String.format("%,.2f", amount)}")
            note?.let { appendLine("For: $it") }
            appendLine()
            appendLine("👉 Tap to pay: $link")
            appendLine("━━━━━━━━━━━━━━━━")
            appendLine("Powered by HealthPay")
        }
        currentInputConnection?.commitText(message, 1)
    }

    private fun handleWalletAction() {
        if (!authManager.isAuthenticated()) {
            showAuthenticationRequired()
            return
        }
        
        biometricHelper.authenticate(
            context = this,
            title = "View Wallet",
            subtitle = "Authenticate to see your balance",
            onSuccess = { showWalletBalance() },
            onError = { keyboardView?.showError("Authentication required") }
        )
    }

    private fun showWalletBalance() {
        lifecycleScope.launch {
            val balance = walletViewModel.getCurrentBalance()
            val message = buildString {
                appendLine("💰 *My HealthPay Balance*")
                appendLine("━━━━━━━━━━━━━━━━")
                appendLine("Available: EGP ${String.format("%,.2f", balance.available)}")
                appendLine("Pending: EGP ${String.format("%,.2f", balance.pending)}")
                appendLine("━━━━━━━━━━━━━━━━")
                appendLine("_Checked at ${balance.timestamp}_")
            }
            // Don't auto-insert, just show in dialog
            keyboardView?.showBalanceCard(balance)
        }
    }

    private fun handleQRAction() {
        if (!authManager.isAuthenticated()) {
            showAuthenticationRequired()
            return
        }
        
        // Launch QR scanner activity
        // Note: QR scanning requires camera and external activity
        keyboardView?.showQROptions(
            onScan = { launchQRScanner() },
            onGenerate = { generateMyQR() }
        )
    }

    private fun launchQRScanner() {
        // Would launch external QR scanner activity
        // For IME, this requires starting an activity with FLAG_ACTIVITY_NEW_TASK
    }

    private fun generateMyQR() {
        lifecycleScope.launch {
            val qrData = walletViewModel.generateReceiveQR()
            keyboardView?.showQRCode(qrData)
        }
    }

    private fun handleHistoryAction() {
        if (!authManager.isAuthenticated()) {
            showAuthenticationRequired()
            return
        }
        
        lifecycleScope.launch {
            val transactions = walletViewModel.getRecentTransactions()
            keyboardView?.showTransactionHistory(transactions)
        }
    }

    private fun handleSettingsAction() {
        keyboardView?.showSettings(
            isAuthenticated = authManager.isAuthenticated(),
            onLogin = { showLoginPrompt() },
            onLogout = { handleLogout() },
            onToggleNotifications = { enabled -> /* Handle */ }
        )
    }

    private fun showAuthenticationRequired() {
        keyboardView?.showAuthPrompt(
            onLoginClicked = { showLoginPrompt() }
        )
    }

    private fun showLoginPrompt() {
        // Launch HealthPay app for authentication
        // Or show embedded OAuth flow
        keyboardView?.showLoginSheet(
            onCredentialsSubmit = { username, password ->
                performLogin(username, password)
            }
        )
    }

    private fun performLogin(username: String, password: String) {
        lifecycleScope.launch {
            try {
                val success = authManager.login(username, password)
                if (success) {
                    keyboardView?.setAuthenticationState(true)
                    loadWalletData()
                } else {
                    keyboardView?.showError("Invalid credentials")
                }
            } catch (e: Exception) {
                keyboardView?.showError("Login failed: ${e.message}")
            }
        }
    }

    private fun handleLogout() {
        lifecycleScope.launch {
            authManager.logout()
            keyboardView?.setAuthenticationState(false)
            keyboardView?.clearWalletData()
        }
    }

    // ==================== Utility Methods ====================

    private fun initializeVibrator() {
        vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            val vibratorManager = getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager
            vibratorManager.defaultVibrator
        } else {
            @Suppress("DEPRECATION")
            getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
        }
    }

    private fun vibrate() {
        if (keyboardViewModel.isHapticFeedbackEnabled) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator?.vibrate(VibrationEffect.createOneShot(30, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator?.vibrate(30)
            }
        }
    }
}

/**
 * Key code constants for special keyboard actions
 */
object KeyboardKeys {
    const val DELETE = -1
    const val ENTER = -2
    const val SPACE = -3
    const val SHIFT = -4
    const val LANGUAGE = -5
    const val PAY = -100
    const val WALLET = -101
    const val QR = -102
    const val HISTORY = -103
    const val SETTINGS = -104
}
