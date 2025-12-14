package tech.healthpay.keyboard.api

import android.content.Context
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import tech.healthpay.keyboard.BuildConfig
import tech.healthpay.keyboard.model.*
import tech.healthpay.keyboard.security.TokenManager
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Singleton

/**
 * HealthPay API Client
 * 
 * Handles all communication with the HealthPay wallet backend.
 * Based on the HealthPay API documentation.
 */
@Singleton
class HealthPayApiClient @Inject constructor(
    private val context: Context,
    private val tokenManager: TokenManager
) {
    companion object {
        const val BASE_URL = "https://portal.beta.healthpay.tech/api/"
        const val TIMEOUT_SECONDS = 30L
    }

    // ==================== Retrofit Setup ====================

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = if (BuildConfig.DEBUG) {
            HttpLoggingInterceptor.Level.BODY
        } else {
            HttpLoggingInterceptor.Level.NONE
        }
    }

    private val authInterceptor = Interceptor { chain ->
        val originalRequest = chain.request()
        val token = tokenManager.getAccessToken()
        
        val newRequest = if (token != null) {
            originalRequest.newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .addHeader("Content-Type", "application/json")
                .addHeader("Accept", "application/json")
                .addHeader("X-Device-Platform", "Android")
                .addHeader("X-App-Version", BuildConfig.VERSION_NAME)
                .build()
        } else {
            originalRequest
        }
        
        chain.proceed(newRequest)
    }

    private val tokenRefreshInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        
        if (response.code == 401) {
            synchronized(this) {
                // Try to refresh token
                val refreshToken = tokenManager.getRefreshToken()
                if (refreshToken != null) {
                    val newTokens = refreshTokenSync(refreshToken)
                    if (newTokens != null) {
                        tokenManager.saveTokens(newTokens.accessToken, newTokens.refreshToken)
                        
                        // Retry original request with new token
                        val newRequest = chain.request().newBuilder()
                            .removeHeader("Authorization")
                            .addHeader("Authorization", "Bearer ${newTokens.accessToken}")
                            .build()
                        
                        response.close()
                        return@Interceptor chain.proceed(newRequest)
                    }
                }
            }
        }
        
        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .addInterceptor(loggingInterceptor)
        .addInterceptor(authInterceptor)
        .addInterceptor(tokenRefreshInterceptor)
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val api = retrofit.create(HealthPayApi::class.java)

    // ==================== Auth Methods ====================

    suspend fun login(username: String, password: String): ApiResult<AuthResponse> {
        return safeApiCall {
            api.login(LoginRequest(username, password))
        }
    }

    suspend fun refreshToken(refreshToken: String): ApiResult<AuthResponse> {
        return safeApiCall {
            api.refreshToken(RefreshTokenRequest(refreshToken))
        }
    }

    private fun refreshTokenSync(refreshToken: String): AuthResponse? {
        return try {
            val call = api.refreshTokenSync(RefreshTokenRequest(refreshToken))
            val response = call.execute()
            if (response.isSuccessful) response.body() else null
        } catch (e: Exception) {
            null
        }
    }

    suspend fun logout(): ApiResult<Unit> {
        return safeApiCall {
            api.logout()
        }
    }

    // ==================== Wallet Methods ====================

    suspend fun getBalance(): ApiResult<WalletBalance> {
        return safeApiCall {
            api.getBalance()
        }
    }

    suspend fun sendPayment(request: SendPaymentRequest): ApiResult<Transaction> {
        return safeApiCall {
            api.sendPayment(request)
        }
    }

    suspend fun requestPayment(request: RequestPaymentRequest): ApiResult<PaymentLink> {
        return safeApiCall {
            api.requestPayment(request)
        }
    }

    suspend fun getTransactionHistory(
        page: Int = 1,
        limit: Int = 20
    ): ApiResult<TransactionListResponse> {
        return safeApiCall {
            api.getTransactions(page, limit)
        }
    }

    suspend fun getTransactionDetails(transactionId: String): ApiResult<Transaction> {
        return safeApiCall {
            api.getTransactionDetails(transactionId)
        }
    }

    // ==================== QR Methods ====================

    suspend fun generatePaymentQR(amount: Double?, description: String?): ApiResult<QRCodeResponse> {
        return safeApiCall {
            api.generatePaymentQR(GenerateQRRequest(amount, description))
        }
    }

    suspend fun processQRPayment(qrData: String, pin: String): ApiResult<Transaction> {
        return safeApiCall {
            api.processQRPayment(ProcessQRRequest(qrData, pin))
        }
    }

    // ==================== Helper Methods ====================

    private suspend fun <T> safeApiCall(call: suspend () -> Response<T>): ApiResult<T> {
        return withContext(Dispatchers.IO) {
            try {
                val response = call()
                
                if (response.isSuccessful) {
                    response.body()?.let {
                        ApiResult.Success(it)
                    } ?: ApiResult.Error("Empty response body")
                } else {
                    val errorBody = response.errorBody()?.string()
                    val errorMessage = parseErrorMessage(errorBody) ?: "Unknown error"
                    ApiResult.Error(errorMessage, response.code())
                }
            } catch (e: Exception) {
                ApiResult.Error(e.message ?: "Network error", -1)
            }
        }
    }

    private fun parseErrorMessage(errorBody: String?): String? {
        return try {
            // Parse error JSON and extract message
            errorBody?.let {
                com.google.gson.Gson().fromJson(it, ErrorResponse::class.java)?.message
            }
        } catch (e: Exception) {
            null
        }
    }
}

