package tech.healthpay.keyboard.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Wallet Balance Model
 */
data class WalletBalance(
    val available: Double,
    val pending: Double,
    val currency: String = "EGP",
    val lastUpdated: String
) {
    val timestamp: String
        get() = SimpleDateFormat("MMM dd, HH:mm", Locale.getDefault()).format(Date())
    
    val total: Double
        get() = available + pending
    
    val formattedAvailable: String
        get() = String.format(Locale.getDefault(), "%s %,.2f", currency, available)
    
    val formattedPending: String
        get() = String.format(Locale.getDefault(), "%s %,.2f", currency, pending)
    
    val formattedTotal: String
        get() = String.format(Locale.getDefault(), "%s %,.2f", currency, total)
}

/**
 * Transaction Model
 */
data class Transaction(
    val id: String,
    val type: TransactionType,
    val status: TransactionStatus,
    val amount: Double,
    val currency: String = "EGP",
    val recipientPhone: String? = null,
    val recipientName: String? = null,
    val senderPhone: String? = null,
    val senderName: String? = null,
    val description: String? = null,
    val referenceNumber: String,
    val createdAt: String,
    val completedAt: String? = null,
    val fee: Double = 0.0,
    val metadata: Map<String, Any>? = null
) {
    val formattedAmount: String
        get() {
            val sign = when (type) {
                TransactionType.SENT -> "-"
                TransactionType.RECEIVED -> "+"
                else -> ""
            }
            return "$sign$currency ${String.format(Locale.getDefault(), "%,.2f", amount)}"
        }
    
    val formattedDate: String
        get() = try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault())
            val date = inputFormat.parse(createdAt)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            createdAt
        }
    
    val shortDate: String
        get() = try {
            val inputFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.getDefault())
            val outputFormat = SimpleDateFormat("MMM dd", Locale.getDefault())
            val date = inputFormat.parse(createdAt)
            outputFormat.format(date ?: Date())
        } catch (e: Exception) {
            createdAt
        }
    
    val counterpartyName: String
        get() = when (type) {
            TransactionType.SENT -> recipientName ?: recipientPhone ?: "Unknown"
            TransactionType.RECEIVED -> senderName ?: senderPhone ?: "Unknown"
            else -> "System"
        }
    
    val counterpartyInitial: String
        get() = counterpartyName.firstOrNull()?.uppercase() ?: "?"
    
    val statusColor: Int
        get() = when (status) {
            TransactionStatus.COMPLETED -> 0xFF4CAF50.toInt() // Green
            TransactionStatus.PENDING -> 0xFFFFA726.toInt() // Orange
            TransactionStatus.FAILED -> 0xFFE53935.toInt() // Red
            TransactionStatus.CANCELLED -> 0xFF9E9E9E.toInt() // Grey
        }
}

/**
 * Transaction Types
 */
enum class TransactionType {
    SENT,
    RECEIVED,
    TOPUP,
    WITHDRAWAL,
    REFUND,
    FEE
}

/**
 * Transaction Status
 */
enum class TransactionStatus {
    PENDING,
    COMPLETED,
    FAILED,
    CANCELLED
}

/**
 * Payment Result
 */
data class PaymentResult(
    val isSuccess: Boolean,
    val transaction: Transaction? = null,
    val errorMessage: String? = null,
    val errorCode: String? = null
) {
    companion object {
        fun success(transaction: Transaction) = PaymentResult(
            isSuccess = true,
            transaction = transaction
        )
        
        fun error(message: String, code: String? = null) = PaymentResult(
            isSuccess = false,
            errorMessage = message,
            errorCode = code
        )
    }
}

/**
 * Quick Pay Request (for UI)
 */
data class QuickPayRequest(
    val amount: Double,
    val recipientPhone: String,
    val description: String? = null,
    val saveRecipient: Boolean = false
)

/**
 * Payment Request (for requesting money)
 */
data class PaymentRequest(
    val amount: Double,
    val description: String? = null,
    val expirationHours: Int = 24
)

/**
 * Saved Recipient
 */
data class SavedRecipient(
    val id: String,
    val name: String,
    val phone: String,
    val avatar: String? = null,
    val lastTransactionAt: String? = null,
    val transactionCount: Int = 0
)

/**
 * User Profile
 */
data class UserProfile(
    val id: String,
    val name: String,
    val phone: String,
    val email: String?,
    val avatar: String?,
    val isVerified: Boolean,
    val kycLevel: Int,
    val createdAt: String
)

/**
 * Keyboard Settings
 */
data class KeyboardSettings(
    val hapticFeedback: Boolean = true,
    val soundFeedback: Boolean = false,
    val autoCapitalize: Boolean = true,
    val showPaymentButton: Boolean = true,
    val defaultLanguage: String = "en",
    val quickPayEnabled: Boolean = true,
    val biometricRequired: Boolean = true,
    val sessionTimeout: Int = 15 // minutes
)

/**
 * QR Code Data
 */
data class QRCodeData(
    val type: QRType,
    val paymentId: String? = null,
    val amount: Double? = null,
    val recipientPhone: String? = null,
    val recipientName: String? = null,
    val description: String? = null,
    val expiresAt: String? = null
)

enum class QRType {
    PAYMENT,
    RECEIVE,
    MERCHANT
}

/**
 * Notification
 */
data class WalletNotification(
    val id: String,
    val type: NotificationType,
    val title: String,
    val message: String,
    val data: Map<String, Any>?,
    val read: Boolean,
    val createdAt: String
)

enum class NotificationType {
    PAYMENT_RECEIVED,
    PAYMENT_SENT,
    PAYMENT_FAILED,
    PAYMENT_REQUEST,
    PROMO,
    SYSTEM
}
