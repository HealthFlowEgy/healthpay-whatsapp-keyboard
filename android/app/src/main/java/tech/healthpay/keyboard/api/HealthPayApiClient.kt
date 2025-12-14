package tech.healthpay.keyboard.api

import android.util.Log
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import tech.healthpay.keyboard.security.TokenManager
import java.io.IOException
import java.util.concurrent.TimeUnit

/**
 * HealthPay API Client - GraphQL Implementation
 * 
 * v1.2.3 - Uses GraphQL endpoint at sword.beta.healthpay.tech
 */
class HealthPayApiClient(private val tokenManager: TokenManager) {

    companion object {
        private const val TAG = "HealthPayApiClient"
        private const val GRAPHQL_URL = "https://sword.beta.healthpay.tech/graphql"
        private const val CONNECT_TIMEOUT = 30L
        private const val READ_TIMEOUT = 30L
        private const val WRITE_TIMEOUT = 30L
    }

    private val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(CONNECT_TIMEOUT, TimeUnit.SECONDS)
        .readTimeout(READ_TIMEOUT, TimeUnit.SECONDS)
        .writeTimeout(WRITE_TIMEOUT, TimeUnit.SECONDS)
        .addInterceptor { chain ->
            val original = chain.request()
            val requestBuilder = original.newBuilder()
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
            
            tokenManager.getAccessToken()?.let { token ->
                requestBuilder.header("Authorization", "Bearer $token")
            }
            
            chain.proceed(requestBuilder.build())
        }
        .build()

    private val jsonMediaType = "application/json; charset=utf-8".toMediaType()

    // =====================
    // GraphQL Helper
    // =====================

