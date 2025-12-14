package tech.healthpay.keyboard.model;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Payment Result
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000&\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0010\n\u0002\u0010\b\n\u0002\b\u0003\b\u0086\b\u0018\u0000 \u001a2\u00020\u0001:\u0001\u001aB1\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u0012\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\u0002\u0010\tJ\t\u0010\u0010\u001a\u00020\u0003H\u00c6\u0003J\u000b\u0010\u0011\u001a\u0004\u0018\u00010\u0005H\u00c6\u0003J\u000b\u0010\u0012\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J\u000b\u0010\u0013\u001a\u0004\u0018\u00010\u0007H\u00c6\u0003J7\u0010\u0014\u001a\u00020\u00002\b\b\u0002\u0010\u0002\u001a\u00020\u00032\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u00052\n\b\u0002\u0010\u0006\u001a\u0004\u0018\u00010\u00072\n\b\u0002\u0010\b\u001a\u0004\u0018\u00010\u0007H\u00c6\u0001J\u0013\u0010\u0015\u001a\u00020\u00032\b\u0010\u0016\u001a\u0004\u0018\u00010\u0001H\u00d6\u0003J\t\u0010\u0017\u001a\u00020\u0018H\u00d6\u0001J\t\u0010\u0019\u001a\u00020\u0007H\u00d6\u0001R\u0013\u0010\b\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\u000bR\u0013\u0010\u0006\u001a\u0004\u0018\u00010\u0007\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\u000bR\u0011\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0002\u0010\rR\u0013\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000f\u00a8\u0006\u001b"}, d2 = {"Ltech/healthpay/keyboard/model/PaymentResult;", "", "isSuccess", "", "transaction", "Ltech/healthpay/keyboard/model/Transaction;", "errorMessage", "", "errorCode", "(ZLtech/healthpay/keyboard/model/Transaction;Ljava/lang/String;Ljava/lang/String;)V", "getErrorCode", "()Ljava/lang/String;", "getErrorMessage", "()Z", "getTransaction", "()Ltech/healthpay/keyboard/model/Transaction;", "component1", "component2", "component3", "component4", "copy", "equals", "other", "hashCode", "", "toString", "Companion", "app_debug"})
public final class PaymentResult {
    private final boolean isSuccess = false;
    @org.jetbrains.annotations.Nullable
    private final tech.healthpay.keyboard.model.Transaction transaction = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String errorMessage = null;
    @org.jetbrains.annotations.Nullable
    private final java.lang.String errorCode = null;
    @org.jetbrains.annotations.NotNull
    public static final tech.healthpay.keyboard.model.PaymentResult.Companion Companion = null;
    
    public PaymentResult(boolean isSuccess, @org.jetbrains.annotations.Nullable
    tech.healthpay.keyboard.model.Transaction transaction, @org.jetbrains.annotations.Nullable
    java.lang.String errorMessage, @org.jetbrains.annotations.Nullable
    java.lang.String errorCode) {
        super();
    }
    
    public final boolean isSuccess() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable
    public final tech.healthpay.keyboard.model.Transaction getTransaction() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getErrorMessage() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String getErrorCode() {
        return null;
    }
    
    public final boolean component1() {
        return false;
    }
    
    @org.jetbrains.annotations.Nullable
    public final tech.healthpay.keyboard.model.Transaction component2() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component3() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.String component4() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final tech.healthpay.keyboard.model.PaymentResult copy(boolean isSuccess, @org.jetbrains.annotations.Nullable
    tech.healthpay.keyboard.model.Transaction transaction, @org.jetbrains.annotations.Nullable
    java.lang.String errorMessage, @org.jetbrains.annotations.Nullable
    java.lang.String errorCode) {
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
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002J\u001a\u0010\u0003\u001a\u00020\u00042\u0006\u0010\u0005\u001a\u00020\u00062\n\b\u0002\u0010\u0007\u001a\u0004\u0018\u00010\u0006J\u000e\u0010\b\u001a\u00020\u00042\u0006\u0010\t\u001a\u00020\n\u00a8\u0006\u000b"}, d2 = {"Ltech/healthpay/keyboard/model/PaymentResult$Companion;", "", "()V", "error", "Ltech/healthpay/keyboard/model/PaymentResult;", "message", "", "code", "success", "transaction", "Ltech/healthpay/keyboard/model/Transaction;", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
        
        @org.jetbrains.annotations.NotNull
        public final tech.healthpay.keyboard.model.PaymentResult success(@org.jetbrains.annotations.NotNull
        tech.healthpay.keyboard.model.Transaction transaction) {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final tech.healthpay.keyboard.model.PaymentResult error(@org.jetbrains.annotations.NotNull
        java.lang.String message, @org.jetbrains.annotations.Nullable
        java.lang.String code) {
            return null;
        }
    }
}