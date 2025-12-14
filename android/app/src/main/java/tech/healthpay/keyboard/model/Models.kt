package tech.healthpay.keyboard.model

/**
 * Data class representing keyboard settings
 */
data class KeyboardSettings(
    val biometricEnabled: Boolean = true,
    val soundEnabled: Boolean = true,
    val vibrationEnabled: Boolean = true,
    val showBalance: Boolean = true,
    val language: String = "en"
) {
    companion object {
        val DEFAULT = KeyboardSettings()
    }
}

/**
 * Data class for wallet balance response
 */
data class WalletBalance(
    val balance: Double,
    val currency: String,
    val formattedBalance: String
)

/**
 * Data class for payment transaction
 */
data class PaymentTransaction(
    val id: String,
    val amount: Double,
    val currency: String,
    val recipient: String?,
    val sender: String?,
    val status: TransactionStatus,
    val type: TransactionType,
    val timestamp: Long,
    val note: String?
)

/**
 * Transaction status enum
 */
enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Transaction type enum
 */
enum class TransactionType {
    SEND,
    RECEIVE,
    REQUEST
}

/**
 * Data class for payment request
 */
data class PaymentRequest(
    val requestId: String,
    val amount: Double,
    val currency: String,
    val paymentLink: String?,
    val qrCodeData: String?,
    val expiresAt: Long?
)

/**
 * User profile data
 */
data class UserProfile(
    val userId: String,
    val phoneNumber: String,
    val displayName: String?,
    val email: String?,
    val isVerified: Boolean
)
