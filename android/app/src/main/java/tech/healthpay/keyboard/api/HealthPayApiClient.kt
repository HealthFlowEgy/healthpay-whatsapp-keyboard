package tech.healthpay.keyboard.api

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.logging.HttpLoggingInterceptor
import tech.healthpay.keyboard.model.PaymentRequest
import tech.healthpay.keyboard.model.PaymentTransaction
import tech.healthpay.keyboard.model.TransactionStatus
import tech.healthpay.keyboard.model.TransactionType
import tech.healthpay.keyboard.model.WalletBalance
import tech.healthpay.keyboard.security.TokenManager
import java.util.concurrent.TimeUnit

/**
 * HealthPay API Client
 * 
 * Handles all HTTP communication with the HealthPay backend.
 * 
 * NO HILT - Instantiated manually in Application class
 */
class HealthPayApiClient(
    private val context: Context,
    private val tokenManager: TokenManager
) {

    private val gson: Gson = GsonBuilder()
        .setDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
        .create()

    private val client: OkHttpClient by lazy {
        val logging = HttpLoggingInterceptor { message ->
            Log.d(TAG, message)
        }.apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        OkHttpClient.Builder()
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)
            .addInterceptor(logging)
            .addInterceptor { chain ->
                val original = chain.request()
                val token = tokenManager.getAccessToken()

                val request = original.newBuilder()
                    .header("Content-Type", "application/json")
                    .header("Accept", "application/json")
                    .apply {
                        if (!token.isNullOrBlank()) {
                            header("Authorization", "Bearer $token")
                        }
                    }
                    .build()

                chain.proceed(request)
            }
            .build()
    }

    // ==================== Authentication APIs ====================

    /**
     * Request OTP for phone number
     */
    suspend fun requestOtp(phoneNumber: String): OtpResponse {
        return withContext(Dispatchers.IO) {
            try {
                val body = mapOf("phone" to phoneNumber)
                val response = post<OtpResponse>("$BASE_URL/auth/request-otp", body)
                response ?: OtpResponse(success = false, message = "Request failed")
            } catch (e: Exception) {
                Log.e(TAG, "requestOtp failed", e)
                OtpResponse(success = false, message = e.message)
            }
        }
    }

    /**
     * Verify OTP and get tokens
     */
    suspend fun verifyOtp(phoneNumber: String, otp: String): AuthResponse {
        return withContext(Dispatchers.IO) {
            try {
                val body = mapOf("phone" to phoneNumber, "otp" to otp)
                val response = post<AuthResponse>("$BASE_URL/auth/verify-otp", body)
                response ?: AuthResponse(success = false, message = "Verification failed")
            } catch (e: Exception) {
                Log.e(TAG, "verifyOtp failed", e)
                AuthResponse(success = false, message = e.message)
            }
        }
    }

    /**
     * Refresh access token
     */
    suspend fun refreshToken(refreshToken: String): AuthResponse {
        return withContext(Dispatchers.IO) {
            try {
                val body = mapOf("refresh_token" to refreshToken)
                val response = post<AuthResponse>("$BASE_URL/auth/refresh", body)
                response ?: AuthResponse(success = false, message = "Refresh failed")
            } catch (e: Exception) {
                Log.e(TAG, "refreshToken failed", e)
                AuthResponse(success = false, message = e.message)
            }
        }
    }

    // ==================== Wallet APIs ====================

    /**
     * Get wallet balance
     */
    suspend fun getWalletBalance(): WalletBalanceResponse {
        return withContext(Dispatchers.IO) {
            try {
                val response = get<WalletBalanceResponse>("$BASE_URL/wallet/balance")
                response ?: WalletBalanceResponse(success = false, message = "Failed to get balance")
            } catch (e: Exception) {
                Log.e(TAG, "getWalletBalance failed", e)
                WalletBalanceResponse(success = false, message = e.message)
            }
        }
    }

    /**
     * Send payment
     */
    suspend fun sendPayment(
        amount: Double,
        recipient: String,
        currency: String = "EGP",
        note: String? = null
    ): PaymentResponse {
        return withContext(Dispatchers.IO) {
            try {
                val body = mutableMapOf<String, Any>(
                    "amount" to amount,
                    "recipient" to recipient,
                    "currency" to currency
                )
                note?.let { body["note"] = it }

                val response = post<PaymentResponse>("$BASE_URL/wallet/send", body)
                response ?: PaymentResponse(success = false, message = "Payment failed")
            } catch (e: Exception) {
                Log.e(TAG, "sendPayment failed", e)
                PaymentResponse(success = false, message = e.message)
            }
        }
    }

    /**
     * Create payment request
     */
    suspend fun requestPayment(
        amount: Double,
        currency: String = "EGP",
        note: String? = null
    ): PaymentRequestResponse {
        return withContext(Dispatchers.IO) {
            try {
                val body = mutableMapOf<String, Any>(
                    "amount" to amount,
                    "currency" to currency
                )
                note?.let { body["note"] = it }

                val response = post<PaymentRequestResponse>("$BASE_URL/wallet/request", body)
                response ?: PaymentRequestResponse(success = false, message = "Request failed")
            } catch (e: Exception) {
                Log.e(TAG, "requestPayment failed", e)
                PaymentRequestResponse(success = false, message = e.message)
            }
        }
    }

    /**
     * Get transaction history
     */
    suspend fun getTransactionHistory(
        page: Int = 1,
        limit: Int = 20
    ): TransactionHistoryResponse {
        return withContext(Dispatchers.IO) {
            try {
                val response = get<TransactionHistoryResponse>(
                    "$BASE_URL/wallet/transactions?page=$page&limit=$limit"
                )
                response ?: TransactionHistoryResponse(success = false, message = "Failed to get history")
            } catch (e: Exception) {
                Log.e(TAG, "getTransactionHistory failed", e)
                TransactionHistoryResponse(success = false, message = e.message)
            }
        }
    }

    // ==================== Helper Methods ====================

    private inline fun <reified T> get(url: String): T? {
        val request = Request.Builder()
            .url(url)
            .get()
            .build()

        val response = client.newCall(request).execute()
        val body = response.body?.string()

        return if (response.isSuccessful && body != null) {
            gson.fromJson(body, T::class.java)
        } else {
            null
        }
    }

    private inline fun <reified T> post(url: String, body: Any): T? {
        val jsonBody = gson.toJson(body).toRequestBody(JSON_MEDIA_TYPE)

        val request = Request.Builder()
            .url(url)
            .post(jsonBody)
            .build()

        val response = client.newCall(request).execute()
        val responseBody = response.body?.string()

        return if (response.isSuccessful && responseBody != null) {
            gson.fromJson(responseBody, T::class.java)
        } else {
            null
        }
    }

    /**
     * Clear authentication state
     */
    fun clearAuth() {
        // Nothing to clear in the client itself
        // TokenManager handles token storage
    }

    companion object {
        private const val TAG = "HealthPayApiClient"
        private const val BASE_URL = "https://portal.beta.healthpay.tech/api/v1"
        private val JSON_MEDIA_TYPE = "application/json".toMediaType()
    }

    // ==================== Response Data Classes ====================

    data class OtpResponse(
        val success: Boolean,
        val message: String? = null
    )

    data class AuthResponse(
        val success: Boolean,
        val message: String? = null,
        @SerializedName("access_token")
        val accessToken: String? = null,
        @SerializedName("refresh_token")
        val refreshToken: String? = null,
        @SerializedName("expires_in")
        val expiresIn: Long? = null,
        @SerializedName("user_id")
        val userId: String? = null
    )

    data class WalletBalanceResponse(
        val success: Boolean,
        val message: String? = null,
        val balance: Double? = null,
        val currency: String? = null,
        @SerializedName("formatted_balance")
        val formattedBalance: String? = null
    ) {
        fun toWalletBalance(): WalletBalance? {
            return if (balance != null && currency != null) {
                WalletBalance(
                    balance = balance,
                    currency = currency,
                    formattedBalance = formattedBalance ?: "$currency $balance"
                )
            } else null
        }
    }

    data class PaymentResponse(
        val success: Boolean,
        val message: String? = null,
        @SerializedName("transaction_id")
        val transactionId: String? = null,
        val amount: Double? = null,
        val currency: String? = null,
        val status: String? = null
    )

    data class PaymentRequestResponse(
        val success: Boolean,
        val message: String? = null,
        @SerializedName("request_id")
        val requestId: String? = null,
        @SerializedName("payment_link")
        val paymentLink: String? = null,
        @SerializedName("qr_code")
        val qrCode: String? = null,
        val amount: Double? = null,
        val currency: String? = null
    ) {
        fun toPaymentRequest(): PaymentRequest? {
            return if (requestId != null && amount != null && currency != null) {
                PaymentRequest(
                    requestId = requestId,
                    amount = amount,
                    currency = currency,
                    paymentLink = paymentLink,
                    qrCodeData = qrCode,
                    expiresAt = null
                )
            } else null
        }
    }

    data class TransactionHistoryResponse(
        val success: Boolean,
        val message: String? = null,
        val transactions: List<TransactionDto>? = null,
        val page: Int? = null,
        @SerializedName("total_pages")
        val totalPages: Int? = null
    )

    data class TransactionDto(
        val id: String,
        val amount: Double,
        val currency: String,
        val recipient: String?,
        val sender: String?,
        val status: String,
        val type: String,
        val timestamp: Long,
        val note: String?
    ) {
        fun toPaymentTransaction(): PaymentTransaction {
            return PaymentTransaction(
                id = id,
                amount = amount,
                currency = currency,
                recipient = recipient,
                sender = sender,
                status = when (status.uppercase()) {
                    "COMPLETED" -> TransactionStatus.COMPLETED
                    "PENDING" -> TransactionStatus.PENDING
                    "FAILED" -> TransactionStatus.FAILED
                    "CANCELLED" -> TransactionStatus.CANCELLED
                    else -> TransactionStatus.PENDING
                },
                type = when (type.uppercase()) {
                    "SEND" -> TransactionType.SEND
                    "RECEIVE" -> TransactionType.RECEIVE
                    "REQUEST" -> TransactionType.REQUEST
                    else -> TransactionType.SEND
                },
                timestamp = timestamp,
                note = note
            )
        }
    }
}
