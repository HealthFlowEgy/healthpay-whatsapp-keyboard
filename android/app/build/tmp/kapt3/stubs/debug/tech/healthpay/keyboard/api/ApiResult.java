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

@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0010\u000e\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\b6\u0018\u0000*\u0006\b\u0000\u0010\u0001 \u00012\u00020\u0002:\u0002\f\rB\u0007\b\u0004\u00a2\u0006\u0002\u0010\u0003J\b\u0010\b\u001a\u0004\u0018\u00010\tJ\r\u0010\n\u001a\u0004\u0018\u00018\u0000\u00a2\u0006\u0002\u0010\u000bR\u0011\u0010\u0004\u001a\u00020\u00058F\u00a2\u0006\u0006\u001a\u0004\b\u0004\u0010\u0006R\u0011\u0010\u0007\u001a\u00020\u00058F\u00a2\u0006\u0006\u001a\u0004\b\u0007\u0010\u0006\u0082\u0001\u0002\u000e\u000f\u00a8\u0006\u0010"}, d2 = {"Ltech/healthpay/keyboard/api/ApiResult;", "T", "", "()V", "isError", "", "()Z", "isSuccess", "errorMessageOrNull", "", "getOrNull", "()Ljava/lang/Object;", "Error", "Success", "Ltech/healthpay/keyboard/api/ApiResult$Error;", "Ltech/healthpay/keyboard/api/ApiResult$Success;", "app_debug"})
public abstract class ApiResult<T extends java.lang.Object> {
    
    private ApiResult() {
        super();
    }
    
    public final boolean isSuccess() {
        return false;
    }
    
    public final boolean isError() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable
    public final T getOrNull() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String errorMessageOrNull() {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000*\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u0001\n\u0000\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\t\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0002\b\u0003\b\u0086\b\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001B\u0017\u0012\u0006\u0010\u0003\u001a\u00020\u0004\u0012\b\b\u0002\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\u0002\u0010\u0007J\t\u0010\f\u001a\u00020\u0004H\u00c6\u0003J\t\u0010\r\u001a\u00020\u0006H\u00c6\u0003J\u001d\u0010\u000e\u001a\u00020\u00002\b\b\u0002\u0010\u0003\u001a\u00020\u00042\b\b\u0002\u0010\u0005\u001a\u00020\u0006H\u00c6\u0001J\u0013\u0010\u000f\u001a\u00020\u00102\b\u0010\u0011\u001a\u0004\u0018\u00010\u0012H\u00d6\u0003J\t\u0010\u0013\u001a\u00020\u0006H\u00d6\u0001J\t\u0010\u0014\u001a\u00020\u0004H\u00d6\u0001R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\b\u0010\tR\u0011\u0010\u0003\u001a\u00020\u0004\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000b\u00a8\u0006\u0015"}, d2 = {"Ltech/healthpay/keyboard/api/ApiResult$Error;", "Ltech/healthpay/keyboard/api/ApiResult;", "", "message", "", "code", "", "(Ljava/lang/String;I)V", "getCode", "()I", "getMessage", "()Ljava/lang/String;", "component1", "component2", "copy", "equals", "", "other", "", "hashCode", "toString", "app_debug"})
    public static final class Error extends tech.healthpay.keyboard.api.ApiResult {
        @org.jetbrains.annotations.NotNull
        private final java.lang.String message = null;
        private final int code = 0;
        
        public Error(@org.jetbrains.annotations.NotNull
        java.lang.String message, int code) {
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String getMessage() {
            return null;
        }
        
        public final int getCode() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull
        public final java.lang.String component1() {
            return null;
        }
        
        public final int component2() {
            return 0;
        }
        
        @org.jetbrains.annotations.NotNull
        public final tech.healthpay.keyboard.api.ApiResult.Error copy(@org.jetbrains.annotations.NotNull
        java.lang.String message, int code) {
            return null;
        }
        
        @java.lang.Override
        public boolean equals(@org.jetbrains.annotations.Nullable
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public java.lang.String toString() {
            return null;
        }
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u000b\n\u0000\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0000\b\u0086\b\u0018\u0000*\u0004\b\u0001\u0010\u00012\b\u0012\u0004\u0012\u0002H\u00010\u0002B\r\u0012\u0006\u0010\u0003\u001a\u00028\u0001\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\b\u001a\u00028\u0001H\u00c6\u0003\u00a2\u0006\u0002\u0010\u0006J\u001e\u0010\t\u001a\b\u0012\u0004\u0012\u00028\u00010\u00002\b\b\u0002\u0010\u0003\u001a\u00028\u0001H\u00c6\u0001\u00a2\u0006\u0002\u0010\nJ\u0013\u0010\u000b\u001a\u00020\f2\b\u0010\r\u001a\u0004\u0018\u00010\u000eH\u00d6\u0003J\t\u0010\u000f\u001a\u00020\u0010H\u00d6\u0001J\t\u0010\u0011\u001a\u00020\u0012H\u00d6\u0001R\u0013\u0010\u0003\u001a\u00028\u0001\u00a2\u0006\n\n\u0002\u0010\u0007\u001a\u0004\b\u0005\u0010\u0006\u00a8\u0006\u0013"}, d2 = {"Ltech/healthpay/keyboard/api/ApiResult$Success;", "T", "Ltech/healthpay/keyboard/api/ApiResult;", "data", "(Ljava/lang/Object;)V", "getData", "()Ljava/lang/Object;", "Ljava/lang/Object;", "component1", "copy", "(Ljava/lang/Object;)Ltech/healthpay/keyboard/api/ApiResult$Success;", "equals", "", "other", "", "hashCode", "", "toString", "", "app_debug"})
    public static final class Success<T extends java.lang.Object> extends tech.healthpay.keyboard.api.ApiResult<T> {
        private final T data = null;
        
        public Success(T data) {
        }
        
        public final T getData() {
            return null;
        }
        
        public final T component1() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final tech.healthpay.keyboard.api.ApiResult.Success<T> copy(T data) {
            return null;
        }
        
        @java.lang.Override
        public boolean equals(@org.jetbrains.annotations.Nullable
        java.lang.Object other) {
            return false;
        }
        
        @java.lang.Override
        public int hashCode() {
            return 0;
        }
        
        @java.lang.Override
        @org.jetbrains.annotations.NotNull
        public java.lang.String toString() {
            return null;
        }
    }
}