package tech.healthpay.keyboard.api;

import android.content.Context;
import kotlinx.coroutines.Dispatchers;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.http.*;
import tech.healthpay.keyboard.BuildConfig;
import tech.healthpay.keyboard.model.*;
import tech.healthpay.keyboard.security.TokenManager;
import java.util.concurrent.TimeUnit;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * HealthPay API Client
 *
 * Handles all communication with the HealthPay wallet backend.
 * Based on the HealthPay API documentation.
 */
@javax.inject.Singleton
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00a6\u0001\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0006\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\b\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0003\b\u0007\u0018\u0000 E2\u00020\u0001:\u0001EB\u0017\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J+\u0010\u0013\u001a\b\u0012\u0004\u0012\u00020\u00150\u00142\b\u0010\u0016\u001a\u0004\u0018\u00010\u00172\b\u0010\u0018\u001a\u0004\u0018\u00010\u0019H\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u001aJ\u0017\u0010\u001b\u001a\b\u0012\u0004\u0012\u00020\u001c0\u0014H\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u001dJ\u001f\u0010\u001e\u001a\b\u0012\u0004\u0012\u00020\u001f0\u00142\u0006\u0010 \u001a\u00020\u0019H\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010!J+\u0010\"\u001a\b\u0012\u0004\u0012\u00020#0\u00142\b\b\u0002\u0010$\u001a\u00020%2\b\b\u0002\u0010&\u001a\u00020%H\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\'J\'\u0010(\u001a\b\u0012\u0004\u0012\u00020)0\u00142\u0006\u0010*\u001a\u00020\u00192\u0006\u0010+\u001a\u00020\u0019H\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010,J\u0017\u0010-\u001a\b\u0012\u0004\u0012\u00020.0\u0014H\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u001dJ\u0014\u0010/\u001a\u0004\u0018\u00010\u00192\b\u00100\u001a\u0004\u0018\u00010\u0019H\u0002J\'\u00101\u001a\b\u0012\u0004\u0012\u00020\u001f0\u00142\u0006\u00102\u001a\u00020\u00192\u0006\u00103\u001a\u00020\u0019H\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010,J\u001f\u00104\u001a\b\u0012\u0004\u0012\u00020)0\u00142\u0006\u00104\u001a\u00020\u0019H\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010!J\u0012\u00105\u001a\u0004\u0018\u00010)2\u0006\u00104\u001a\u00020\u0019H\u0002J\u001f\u00106\u001a\b\u0012\u0004\u0012\u0002070\u00142\u0006\u00108\u001a\u000209H\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010:JA\u0010;\u001a\b\u0012\u0004\u0012\u0002H<0\u0014\"\u0004\b\u0000\u0010<2\"\u0010=\u001a\u001e\b\u0001\u0012\u0010\u0012\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u0002H<0@0?\u0012\u0006\u0012\u0004\u0018\u00010\u00010>H\u0082@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010AJ\u001f\u0010B\u001a\b\u0012\u0004\u0012\u00020\u001f0\u00142\u0006\u00108\u001a\u00020CH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010DR\u0016\u0010\u0007\u001a\n \t*\u0004\u0018\u00010\b0\bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0010\u001a\n \t*\u0004\u0018\u00010\u00110\u0011X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0012\u001a\u00020\u000bX\u0082\u0004\u00a2\u0006\u0002\n\u0000\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006F"}, d2 = {"Ltech/healthpay/keyboard/api/HealthPayApiClient;", "", "context", "Landroid/content/Context;", "tokenManager", "Ltech/healthpay/keyboard/security/TokenManager;", "(Landroid/content/Context;Ltech/healthpay/keyboard/security/TokenManager;)V", "api", "Ltech/healthpay/keyboard/api/HealthPayApi;", "kotlin.jvm.PlatformType", "authInterceptor", "Lokhttp3/Interceptor;", "loggingInterceptor", "Lokhttp3/logging/HttpLoggingInterceptor;", "okHttpClient", "Lokhttp3/OkHttpClient;", "retrofit", "Lretrofit2/Retrofit;", "tokenRefreshInterceptor", "generatePaymentQR", "Ltech/healthpay/keyboard/api/ApiResult;", "Ltech/healthpay/keyboard/api/QRCodeResponse;", "amount", "", "description", "", "(Ljava/lang/Double;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getBalance", "Ltech/healthpay/keyboard/model/WalletBalance;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getTransactionDetails", "Ltech/healthpay/keyboard/model/Transaction;", "transactionId", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getTransactionHistory", "Ltech/healthpay/keyboard/api/TransactionListResponse;", "page", "", "limit", "(IILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "login", "Ltech/healthpay/keyboard/api/AuthResponse;", "username", "password", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "logout", "", "parseErrorMessage", "errorBody", "processQRPayment", "qrData", "pin", "refreshToken", "refreshTokenSync", "requestPayment", "Ltech/healthpay/keyboard/api/PaymentLink;", "request", "Ltech/healthpay/keyboard/api/RequestPaymentRequest;", "(Ltech/healthpay/keyboard/api/RequestPaymentRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "safeApiCall", "T", "call", "Lkotlin/Function1;", "Lkotlin/coroutines/Continuation;", "Lretrofit2/Response;", "(Lkotlin/jvm/functions/Function1;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "sendPayment", "Ltech/healthpay/keyboard/api/SendPaymentRequest;", "(Ltech/healthpay/keyboard/api/SendPaymentRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "Companion", "app_debug"})
public final class HealthPayApiClient {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private final tech.healthpay.keyboard.security.TokenManager tokenManager = null;
    @org.jetbrains.annotations.NotNull
    public static final java.lang.String BASE_URL = "https://portal.beta.healthpay.tech/api/";
    public static final long TIMEOUT_SECONDS = 30L;
    @org.jetbrains.annotations.NotNull
    private final okhttp3.logging.HttpLoggingInterceptor loggingInterceptor = null;
    @org.jetbrains.annotations.NotNull
    private final okhttp3.Interceptor authInterceptor = null;
    @org.jetbrains.annotations.NotNull
    private final okhttp3.Interceptor tokenRefreshInterceptor = null;
    @org.jetbrains.annotations.NotNull
    private final okhttp3.OkHttpClient okHttpClient = null;
    private final retrofit2.Retrofit retrofit = null;
    private final tech.healthpay.keyboard.api.HealthPayApi api = null;
    @org.jetbrains.annotations.NotNull
    public static final tech.healthpay.keyboard.api.HealthPayApiClient.Companion Companion = null;
    
    @javax.inject.Inject
    public HealthPayApiClient(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.security.TokenManager tokenManager) {
        super();
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object login(@org.jetbrains.annotations.NotNull
    java.lang.String username, @org.jetbrains.annotations.NotNull
    java.lang.String password, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super tech.healthpay.keyboard.api.ApiResult<tech.healthpay.keyboard.api.AuthResponse>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object refreshToken(@org.jetbrains.annotations.NotNull
    java.lang.String refreshToken, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super tech.healthpay.keyboard.api.ApiResult<tech.healthpay.keyboard.api.AuthResponse>> $completion) {
        return null;
    }
    
    private final tech.healthpay.keyboard.api.AuthResponse refreshTokenSync(java.lang.String refreshToken) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object logout(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super tech.healthpay.keyboard.api.ApiResult<kotlin.Unit>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getBalance(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super tech.healthpay.keyboard.api.ApiResult<tech.healthpay.keyboard.model.WalletBalance>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object sendPayment(@org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.api.SendPaymentRequest request, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super tech.healthpay.keyboard.api.ApiResult<tech.healthpay.keyboard.model.Transaction>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object requestPayment(@org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.api.RequestPaymentRequest request, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super tech.healthpay.keyboard.api.ApiResult<tech.healthpay.keyboard.api.PaymentLink>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getTransactionHistory(int page, int limit, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super tech.healthpay.keyboard.api.ApiResult<tech.healthpay.keyboard.api.TransactionListResponse>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getTransactionDetails(@org.jetbrains.annotations.NotNull
    java.lang.String transactionId, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super tech.healthpay.keyboard.api.ApiResult<tech.healthpay.keyboard.model.Transaction>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object generatePaymentQR(@org.jetbrains.annotations.Nullable
    java.lang.Double amount, @org.jetbrains.annotations.Nullable
    java.lang.String description, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super tech.healthpay.keyboard.api.ApiResult<tech.healthpay.keyboard.api.QRCodeResponse>> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object processQRPayment(@org.jetbrains.annotations.NotNull
    java.lang.String qrData, @org.jetbrains.annotations.NotNull
    java.lang.String pin, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super tech.healthpay.keyboard.api.ApiResult<tech.healthpay.keyboard.model.Transaction>> $completion) {
        return null;
    }
    
    private final <T extends java.lang.Object>java.lang.Object safeApiCall(kotlin.jvm.functions.Function1<? super kotlin.coroutines.Continuation<? super retrofit2.Response<T>>, ? extends java.lang.Object> call, kotlin.coroutines.Continuation<? super tech.healthpay.keyboard.api.ApiResult<? extends T>> $completion) {
        return null;
    }
    
    private final java.lang.String parseErrorMessage(java.lang.String errorBody) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0018\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\t\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0007"}, d2 = {"Ltech/healthpay/keyboard/api/HealthPayApiClient$Companion;", "", "()V", "BASE_URL", "", "TIMEOUT_SECONDS", "", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}