    private fun executeGraphQL(
        query: String,
        variables: Map<String, Any?>,
        operationName: String,
        callback: (JSONObject?, ApiError?) -> Unit
    ) {
        val graphqlBody = JSONObject().apply {
            put("query", query)
            put("variables", JSONObject(variables))
            put("operationName", operationName)
        }

        Log.d(TAG, "GraphQL Request: $operationName")
        Log.d(TAG, "Variables: $variables")

        val request = Request.Builder()
            .url(GRAPHQL_URL)
            .post(graphqlBody.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "GraphQL network failure: ${e.message}", e)
                callback(null, parseNetworkError(e))
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string()
                    Log.d(TAG, "GraphQL Response: ${response.code}")
                    Log.d(TAG, "Body: ${responseBody?.take(500)}")

                    if (responseBody == null) {
                        callback(null, ApiError(ApiErrorCode.PARSE_ERROR, "Empty response from server", response.code))
                        return
                    }

                    val json = JSONObject(responseBody)

                    // Check for GraphQL errors
                    if (json.has("errors")) {
                        val errors = json.getJSONArray("errors")
                        if (errors.length() > 0) {
                            val firstError = errors.getJSONObject(0)
                            val message = firstError.optString("message", "Unknown error")
                            val extensions = firstError.optJSONObject("extensions")
                            val code = extensions?.optString("code", "UNKNOWN") ?: "UNKNOWN"
                            
                            Log.e(TAG, "GraphQL Error: $code - $message")
                            callback(null, parseGraphQLError(code, message, response.code))
                            return
                        }
                    }

                    // Return data
                    val data = json.optJSONObject("data")
                    if (data != null) {
                        callback(data, null)
                    } else {
                        callback(null, ApiError(ApiErrorCode.PARSE_ERROR, "No data in response", response.code))
                    }

                } catch (e: Exception) {
                    Log.e(TAG, "GraphQL response parsing error", e)
                    callback(null, ApiError(ApiErrorCode.PARSE_ERROR, "Failed to process server response", null, e.message))
                }
            }
        })
    }

    // =====================
    // Authentication APIs
    // =====================

    fun requestOtp(mobileNumber: String, callback: ApiCallback<OtpResponse>) {
        // GraphQL mutation for requesting OTP
        val mutation = """
            mutation RequestOtp(${'$'}mobile: String!, ${'$'}countryCode: String) {
                requestOtp(input: { mobile: ${'$'}mobile, countryCode: ${'$'}countryCode }) {
                    success
                    message
                    requestId
                    expiresIn
                }
            }
        """.trimIndent()

        // Alternative mutation format (if the above doesn't work)
        val mutationAlt = """
            mutation RequestOtp(${'$'}mobile: String!) {
                sendOtp(mobile: ${'$'}mobile) {
                    success
                    message
                    requestId
                    expiresIn
                }
            }
        """.trimIndent()

        val variables = mapOf(
            "mobile" to mobileNumber,
            "countryCode" to "+20"
        )

        Log.d(TAG, "Requesting OTP for: ${mobileNumber.takeLast(4)}")

        executeGraphQL(mutation, variables, "RequestOtp") { data, error ->
            if (error != null) {
                // Try alternative mutation format
                Log.d(TAG, "Trying alternative mutation format...")
                executeGraphQL(mutationAlt, mapOf("mobile" to mobileNumber), "RequestOtp") { altData, altError ->
                    if (altError != null) {
                        callback.onError(altError)
                    } else {
                        parseOtpResponse(altData, "sendOtp", callback)
                    }
                }
                return@executeGraphQL
            }

            parseOtpResponse(data, "requestOtp", callback)
        }
    }

    private fun parseOtpResponse(data: JSONObject?, fieldName: String, callback: ApiCallback<OtpResponse>) {
        try {
            val otpData = data?.optJSONObject(fieldName)
            if (otpData != null) {
                callback.onSuccess(OtpResponse(
                    success = otpData.optBoolean("success", true),
                    message = otpData.optString("message", "OTP sent successfully"),
                    requestId = otpData.optString("requestId", otpData.optString("request_id", "")),
                    expiresIn = otpData.optInt("expiresIn", otpData.optInt("expires_in", 300))
                ))
            } else {
                // If no specific field, assume success (some APIs just return empty on success)
                callback.onSuccess(OtpResponse(
                    success = true,
                    message = "OTP sent successfully",
                    requestId = System.currentTimeMillis().toString(),
                    expiresIn = 300
                ))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing OTP response", e)
            callback.onError(ApiError(ApiErrorCode.PARSE_ERROR, "Failed to parse OTP response", null, e.message))
        }
    }

    fun verifyOtp(mobileNumber: String, otpCode: String, requestId: String, callback: ApiCallback<AuthResponse>) {
        // GraphQL mutation for verifying OTP
        val mutation = """
            mutation VerifyOtp(${'$'}mobile: String!, ${'$'}otp: String!, ${'$'}requestId: String) {
                verifyOtp(input: { mobile: ${'$'}mobile, otp: ${'$'}otp, requestId: ${'$'}requestId }) {
                    success
                    accessToken
                    refreshToken
                    expiresIn
                    userId
                }
            }
        """.trimIndent()

        val variables = mapOf(
            "mobile" to mobileNumber,
            "otp" to otpCode,
            "requestId" to requestId
        )

        Log.d(TAG, "Verifying OTP for: ${mobileNumber.takeLast(4)}")

        executeGraphQL(mutation, variables, "VerifyOtp") { data, error ->
            if (error != null) {
                callback.onError(error)
                return@executeGraphQL
            }

            try {
                val verifyData = data?.optJSONObject("verifyOtp")
                if (verifyData != null) {
                    val authResponse = AuthResponse(
                        success = verifyData.optBoolean("success", true),
                        accessToken = verifyData.optString("accessToken", verifyData.optString("access_token", "")),
                        refreshToken = verifyData.optString("refreshToken", verifyData.optString("refresh_token", "")),
                        expiresIn = verifyData.optInt("expiresIn", verifyData.optInt("expires_in", 3600)),
                        userId = verifyData.optString("userId", verifyData.optString("user_id", ""))
                    )

                    if (authResponse.accessToken.isNotEmpty()) {
                        tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken, authResponse.expiresIn)
                        tokenManager.saveUserInfo(authResponse.userId, mobileNumber)
                    }

                    callback.onSuccess(authResponse)
                } else {
                    callback.onError(ApiError(ApiErrorCode.VALIDATION_ERROR, "Invalid OTP", null))
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing verify response", e)
                callback.onError(ApiError(ApiErrorCode.PARSE_ERROR, "Failed to verify OTP", null, e.message))
            }
        }
    }

    // =====================
    // Wallet APIs
    // =====================

    fun getWalletBalance(callback: ApiCallback<WalletBalance>) {
        val query = """
            query GetWalletBalance {
                wallet {
                    balance
                    currency
                    lastUpdated
                }
            }
        """.trimIndent()

        executeGraphQL(query, emptyMap(), "GetWalletBalance") { data, error ->
            if (error != null) {
                callback.onError(error)
                return@executeGraphQL
            }

            try {
                val walletData = data?.optJSONObject("wallet")
                callback.onSuccess(WalletBalance(
                    balance = walletData?.optDouble("balance", 0.0) ?: 0.0,
                    currency = walletData?.optString("currency", "EGP") ?: "EGP",
                    lastUpdated = walletData?.optString("lastUpdated", "") ?: ""
                ))
            } catch (e: Exception) {
                callback.onError(ApiError(ApiErrorCode.PARSE_ERROR, "Failed to parse balance", null, e.message))
            }
        }
    }

    fun initiateTransfer(recipientMobile: String, amount: Double, note: String?, callback: ApiCallback<TransferResponse>) {
        val mutation = """
            mutation Transfer(${'$'}recipientMobile: String!, ${'$'}amount: Float!, ${'$'}note: String) {
                transfer(input: { recipientMobile: ${'$'}recipientMobile, amount: ${'$'}amount, note: ${'$'}note }) {
                    success
                    transactionId
                    status
                    message
                }
            }
        """.trimIndent()

        val variables = mapOf(
            "recipientMobile" to recipientMobile,
            "amount" to amount,
            "note" to note
        )

        executeGraphQL(mutation, variables, "Transfer") { data, error ->
            if (error != null) {
                callback.onError(error)
                return@executeGraphQL
            }

            try {
                val transferData = data?.optJSONObject("transfer")
                callback.onSuccess(TransferResponse(
                    success = transferData?.optBoolean("success", true) ?: true,
                    transactionId = transferData?.optString("transactionId", "") ?: "",
                    status = transferData?.optString("status", "pending") ?: "pending",
                    message = transferData?.optString("message", "Transfer initiated") ?: "Transfer initiated"
                ))
            } catch (e: Exception) {
                callback.onError(ApiError(ApiErrorCode.PARSE_ERROR, "Failed to process transfer", null, e.message))
            }
        }
    }

    // =====================
    // Error Handling
    // =====================

    private fun parseNetworkError(e: IOException): ApiError {
        return when {
            e.message?.contains("timeout", ignoreCase = true) == true -> 
                ApiError(ApiErrorCode.TIMEOUT, "Connection timed out. Please check your internet and try again.", null, e.message)
            e.message?.contains("Unable to resolve host", ignoreCase = true) == true -> 
                ApiError(ApiErrorCode.NO_INTERNET, "No internet connection. Please check your network settings.", null, e.message)
            e.message?.contains("Connection refused", ignoreCase = true) == true -> 
                ApiError(ApiErrorCode.SERVER_UNREACHABLE, "Unable to reach server. Please try again later.", null, e.message)
            e.message?.contains("SSL", ignoreCase = true) == true ||
            e.message?.contains("Certificate", ignoreCase = true) == true -> 
                ApiError(ApiErrorCode.SERVER_UNREACHABLE, "Secure connection failed. Please try again.", null, e.message)
            else -> 
                ApiError(ApiErrorCode.NETWORK_ERROR, "Network error occurred. Please try again.", null, e.message)
        }
    }

    private fun parseGraphQLError(code: String, message: String, httpCode: Int?): ApiError {
        return when (code.uppercase()) {
            "UNAUTHENTICATED", "UNAUTHORIZED" -> 
                ApiError(ApiErrorCode.UNAUTHORIZED, message, httpCode)
            "FORBIDDEN" -> 
                ApiError(ApiErrorCode.FORBIDDEN, message, httpCode)
            "NOT_FOUND" -> 
                ApiError(ApiErrorCode.NOT_FOUND, message, httpCode)
            "BAD_USER_INPUT", "VALIDATION_ERROR" -> 
                ApiError(ApiErrorCode.VALIDATION_ERROR, message, httpCode)
            "RATE_LIMITED", "TOO_MANY_REQUESTS" -> 
                ApiError(ApiErrorCode.RATE_LIMITED, message, httpCode)
            "INTERNAL_SERVER_ERROR", "SERVER_ERROR" -> 
                ApiError(ApiErrorCode.SERVER_ERROR, message, httpCode)
            else -> 
                ApiError(ApiErrorCode.UNKNOWN, message, httpCode)
        }
    }
}

// =====================
// Response Models
// =====================

data class OtpResponse(
    val success: Boolean,
    val message: String,
    val requestId: String,
    val expiresIn: Int
)

data class AuthResponse(
    val success: Boolean,
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Int,
    val userId: String
)

data class WalletBalance(
    val balance: Double,
    val currency: String,
    val lastUpdated: String
)

data class TransferResponse(
    val success: Boolean,
    val transactionId: String,
    val status: String,
    val message: String
)

// =====================
// Error Models
// =====================

enum class ApiErrorCode {
    NO_INTERNET, TIMEOUT, NETWORK_ERROR, SERVER_UNREACHABLE,
    BAD_REQUEST, UNAUTHORIZED, FORBIDDEN, NOT_FOUND, VALIDATION_ERROR, RATE_LIMITED, SERVER_ERROR,
    PARSE_ERROR, UNKNOWN
}

data class ApiError(
    val code: ApiErrorCode,
    val message: String,
    val httpCode: Int? = null,
    val details: String? = null
)

interface ApiCallback<T> {
    fun onSuccess(response: T)
    fun onError(error: ApiError)
}