// ==================== API Interface ====================

interface HealthPayApi {
    
    // Auth
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequest): Response<AuthResponse>

    @POST("auth/refresh")
    suspend fun refreshToken(@Body request: RefreshTokenRequest): Response<AuthResponse>

    @POST("auth/refresh")
    fun refreshTokenSync(@Body request: RefreshTokenRequest): retrofit2.Call<AuthResponse>

    @POST("auth/logout")
    suspend fun logout(): Response<Unit>

    // Wallet
    @GET("wallet/balance")
    suspend fun getBalance(): Response<WalletBalance>

    @POST("wallet/send")
    suspend fun sendPayment(@Body request: SendPaymentRequest): Response<Transaction>

    @POST("wallet/request")
    suspend fun requestPayment(@Body request: RequestPaymentRequest): Response<PaymentLink>

    @GET("wallet/transactions")
    suspend fun getTransactions(
        @Query("page") page: Int,
        @Query("limit") limit: Int
    ): Response<TransactionListResponse>

    @GET("wallet/transactions/{id}")
    suspend fun getTransactionDetails(@Path("id") id: String): Response<Transaction>

    // QR
    @POST("qr/generate")
    suspend fun generatePaymentQR(@Body request: GenerateQRRequest): Response<QRCodeResponse>

    @POST("qr/process")
    suspend fun processQRPayment(@Body request: ProcessQRRequest): Response<Transaction>
}

// ==================== Request/Response Models ====================

data class LoginRequest(
    val username: String,
    val password: String
)

data class RefreshTokenRequest(
    val refreshToken: String
)

data class AuthResponse(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
    val user: UserInfo
)

data class UserInfo(
    val id: String,
    val name: String,
    val phone: String,
    val email: String?
)

data class SendPaymentRequest(
    val amount: Double,
    val recipientPhone: String,
    val description: String? = null,
    val pin: String? = null // Encrypted
)

data class RequestPaymentRequest(
    val amount: Double,
    val description: String? = null,
    val expiresIn: Int = 24 // Hours
)

data class PaymentLink(
    val link: String,
    val qrCode: String,
    val expiresAt: String
)

data class TransactionListResponse(
    val transactions: List<Transaction>,
    val total: Int,
    val page: Int,
    val hasMore: Boolean
)

data class GenerateQRRequest(
    val amount: Double?,
    val description: String?
)

data class QRCodeResponse(
    val qrData: String,
    val qrImage: String, // Base64 encoded
    val expiresAt: String
)

data class ProcessQRRequest(
    val qrData: String,
    val pin: String // Encrypted
)

data class ErrorResponse(
    val message: String?,
    val code: String?,
    val errors: Map<String, List<String>>?
)

// ==================== API Result Wrapper ====================

sealed class ApiResult<out T> {
    data class Success<T>(val data: T) : ApiResult<T>()
    data class Error(val message: String, val code: Int = -1) : ApiResult<Nothing>()
    
    val isSuccess: Boolean get() = this is Success
    val isError: Boolean get() = this is Error
    
    fun getOrNull(): T? = (this as? Success)?.data
    fun errorMessageOrNull(): String? = (this as? Error)?.message
}
