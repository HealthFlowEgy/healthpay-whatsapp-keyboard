package tech.healthpay.keyboard.api

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import tech.healthpay.keyboard.model.OtpRequestResponse
import tech.healthpay.keyboard.model.OtpVerifyResponse
import tech.healthpay.keyboard.model.WalletBalance
import tech.healthpay.keyboard.security.TokenManager
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL

/**
 * HealthPay API Client
 * 
 * Handles all API communication with the HealthPay backend.
 */
class HealthPayApiClient(
    private val tokenManager: TokenManager
) {
    companion object {
        private const val TAG = "HealthPayApiClient"
        
        // API Base URLs
        private const val BASE_URL = "https://api.beta.healthpay.tech"
        private const val GRAPHQL_URL = "https://sword.beta.healthpay.tech/graphql"
        
        // Endpoints
        private const val ENDPOINT_REQUEST_OTP = "/auth/otp/request"
        private const val ENDPOINT_VERIFY_OTP = "/auth/otp/verify"
        private const val ENDPOINT_WALLET_BALANCE = "/wallet/balance"
        private const val ENDPOINT_SEND_MONEY = "/wallet/send"
        private const val ENDPOINT_REQUEST_MONEY = "/wallet/request"
        
        // Timeouts
        private const val CONNECT_TIMEOUT = 30000
        private const val READ_TIMEOUT = 30000
    }

    /**
     * Request OTP for phone number verification.
     */
    suspend fun requestOtp(phoneNumber: String): Result<OtpRequestResponse> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL$ENDPOINT_REQUEST_OTP")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT
                readTimeout = READ_TIMEOUT
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }

            val requestBody = JSONObject().apply {
                put("phone", normalizePhoneNumber(phoneNumber))
                put("country_code", "+20")
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            Log.d(TAG, "requestOtp response code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = readResponse(connection)
                val json = JSONObject(response)
                
                Result.success(OtpRequestResponse(
                    requestId = json.getString("request_id"),
                    expiresIn = json.optInt("expires_in", 300),
                    message = json.optString("message", "OTP sent successfully")
                ))
            } else {
                val error = readErrorResponse(connection)
                Result.failure(Exception("Failed to send OTP: $error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error requesting OTP", e)
            Result.failure(e)
        }
    }

    /**
     * Verify OTP and get access token.
     */
    suspend fun verifyOtp(
        phoneNumber: String,
        otpCode: String,
        requestId: String
    ): Result<OtpVerifyResponse> = withContext(Dispatchers.IO) {
        try {
            val url = URL("$BASE_URL$ENDPOINT_VERIFY_OTP")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT
                readTimeout = READ_TIMEOUT
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }

            val requestBody = JSONObject().apply {
                put("phone", normalizePhoneNumber(phoneNumber))
                put("otp", otpCode)
                put("request_id", requestId)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            Log.d(TAG, "verifyOtp response code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = readResponse(connection)
                val json = JSONObject(response)
                
                Result.success(OtpVerifyResponse(
                    accessToken = json.getString("access_token"),
                    refreshToken = json.optString("refresh_token", null),
                    expiresIn = json.optInt("expires_in", 3600),
                    userId = json.optString("user_id", null)
                ))
            } else {
                val error = readErrorResponse(connection)
                Result.failure(Exception("OTP verification failed: $error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error verifying OTP", e)
            Result.failure(e)
        }
    }

    /**
     * Get wallet balance.
     */
    suspend fun getWalletBalance(): Result<WalletBalance> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getAccessToken()
            if (token == null) {
                return@withContext Result.failure(Exception("Not authenticated"))
            }

            val url = URL("$BASE_URL$ENDPOINT_WALLET_BALANCE")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "GET"
                connectTimeout = CONNECT_TIMEOUT
                readTimeout = READ_TIMEOUT
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Content-Type", "application/json")
            }

            val responseCode = connection.responseCode
            Log.d(TAG, "getWalletBalance response code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = readResponse(connection)
                val json = JSONObject(response)
                
                Result.success(WalletBalance(
                    available = json.getDouble("available"),
                    pending = json.optDouble("pending", 0.0),
                    currency = json.optString("currency", "EGP")
                ))
            } else if (responseCode == HttpURLConnection.HTTP_UNAUTHORIZED) {
                // Token expired - try to refresh
                val refreshed = refreshToken()
                if (refreshed) {
                    // Retry the request
                    return@withContext getWalletBalance()
                }
                Result.failure(Exception("Session expired. Please login again."))
            } else {
                val error = readErrorResponse(connection)
                Result.failure(Exception("Failed to get balance: $error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error getting wallet balance", e)
            Result.failure(e)
        }
    }

    /**
     * Send money to a phone number.
     */
    suspend fun sendMoney(
        recipientPhone: String,
        amount: Double,
        note: String? = null
    ): Result<TransactionResponse> = withContext(Dispatchers.IO) {
        try {
            val token = tokenManager.getAccessToken()
            if (token == null) {
                return@withContext Result.failure(Exception("Not authenticated"))
            }

            val url = URL("$BASE_URL$ENDPOINT_SEND_MONEY")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT
                readTimeout = READ_TIMEOUT
                setRequestProperty("Authorization", "Bearer $token")
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }

            val requestBody = JSONObject().apply {
                put("recipient_phone", normalizePhoneNumber(recipientPhone))
                put("amount", amount)
                put("currency", "EGP")
                if (!note.isNullOrBlank()) {
                    put("note", note)
                }
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            Log.d(TAG, "sendMoney response code: $responseCode")

            if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_CREATED) {
                val response = readResponse(connection)
                val json = JSONObject(response)
                
                Result.success(TransactionResponse(
                    transactionId = json.getString("transaction_id"),
                    status = json.getString("status"),
                    amount = json.getDouble("amount"),
                    currency = json.optString("currency", "EGP"),
                    reference = json.optString("reference", null)
                ))
            } else {
                val error = readErrorResponse(connection)
                Result.failure(Exception("Transfer failed: $error"))
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error sending money", e)
            Result.failure(e)
        }
    }

    /**
     * Refresh access token using refresh token.
     */
    private suspend fun refreshToken(): Boolean = withContext(Dispatchers.IO) {
        try {
            val refreshToken = tokenManager.getRefreshToken()
            if (refreshToken == null) {
                return@withContext false
            }

            val url = URL("$BASE_URL/auth/refresh")
            val connection = url.openConnection() as HttpURLConnection
            
            connection.apply {
                requestMethod = "POST"
                connectTimeout = CONNECT_TIMEOUT
                readTimeout = READ_TIMEOUT
                setRequestProperty("Content-Type", "application/json")
                doOutput = true
            }

            val requestBody = JSONObject().apply {
                put("refresh_token", refreshToken)
            }

            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(requestBody.toString())
                writer.flush()
            }

            val responseCode = connection.responseCode
            if (responseCode == HttpURLConnection.HTTP_OK) {
                val response = readResponse(connection)
                val json = JSONObject(response)
                
                tokenManager.saveAccessToken(json.getString("access_token"))
                json.optString("refresh_token", null)?.let {
                    tokenManager.saveRefreshToken(it)
                }
                true
            } else {
                false
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error refreshing token", e)
            false
        }
    }

    private fun normalizePhoneNumber(phone: String): String {
        var normalized = phone.replace(Regex("[^0-9+]"), "")
        
        // Convert Egyptian numbers to international format
        if (normalized.startsWith("01")) {
            normalized = "+20" + normalized.substring(1)
        } else if (normalized.startsWith("1") && normalized.length == 10) {
            normalized = "+20$normalized"
        }
        
        return normalized
    }

    private fun readResponse(connection: HttpURLConnection): String {
        return BufferedReader(InputStreamReader(connection.inputStream)).use { reader ->
            reader.readText()
        }
    }

    private fun readErrorResponse(connection: HttpURLConnection): String {
        return try {
            BufferedReader(InputStreamReader(connection.errorStream)).use { reader ->
                val response = reader.readText()
                try {
                    val json = JSONObject(response)
                    json.optString("message", json.optString("error", response))
                } catch (e: Exception) {
                    response
                }
            }
        } catch (e: Exception) {
            "Unknown error"
        }
    }

    /**
     * Transaction response data class.
     */
    data class TransactionResponse(
        val transactionId: String,
        val status: String,
        val amount: Double,
        val currency: String,
        val reference: String?
    )
}
