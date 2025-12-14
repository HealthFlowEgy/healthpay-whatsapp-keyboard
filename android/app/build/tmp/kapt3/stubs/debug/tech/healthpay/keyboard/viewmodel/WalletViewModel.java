package tech.healthpay.keyboard.viewmodel;

import androidx.lifecycle.ViewModel;
import dagger.hilt.android.lifecycle.HiltViewModel;
import kotlinx.coroutines.flow.StateFlow;
import tech.healthpay.keyboard.api.ApiResult;
import tech.healthpay.keyboard.api.HealthPayApiClient;
import tech.healthpay.keyboard.api.SendPaymentRequest;
import tech.healthpay.keyboard.api.RequestPaymentRequest;
import tech.healthpay.keyboard.model.*;
import tech.healthpay.keyboard.security.AuthenticationManager;
import tech.healthpay.keyboard.security.EncryptionManager;
import javax.inject.Inject;

/**
 * ViewModel for Wallet operations
 *
 * Manages:
 * - Wallet balance
 * - Transactions
 * - Payments (send/request)
 * - QR operations
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000n\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\u0006\n\u0002\b\b\n\u0002\u0010\b\n\u0002\b\f\b\u0007\u0018\u00002\u00020\u0001B\u001f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\u0006\u0010\u0004\u001a\u00020\u0005\u0012\u0006\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\b\u0010*\u001a\u00020+H\u0002J\u0006\u0010,\u001a\u00020+J\u0006\u0010-\u001a\u00020+J%\u0010.\u001a\u00020\u000f2\u0006\u0010/\u001a\u0002002\n\b\u0002\u00101\u001a\u0004\u0018\u00010\u000fH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u00102J)\u00103\u001a\u00020\u000f2\n\b\u0002\u0010/\u001a\u0004\u0018\u0001002\n\b\u0002\u00101\u001a\u0004\u0018\u00010\u000fH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u00104J\u0011\u00105\u001a\u00020\rH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u00106J\f\u0010%\u001a\b\u0012\u0004\u0012\u00020\u00150\u0014J\u0010\u00107\u001a\u00020+2\b\b\u0002\u00108\u001a\u000209J\u001a\u0010:\u001a\u00020+2\b\b\u0002\u0010;\u001a\u0002092\b\b\u0002\u00108\u001a\u000209J\u0006\u0010<\u001a\u00020+J!\u0010=\u001a\u00020\u00122\u0006\u0010>\u001a\u00020\u000f2\u0006\u0010?\u001a\u00020\u000fH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010@J\u0006\u0010A\u001a\u00020+J9\u0010B\u001a\u00020\u00122\u0006\u0010/\u001a\u0002002\u0006\u0010C\u001a\u00020\u000f2\n\b\u0002\u00101\u001a\u0004\u0018\u00010\u000f2\n\b\u0002\u0010?\u001a\u0004\u0018\u00010\u000fH\u0086@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010DR\u0014\u0010\t\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\r0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u000e\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000f0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0010\u001a\b\u0012\u0004\u0012\u00020\u000b0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0016\u0010\u0011\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00120\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0013\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00140\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0016\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00170\u00140\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0018\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00140\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0017\u0010\u0019\u001a\b\u0012\u0004\u0012\u00020\u000b0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001b\u0010\u001cR\u0019\u0010\u001d\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\r0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b\u001e\u0010\u001cR\u000e\u0010\u0006\u001a\u00020\u0007X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0019\u0010\u001f\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u000f0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b \u0010\u001cR\u0017\u0010!\u001a\b\u0012\u0004\u0012\u00020\u000b0\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b!\u0010\u001cR\u0019\u0010\"\u001a\n\u0012\u0006\u0012\u0004\u0018\u00010\u00120\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b#\u0010\u001cR\u001d\u0010$\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00140\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b%\u0010\u001cR\u001d\u0010&\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00170\u00140\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b\'\u0010\u001cR\u001d\u0010(\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u00150\u00140\u001a\u00a2\u0006\b\n\u0000\u001a\u0004\b)\u0010\u001c\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006E"}, d2 = {"Ltech/healthpay/keyboard/viewmodel/WalletViewModel;", "Landroidx/lifecycle/ViewModel;", "apiClient", "Ltech/healthpay/keyboard/api/HealthPayApiClient;", "authManager", "Ltech/healthpay/keyboard/security/AuthenticationManager;", "encryptionManager", "Ltech/healthpay/keyboard/security/EncryptionManager;", "(Ltech/healthpay/keyboard/api/HealthPayApiClient;Ltech/healthpay/keyboard/security/AuthenticationManager;Ltech/healthpay/keyboard/security/EncryptionManager;)V", "_authState", "Lkotlinx/coroutines/flow/MutableStateFlow;", "", "_balance", "Ltech/healthpay/keyboard/model/WalletBalance;", "_error", "", "_isLoading", "_paymentResult", "Ltech/healthpay/keyboard/model/PaymentResult;", "_recentTransactions", "", "Ltech/healthpay/keyboard/model/Transaction;", "_savedRecipients", "Ltech/healthpay/keyboard/model/SavedRecipient;", "_transactions", "authState", "Lkotlinx/coroutines/flow/StateFlow;", "getAuthState", "()Lkotlinx/coroutines/flow/StateFlow;", "balance", "getBalance", "error", "getError", "isLoading", "paymentResult", "getPaymentResult", "recentTransactions", "getRecentTransactions", "savedRecipients", "getSavedRecipients", "transactions", "getTransactions", "checkAuthState", "", "clearError", "clearPaymentResult", "generatePaymentLink", "amount", "", "description", "(DLjava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "generateReceiveQR", "(Ljava/lang/Double;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "getCurrentBalance", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "loadRecentTransactions", "limit", "", "loadTransactionHistory", "page", "onLogout", "processQRPayment", "qrData", "pin", "(Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "refreshBalance", "sendPayment", "recipientPhone", "(DLjava/lang/String;Ljava/lang/String;Ljava/lang/String;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public final class WalletViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final tech.healthpay.keyboard.api.HealthPayApiClient apiClient = null;
    @org.jetbrains.annotations.NotNull
    private final tech.healthpay.keyboard.security.AuthenticationManager authManager = null;
    @org.jetbrains.annotations.NotNull
    private final tech.healthpay.keyboard.security.EncryptionManager encryptionManager = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<tech.healthpay.keyboard.model.WalletBalance> _balance = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<tech.healthpay.keyboard.model.WalletBalance> balance = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<tech.healthpay.keyboard.model.Transaction>> _transactions = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<tech.healthpay.keyboard.model.Transaction>> transactions = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<tech.healthpay.keyboard.model.Transaction>> _recentTransactions = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<tech.healthpay.keyboard.model.Transaction>> recentTransactions = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _authState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> authState = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<tech.healthpay.keyboard.model.PaymentResult> _paymentResult = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<tech.healthpay.keyboard.model.PaymentResult> paymentResult = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.Boolean> _isLoading = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.lang.String> _error = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.lang.String> error = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<java.util.List<tech.healthpay.keyboard.model.SavedRecipient>> _savedRecipients = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<java.util.List<tech.healthpay.keyboard.model.SavedRecipient>> savedRecipients = null;
    
    @javax.inject.Inject
    public WalletViewModel(@org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.api.HealthPayApiClient apiClient, @org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.security.AuthenticationManager authManager, @org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.security.EncryptionManager encryptionManager) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<tech.healthpay.keyboard.model.WalletBalance> getBalance() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<tech.healthpay.keyboard.model.Transaction>> getTransactions() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<tech.healthpay.keyboard.model.Transaction>> getRecentTransactions() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> getAuthState() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<tech.healthpay.keyboard.model.PaymentResult> getPaymentResult() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.Boolean> isLoading() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.lang.String> getError() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<java.util.List<tech.healthpay.keyboard.model.SavedRecipient>> getSavedRecipients() {
        return null;
    }
    
    private final void checkAuthState() {
    }
    
    public final void refreshBalance() {
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object getCurrentBalance(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super tech.healthpay.keyboard.model.WalletBalance> $completion) {
        return null;
    }
    
    public final void loadRecentTransactions(int limit) {
    }
    
    public final void loadTransactionHistory(int page, int limit) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.util.List<tech.healthpay.keyboard.model.Transaction> getRecentTransactions() {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object sendPayment(double amount, @org.jetbrains.annotations.NotNull
    java.lang.String recipientPhone, @org.jetbrains.annotations.Nullable
    java.lang.String description, @org.jetbrains.annotations.Nullable
    java.lang.String pin, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super tech.healthpay.keyboard.model.PaymentResult> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object generatePaymentLink(double amount, @org.jetbrains.annotations.Nullable
    java.lang.String description, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object generateReceiveQR(@org.jetbrains.annotations.Nullable
    java.lang.Double amount, @org.jetbrains.annotations.Nullable
    java.lang.String description, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super java.lang.String> $completion) {
        return null;
    }
    
    @org.jetbrains.annotations.Nullable
    public final java.lang.Object processQRPayment(@org.jetbrains.annotations.NotNull
    java.lang.String qrData, @org.jetbrains.annotations.NotNull
    java.lang.String pin, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super tech.healthpay.keyboard.model.PaymentResult> $completion) {
        return null;
    }
    
    public final void clearError() {
    }
    
    public final void clearPaymentResult() {
    }
    
    public final void onLogout() {
    }
}