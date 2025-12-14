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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000|\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\bf\u0018\u00002\u00020\u0001J!\u0010\u0002\u001a\b\u0012\u0004\u0012\u00020\u00040\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0006H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0007J\u0017\u0010\b\u001a\b\u0012\u0004\u0012\u00020\t0\u0003H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\nJ!\u0010\u000b\u001a\b\u0012\u0004\u0012\u00020\f0\u00032\b\b\u0001\u0010\r\u001a\u00020\u000eH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u000fJ+\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u00110\u00032\b\b\u0001\u0010\u0012\u001a\u00020\u00132\b\b\u0001\u0010\u0014\u001a\u00020\u0013H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0015J!\u0010\u0016\u001a\b\u0012\u0004\u0012\u00020\u00170\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u0018H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0019J\u0017\u0010\u001a\u001a\b\u0012\u0004\u0012\u00020\u001b0\u0003H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\nJ!\u0010\u001c\u001a\b\u0012\u0004\u0012\u00020\f0\u00032\b\b\u0001\u0010\u0005\u001a\u00020\u001dH\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u001eJ!\u0010\u001f\u001a\b\u0012\u0004\u0012\u00020\u00170\u00032\b\b\u0001\u0010\u0005\u001a\u00020 H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010!J\u0018\u0010\"\u001a\b\u0012\u0004\u0012\u00020\u00170#2\b\b\u0001\u0010\u0005\u001a\u00020 H\'J!\u0010$\u001a\b\u0012\u0004\u0012\u00020%0\u00032\b\b\u0001\u0010\u0005\u001a\u00020&H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\'J!\u0010(\u001a\b\u0012\u0004\u0012\u00020\f0\u00032\b\b\u0001\u0010\u0005\u001a\u00020)H\u00a7@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010*\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006+"}, d2 = {"Ltech/healthpay/keyboard/api/HealthPayApi;", "", "generatePaymentQR", "Lretrofit2/Response;", "Ltech/healthpay/keyboard/api/QRCodeResponse;", "request", "Ltech/healthpay/keyboard/api/GenerateQRRequest;", "(Ltech/healthpay/keyboard/api/GenerateQRRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getBalance", "Ltech/healthpay/keyboard/model/WalletBalance;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getTransactionDetails", "Ltech/healthpay/keyboard/model/Transaction;", "id", "", "(Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getTransactions", "Ltech/healthpay/keyboard/api/TransactionListResponse;", "page", "", "limit", "(IILkotlin/coroutines/Continuation;)Ljava/lang/Object;", "login", "Ltech/healthpay/keyboard/api/AuthResponse;", "Ltech/healthpay/keyboard/api/LoginRequest;", "(Ltech/healthpay/keyboard/api/LoginRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "logout", "", "processQRPayment", "Ltech/healthpay/keyboard/api/ProcessQRRequest;", "(Ltech/healthpay/keyboard/api/ProcessQRRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "refreshToken", "Ltech/healthpay/keyboard/api/RefreshTokenRequest;", "(Ltech/healthpay/keyboard/api/RefreshTokenRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "refreshTokenSync", "Lretrofit2/Call;", "requestPayment", "Ltech/healthpay/keyboard/api/PaymentLink;", "Ltech/healthpay/keyboard/api/RequestPaymentRequest;", "(Ltech/healthpay/keyboard/api/RequestPaymentRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "sendPayment", "Ltech/healthpay/keyboard/api/SendPaymentRequest;", "(Ltech/healthpay/keyboard/api/SendPaymentRequest;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface HealthPayApi {
    
    @retrofit2.http.POST(value = "auth/login")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object login(@retrofit2.http.Body
    @org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.api.LoginRequest request, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super retrofit2.Response<tech.healthpay.keyboard.api.AuthResponse>> $completion);
    
    @retrofit2.http.POST(value = "auth/refresh")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object refreshToken(@retrofit2.http.Body
    @org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.api.RefreshTokenRequest request, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super retrofit2.Response<tech.healthpay.keyboard.api.AuthResponse>> $completion);
    
    @retrofit2.http.POST(value = "auth/refresh")
    @org.jetbrains.annotations.NotNull
    public abstract retrofit2.Call<tech.healthpay.keyboard.api.AuthResponse> refreshTokenSync(@retrofit2.http.Body
    @org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.api.RefreshTokenRequest request);
    
    @retrofit2.http.POST(value = "auth/logout")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object logout(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super retrofit2.Response<kotlin.Unit>> $completion);
    
    @retrofit2.http.GET(value = "wallet/balance")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getBalance(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super retrofit2.Response<tech.healthpay.keyboard.model.WalletBalance>> $completion);
    
    @retrofit2.http.POST(value = "wallet/send")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object sendPayment(@retrofit2.http.Body
    @org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.api.SendPaymentRequest request, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super retrofit2.Response<tech.healthpay.keyboard.model.Transaction>> $completion);
    
    @retrofit2.http.POST(value = "wallet/request")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object requestPayment(@retrofit2.http.Body
    @org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.api.RequestPaymentRequest request, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super retrofit2.Response<tech.healthpay.keyboard.api.PaymentLink>> $completion);
    
    @retrofit2.http.GET(value = "wallet/transactions")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getTransactions(@retrofit2.http.Query(value = "page")
    int page, @retrofit2.http.Query(value = "limit")
    int limit, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super retrofit2.Response<tech.healthpay.keyboard.api.TransactionListResponse>> $completion);
    
    @retrofit2.http.GET(value = "wallet/transactions/{id}")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getTransactionDetails(@retrofit2.http.Path(value = "id")
    @org.jetbrains.annotations.NotNull
    java.lang.String id, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super retrofit2.Response<tech.healthpay.keyboard.model.Transaction>> $completion);
    
    @retrofit2.http.POST(value = "qr/generate")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object generatePaymentQR(@retrofit2.http.Body
    @org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.api.GenerateQRRequest request, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super retrofit2.Response<tech.healthpay.keyboard.api.QRCodeResponse>> $completion);
    
    @retrofit2.http.POST(value = "qr/process")
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object processQRPayment(@retrofit2.http.Body
    @org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.api.ProcessQRRequest request, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super retrofit2.Response<tech.healthpay.keyboard.model.Transaction>> $completion);
}