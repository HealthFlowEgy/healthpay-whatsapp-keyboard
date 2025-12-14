package tech.healthpay.keyboard.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import tech.healthpay.keyboard.api.ApiResult
import tech.healthpay.keyboard.api.HealthPayApiClient
import tech.healthpay.keyboard.api.SendPaymentRequest
import tech.healthpay.keyboard.api.RequestPaymentRequest
import tech.healthpay.keyboard.model.*
import tech.healthpay.keyboard.security.AuthenticationManager
import tech.healthpay.keyboard.security.EncryptionManager
import javax.inject.Inject

/**
 * ViewModel for Wallet operations
 * 
 * Manages:
 * - Wallet balance
 * - Transactions
 * - Payments (send/request)
 * - QR operations
 */
@HiltViewModel
class WalletViewModel @Inject constructor(
    private val apiClient: HealthPayApiClient,
    private val authManager: AuthenticationManager,
    private val encryptionManager: EncryptionManager
) : ViewModel() {

    // ==================== State Flows ====================

    private val _balance = MutableStateFlow<WalletBalance?>(null)
    val balance: StateFlow<WalletBalance?> = _balance.asStateFlow()

    private val _transactions = MutableStateFlow<List<Transaction>>(emptyList())
    val transactions: StateFlow<List<Transaction>> = _transactions.asStateFlow()

    private val _recentTransactions = MutableStateFlow<List<Transaction>>(emptyList())
    val recentTransactions: StateFlow<List<Transaction>> = _recentTransactions.asStateFlow()

    private val _authState = MutableStateFlow(false)
    val authState: StateFlow<Boolean> = _authState.asStateFlow()

    private val _paymentResult = MutableStateFlow<PaymentResult?>(null)
    val paymentResult: StateFlow<PaymentResult?> = _paymentResult.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    private val _savedRecipients = MutableStateFlow<List<SavedRecipient>>(emptyList())
    val savedRecipients: StateFlow<List<SavedRecipient>> = _savedRecipients.asStateFlow()

    // ==================== Initialization ====================

    init {
        checkAuthState()
    }

    private fun checkAuthState() {
        _authState.value = authManager.isAuthenticated()
        if (_authState.value) {
            refreshBalance()
            loadRecentTransactions()
        }
    }

    // ==================== Balance Operations ====================

    fun refreshBalance() {
        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            when (val result = apiClient.getBalance()) {
                is ApiResult.Success -> {
                    _balance.value = result.data
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                }
            }

            _isLoading.value = false
        }
    }

    suspend fun getCurrentBalance(): WalletBalance {
        return _balance.value ?: run {
            when (val result = apiClient.getBalance()) {
                is ApiResult.Success -> {
                    _balance.value = result.data
                    result.data
                }
                is ApiResult.Error -> {
                    WalletBalance(0.0, 0.0, "EGP", "")
                }
            }
        }
    }

    // ==================== Transaction Operations ====================

    fun loadRecentTransactions(limit: Int = 5) {
        viewModelScope.launch {
            when (val result = apiClient.getTransactionHistory(page = 1, limit = limit)) {
                is ApiResult.Success -> {
                    _recentTransactions.value = result.data.transactions
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                }
            }
        }
    }

    fun loadTransactionHistory(page: Int = 1, limit: Int = 20) {
        viewModelScope.launch {
            _isLoading.value = true

            when (val result = apiClient.getTransactionHistory(page, limit)) {
                is ApiResult.Success -> {
                    if (page == 1) {
                        _transactions.value = result.data.transactions
                    } else {
                        _transactions.value = _transactions.value + result.data.transactions
                    }
                }
                is ApiResult.Error -> {
                    _error.value = result.message
                }
            }

            _isLoading.value = false
        }
    }

    fun getRecentTransactions(): List<Transaction> = _recentTransactions.value

    // ==================== Payment Operations ====================

    suspend fun sendPayment(
        amount: Double,
        recipientPhone: String,
        description: String? = null,
        pin: String? = null
    ): PaymentResult {
        _isLoading.value = true
        _error.value = null

        val encryptedPin = pin?.let { encryptionManager.encryptPin(it) }

        val request = SendPaymentRequest(
            amount = amount,
            recipientPhone = recipientPhone,
            description = description ?: "Payment via HealthPay Keyboard",
            pin = encryptedPin
        )

        return when (val result = apiClient.sendPayment(request)) {
            is ApiResult.Success -> {
                // Refresh balance after successful payment
                refreshBalance()
                loadRecentTransactions()
                
                val paymentResult = PaymentResult.success(result.data)
                _paymentResult.value = paymentResult
                _isLoading.value = false
                paymentResult
            }
            is ApiResult.Error -> {
                val paymentResult = PaymentResult.error(result.message)
                _paymentResult.value = paymentResult
                _error.value = result.message
                _isLoading.value = false
                paymentResult
            }
        }
    }

    suspend fun generatePaymentLink(
        amount: Double,
        description: String? = null
    ): String {
        val request = RequestPaymentRequest(
            amount = amount,
            description = description,
            expiresIn = 24
        )

        return when (val result = apiClient.requestPayment(request)) {
            is ApiResult.Success -> {
                result.data.link
            }
            is ApiResult.Error -> {
                throw Exception(result.message)
            }
        }
    }

    // ==================== QR Operations ====================

    suspend fun generateReceiveQR(
        amount: Double? = null,
        description: String? = null
    ): String {
        return when (val result = apiClient.generatePaymentQR(amount, description)) {
            is ApiResult.Success -> {
                result.data.qrImage // Base64 encoded image
            }
            is ApiResult.Error -> {
                throw Exception(result.message)
            }
        }
    }

    suspend fun processQRPayment(qrData: String, pin: String): PaymentResult {
        val encryptedPin = encryptionManager.encryptPin(pin)

        return when (val result = apiClient.processQRPayment(qrData, encryptedPin)) {
            is ApiResult.Success -> {
                refreshBalance()
                loadRecentTransactions()
                PaymentResult.success(result.data)
            }
            is ApiResult.Error -> {
                PaymentResult.error(result.message)
            }
        }
    }

    // ==================== Utility Methods ====================

    fun clearError() {
        _error.value = null
    }

    fun clearPaymentResult() {
        _paymentResult.value = null
    }

    fun onLogout() {
        _balance.value = null
        _transactions.value = emptyList()
        _recentTransactions.value = emptyList()
        _authState.value = false
        _savedRecipients.value = emptyList()
    }
}

