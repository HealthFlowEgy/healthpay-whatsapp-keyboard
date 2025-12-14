package tech.healthpay.keyboard.model

/**
 * Response from OTP request API.
 */
data class OtpRequestResponse(
    val requestId: String,
    val expiresIn: Int,
    val message: String
)

/**
 * Response from OTP verification API.
 */
data class OtpVerifyResponse(
    val accessToken: String,
    val refreshToken: String?,
    val expiresIn: Int,
    val userId: String?
)

/**
 * Wallet balance information.
 */
data class WalletBalance(
    val available: Double,
    val pending: Double,
    val currency: String
)

/**
 * Transaction details.
 */
data class Transaction(
    val id: String,
    val type: TransactionType,
    val amount: Double,
    val currency: String,
    val status: TransactionStatus,
    val recipientPhone: String?,
    val senderPhone: String?,
    val note: String?,
    val reference: String?,
    val createdAt: Long
)

/**
 * Transaction types.
 */
enum class TransactionType {
    SEND,
    RECEIVE,
    TOP_UP,
    WITHDRAW,
    PAYMENT
}

/**
 * Transaction status.
 */
enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED
}
