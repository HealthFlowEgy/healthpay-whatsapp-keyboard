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
 * HealthPay API Client
 * 
 * v1.2.1 - Complete implementation with robust error handling
 */
class HealthPayApiClient(private val tokenManager: TokenManager) {

    companion object {
        private const val TAG = "HealthPayApiClient"
        private const val BASE_URL = "https://portal.beta.healthpay.tech/api/v1"
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
    // Authentication APIs
    // =====================

    fun requestOtp(mobileNumber: String, callback: ApiCallback<OtpResponse>) {
        val jsonBody = JSONObject().apply {
            put("mobile", mobileNumber)
            put("country_code", "+20")
        }

        val request = Request.Builder()
            .url("$BASE_URL/auth/otp/request")
            .post(jsonBody.toString().toRequestBody(jsonMediaType))
            .build()

        Log.d(TAG, "Requesting OTP for: ${mobileNumber.takeLast(4)}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "OTP request network failure", e)
                callback.onError(parseNetworkError(e))
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string()
                    Log.d(TAG, "OTP response code: ${response.code}")
                    
                    if (response.isSuccessful && responseBody != null) {
                        val json = JSONObject(responseBody)
                        callback.onSuccess(OtpResponse(
                            success = json.optBoolean("success", true),
                            message = json.optString("message", "OTP sent successfully"),
                            requestId = json.optString("request_id", ""),
                            expiresIn = json.optInt("expires_in", 300)
                        ))
                    } else {
                        callback.onError(parseApiError(response.code, responseBody))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "OTP response parsing error", e)
                    callback.onError(ApiError(ApiErrorCode.PARSE_ERROR, "Failed to process server response", null, e.message))
                }
            }
        })
    }

    fun verifyOtp(mobileNumber: String, otpCode: String, requestId: String, callback: ApiCallback<AuthResponse>) {
        val jsonBody = JSONObject().apply {
            put("mobile", mobileNumber)
            put("otp", otpCode)
            put("request_id", requestId)
        }

        val request = Request.Builder()
            .url("$BASE_URL/auth/otp/verify")
            .post(jsonBody.toString().toRequestBody(jsonMediaType))
            .build()

        Log.d(TAG, "Verifying OTP for: ${mobileNumber.takeLast(4)}")

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e(TAG, "OTP verify network failure", e)
                callback.onError(parseNetworkError(e))
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string()
                    
                    if (response.isSuccessful && responseBody != null) {
                        val json = JSONObject(responseBody)
                        val authResponse = AuthResponse(
                            success = true,
                            accessToken = json.optString("access_token"),
                            refreshToken = json.optString("refresh_token"),
                            expiresIn = json.optInt("expires_in", 3600),
                            userId = json.optString("user_id")
                        )
                        tokenManager.saveTokens(authResponse.accessToken, authResponse.refreshToken, authResponse.expiresIn)
                        callback.onSuccess(authResponse)
                    } else {
                        callback.onError(parseApiError(response.code, responseBody))
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "OTP verify parsing error", e)
                    callback.onError(ApiError(ApiErrorCode.PARSE_ERROR, "Failed to process verification response", null, e.message))
                }
            }
        })
    }

    // =====================
    // Wallet APIs
    // =====================

    fun getWalletBalance(callback: ApiCallback<WalletBalance>) {
        val request = Request.Builder()
            .url("$BASE_URL/wallet/balance")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError(parseNetworkError(e))
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string()
                    if (response.isSuccessful && responseBody != null) {
                        val json = JSONObject(responseBody)
                        callback.onSuccess(WalletBalance(
                            balance = json.optDouble("balance", 0.0),
                            currency = json.optString("currency", "EGP"),
                            lastUpdated = json.optString("last_updated", "")
                        ))
                    } else {
                        callback.onError(parseApiError(response.code, responseBody))
                    }
                } catch (e: Exception) {
                    callback.onError(ApiError(ApiErrorCode.PARSE_ERROR, "Failed to parse balance", null, e.message))
                }
            }
        })
    }

    fun initiateTransfer(recipientMobile: String, amount: Double, note: String?, callback: ApiCallback<TransferResponse>) {
        val jsonBody = JSONObject().apply {
            put("recipient_mobile", recipientMobile)
            put("amount", amount)
            put("currency", "EGP")
            note?.let { put("note", it) }
        }

        val request = Request.Builder()
            .url("$BASE_URL/wallet/transfer")
            .post(jsonBody.toString().toRequestBody(jsonMediaType))
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                callback.onError(parseNetworkError(e))
            }

            override fun onResponse(call: Call, response: Response) {
                try {
                    val responseBody = response.body?.string()
                    if (response.isSuccessful && responseBody != null) {
                        val json = JSONObject(responseBody)
                        callback.onSuccess(TransferResponse(
                            success = true,
                            transactionId = json.optString("transaction_id"),
                            status = json.optString("status", "pending"),
                            message = json.optString("message", "Transfer initiated")
                        ))
                    } else {
                        callback.onError(parseApiError(response.code, responseBody))
                    }
                } catch (e: Exception) {
                    callback.onError(ApiError(ApiErrorCode.PARSE_ERROR, "Failed to process transfer", null, e.message))
                }
            }
        })
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
            else -> 
                ApiError(ApiErrorCode.NETWORK_ERROR, "Network error occurred. Please try again.", null, e.message)
        }
    }

    private fun parseApiError(statusCode: Int, responseBody: String?): ApiError {
        val serverMessage = try {
            responseBody?.let { JSONObject(it).optString("message") ?: JSONObject(it).optString("error") }
        } catch (e: Exception) { null }

        return when (statusCode) {
            400 -> ApiError(ApiErrorCode.BAD_REQUEST, serverMessage ?: "Invalid request. Please check your input.", statusCode)
            401 -> ApiError(ApiErrorCode.UNAUTHORIZED, serverMessage ?: "Session expired. Please login again.", statusCode)
            403 -> ApiError(ApiErrorCode.FORBIDDEN, serverMessage ?: "You don't have permission for this action.", statusCode)
            404 -> ApiError(ApiErrorCode.NOT_FOUND, serverMessage ?: "Service not found.", statusCode)
            422 -> ApiError(ApiErrorCode.VALIDATION_ERROR, serverMessage ?: "Invalid mobile number format.", statusCode)
            429 -> ApiError(ApiErrorCode.RATE_LIMITED, serverMessage ?: "Too many requests. Please wait a moment.", statusCode)
            in 500..599 -> ApiError(ApiErrorCode.SERVER_ERROR, serverMessage ?: "Server error. Please try again later.", statusCode)
            else -> ApiError(ApiErrorCode.UNKNOWN, serverMessage ?: "An unexpected error occurred.", statusCode)
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