/**
 * ViewModel for Keyboard settings and state
 */
@HiltViewModel
class KeyboardViewModel @Inject constructor(
    private val settingsRepository: KeyboardSettingsRepository
) : ViewModel() {

    private val _settings = MutableStateFlow(KeyboardSettings())
    val settings: StateFlow<KeyboardSettings> = _settings.asStateFlow()

    val isHapticFeedbackEnabled: Boolean
        get() = _settings.value.hapticFeedback

    val isSoundFeedbackEnabled: Boolean
        get() = _settings.value.soundFeedback

    init {
        loadSettings()
    }

    private fun loadSettings() {
        viewModelScope.launch {
            _settings.value = settingsRepository.getSettings()
        }
    }

    fun updateSetting(update: (KeyboardSettings) -> KeyboardSettings) {
        viewModelScope.launch {
            val newSettings = update(_settings.value)
            _settings.value = newSettings
            settingsRepository.saveSettings(newSettings)
        }
    }

    fun toggleHapticFeedback() {
        updateSetting { it.copy(hapticFeedback = !it.hapticFeedback) }
    }

    fun toggleSoundFeedback() {
        updateSetting { it.copy(soundFeedback = !it.soundFeedback) }
    }

    fun setDefaultLanguage(language: String) {
        updateSetting { it.copy(defaultLanguage = language) }
    }

    fun toggleBiometricRequired() {
        updateSetting { it.copy(biometricRequired = !it.biometricRequired) }
    }
}

/**
 * Settings Repository Interface
 */
interface KeyboardSettingsRepository {
    suspend fun getSettings(): KeyboardSettings
    suspend fun saveSettings(settings: KeyboardSettings)
}
