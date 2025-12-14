package tech.healthpay.keyboard.ime;

import android.content.Context;
import android.inputmethodservice.InputMethodService;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import android.os.VibratorManager;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputConnection;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LifecycleRegistry;
import androidx.lifecycle.ViewModelStore;
import androidx.lifecycle.ViewModelStoreOwner;
import dagger.hilt.android.AndroidEntryPoint;
import tech.healthpay.keyboard.api.HealthPayApiClient;
import tech.healthpay.keyboard.model.PaymentResult;
import tech.healthpay.keyboard.model.Transaction;
import tech.healthpay.keyboard.security.AuthenticationManager;
import tech.healthpay.keyboard.security.BiometricHelper;
import tech.healthpay.keyboard.ui.HealthPayKeyboardView;
import tech.healthpay.keyboard.ui.PaymentBottomSheet;
import tech.healthpay.keyboard.ui.QuickPayDialog;
import tech.healthpay.keyboard.viewmodel.KeyboardViewModel;
import tech.healthpay.keyboard.viewmodel.WalletViewModel;
import javax.inject.Inject;

/**
 * HealthPay Custom Input Method Service
 *
 * This service provides a custom keyboard with integrated HealthPay wallet
 * functionality for WhatsApp and other messaging apps.
 *
 * Key Features:
 * - Standard QWERTY keyboard with Arabic support
 * - Quick payment buttons in toolbar
 * - Biometric authentication for payments
 * - Balance display and transaction history
 */
@dagger.hilt.android.AndroidEntryPoint
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u00a8\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0010\u0006\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0010\n\u0002\u0018\u0002\n\u0002\b\u0004\n\u0002\u0010\b\n\u0002\b\u0013\b\u0007\u0018\u00002\u00020\u00012\u00020\u00022\u00020\u00032\u00020\u0004B\u0005\u00a2\u0006\u0002\u0010\u0005J\u0010\u00109\u001a\u00020:2\u0006\u0010;\u001a\u00020<H\u0002J\u0010\u0010=\u001a\u00020>2\u0006\u0010?\u001a\u00020:H\u0002J\b\u0010@\u001a\u00020>H\u0002J\u001a\u0010A\u001a\u00020>2\u0006\u0010B\u001a\u00020C2\b\u0010D\u001a\u0004\u0018\u00010:H\u0002J\b\u0010E\u001a\u00020>H\u0002J\b\u0010F\u001a\u00020>H\u0002J\b\u0010G\u001a\u00020>H\u0002J\b\u0010H\u001a\u00020>H\u0002J\b\u0010I\u001a\u00020>H\u0002J\b\u0010J\u001a\u00020>H\u0002J\u0012\u0010K\u001a\u00020>2\b\u0010L\u001a\u0004\u0018\u00010MH\u0002J\b\u0010N\u001a\u00020>H\u0002J\b\u0010O\u001a\u00020>H\u0002J\b\u0010P\u001a\u00020>H\u0002J\b\u0010Q\u001a\u00020>H\u0002J\b\u0010R\u001a\u00020>H\u0002J\b\u0010S\u001a\u00020>H\u0002J\b\u0010T\u001a\u00020>H\u0002J\u0010\u0010U\u001a\u00020>2\u0006\u0010;\u001a\u00020<H\u0002J\"\u0010V\u001a\u00020>2\u0006\u0010B\u001a\u00020C2\u0006\u0010W\u001a\u00020:2\b\u0010D\u001a\u0004\u0018\u00010:H\u0002J\b\u0010X\u001a\u00020>H\u0002J\b\u0010Y\u001a\u00020>H\u0002J\b\u0010Z\u001a\u00020>H\u0002J\b\u0010[\u001a\u00020>H\u0016J\b\u0010\\\u001a\u00020>H\u0016J\b\u0010]\u001a\u00020^H\u0016J\b\u0010_\u001a\u00020>H\u0016J\b\u0010`\u001a\u00020>H\u0016J\u0018\u0010a\u001a\u00020>2\u0006\u0010b\u001a\u00020c2\u0006\u0010d\u001a\u00020:H\u0016J\b\u0010e\u001a\u00020>H\u0016J\b\u0010f\u001a\u00020>H\u0016J\b\u0010g\u001a\u00020>H\u0016J\b\u0010h\u001a\u00020>H\u0016J\u001a\u0010i\u001a\u00020>2\b\u0010j\u001a\u0004\u0018\u00010\u00192\u0006\u0010k\u001a\u00020\u001dH\u0016J\u0018\u0010l\u001a\u00020>2\u0006\u0010m\u001a\u00020:2\u0006\u0010n\u001a\u00020:H\u0002J\"\u0010o\u001a\u00020>2\u0006\u0010B\u001a\u00020C2\u0006\u0010p\u001a\u00020:2\b\u0010D\u001a\u0004\u0018\u00010:H\u0002J\b\u0010q\u001a\u00020>H\u0002J\b\u0010r\u001a\u00020>H\u0002J\b\u0010s\u001a\u00020>H\u0002J\b\u0010t\u001a\u00020>H\u0002J\b\u0010u\u001a\u00020>H\u0002R\u001e\u0010\u0006\u001a\u00020\u00078\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\b\u0010\t\"\u0004\b\n\u0010\u000bR\u001e\u0010\f\u001a\u00020\r8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u000e\u0010\u000f\"\u0004\b\u0010\u0010\u0011R\u001e\u0010\u0012\u001a\u00020\u00138\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0014\u0010\u0015\"\u0004\b\u0016\u0010\u0017R\u0010\u0010\u0018\u001a\u0004\u0018\u00010\u0019X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001a\u001a\u0004\u0018\u00010\u001bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001c\u001a\u00020\u001dX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001e\u001a\u0004\u0018\u00010\u001fX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001e\u0010 \u001a\u00020!8\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\"\u0010#\"\u0004\b$\u0010%R\u0014\u0010&\u001a\u00020\'8VX\u0096\u0004\u00a2\u0006\u0006\u001a\u0004\b(\u0010)R\u000e\u0010*\u001a\u00020+X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0010\u0010,\u001a\u0004\u0018\u00010-X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0014\u0010.\u001a\u00020/8VX\u0096\u0004\u00a2\u0006\u0006\u001a\u0004\b0\u00101R\u000e\u00102\u001a\u00020/X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001e\u00103\u001a\u0002048\u0006@\u0006X\u0087.\u00a2\u0006\u000e\n\u0000\u001a\u0004\b5\u00106\"\u0004\b7\u00108\u00a8\u0006v"}, d2 = {"Ltech/healthpay/keyboard/ime/HealthPayInputMethodService;", "Landroid/inputmethodservice/InputMethodService;", "Landroidx/lifecycle/LifecycleOwner;", "Landroidx/lifecycle/ViewModelStoreOwner;", "Ltech/healthpay/keyboard/ui/HealthPayKeyboardView$KeyboardListener;", "()V", "apiClient", "Ltech/healthpay/keyboard/api/HealthPayApiClient;", "getApiClient", "()Ltech/healthpay/keyboard/api/HealthPayApiClient;", "setApiClient", "(Ltech/healthpay/keyboard/api/HealthPayApiClient;)V", "authManager", "Ltech/healthpay/keyboard/security/AuthenticationManager;", "getAuthManager", "()Ltech/healthpay/keyboard/security/AuthenticationManager;", "setAuthManager", "(Ltech/healthpay/keyboard/security/AuthenticationManager;)V", "biometricHelper", "Ltech/healthpay/keyboard/security/BiometricHelper;", "getBiometricHelper", "()Ltech/healthpay/keyboard/security/BiometricHelper;", "setBiometricHelper", "(Ltech/healthpay/keyboard/security/BiometricHelper;)V", "currentEditorInfo", "Landroid/view/inputmethod/EditorInfo;", "currentInputConnection", "Landroid/view/inputmethod/InputConnection;", "isWhatsAppContext", "", "keyboardView", "Ltech/healthpay/keyboard/ui/HealthPayKeyboardView;", "keyboardViewModel", "Ltech/healthpay/keyboard/viewmodel/KeyboardViewModel;", "getKeyboardViewModel", "()Ltech/healthpay/keyboard/viewmodel/KeyboardViewModel;", "setKeyboardViewModel", "(Ltech/healthpay/keyboard/viewmodel/KeyboardViewModel;)V", "lifecycle", "Landroidx/lifecycle/Lifecycle;", "getLifecycle", "()Landroidx/lifecycle/Lifecycle;", "lifecycleRegistry", "Landroidx/lifecycle/LifecycleRegistry;", "vibrator", "Landroid/os/Vibrator;", "viewModelStore", "Landroidx/lifecycle/ViewModelStore;", "getViewModelStore", "()Landroidx/lifecycle/ViewModelStore;", "viewModelStoreOwner", "walletViewModel", "Ltech/healthpay/keyboard/viewmodel/WalletViewModel;", "getWalletViewModel", "()Ltech/healthpay/keyboard/viewmodel/WalletViewModel;", "setWalletViewModel", "(Ltech/healthpay/keyboard/viewmodel/WalletViewModel;)V", "buildPaymentMessage", "", "transaction", "Ltech/healthpay/keyboard/model/Transaction;", "commitText", "", "text", "generateMyQR", "generatePaymentRequest", "amount", "", "note", "handleDelete", "handleEnter", "handleHistoryAction", "handleLanguageSwitch", "handleLogout", "handlePayAction", "handlePaymentResult", "result", "Ltech/healthpay/keyboard/model/PaymentResult;", "handleQRAction", "handleRequestPayment", "handleSettingsAction", "handleShift", "handleSpace", "handleWalletAction", "initializeVibrator", "insertPaymentConfirmation", "insertPaymentRequest", "link", "launchQRScanner", "loadWalletData", "observeViewModels", "onBalanceClicked", "onCreate", "onCreateInputView", "Landroid/view/View;", "onDestroy", "onFinishInput", "onKeyPressed", "keyCode", "", "keyText", "onPayButtonClicked", "onQRScanClicked", "onRequestPaymentClicked", "onSettingsClicked", "onStartInput", "attribute", "restarting", "performLogin", "username", "password", "processPayment", "recipientPhone", "showAuthenticationRequired", "showLoginPrompt", "showPaymentDialog", "showWalletBalance", "vibrate", "app_debug"})
public final class HealthPayInputMethodService extends android.inputmethodservice.InputMethodService implements androidx.lifecycle.LifecycleOwner, androidx.lifecycle.ViewModelStoreOwner, tech.healthpay.keyboard.ui.HealthPayKeyboardView.KeyboardListener {
    @javax.inject.Inject
    public tech.healthpay.keyboard.security.AuthenticationManager authManager;
    @javax.inject.Inject
    public tech.healthpay.keyboard.api.HealthPayApiClient apiClient;
    @javax.inject.Inject
    public tech.healthpay.keyboard.security.BiometricHelper biometricHelper;
    @javax.inject.Inject
    public tech.healthpay.keyboard.viewmodel.WalletViewModel walletViewModel;
    @javax.inject.Inject
    public tech.healthpay.keyboard.viewmodel.KeyboardViewModel keyboardViewModel;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.LifecycleRegistry lifecycleRegistry = null;
    @org.jetbrains.annotations.NotNull
    private final androidx.lifecycle.ViewModelStore viewModelStoreOwner = null;
    @org.jetbrains.annotations.Nullable
    private tech.healthpay.keyboard.ui.HealthPayKeyboardView keyboardView;
    @org.jetbrains.annotations.Nullable
    private android.view.inputmethod.InputConnection currentInputConnection;
    @org.jetbrains.annotations.Nullable
    private android.view.inputmethod.EditorInfo currentEditorInfo;
    private boolean isWhatsAppContext = false;
    @org.jetbrains.annotations.Nullable
    private android.os.Vibrator vibrator;
    
    public HealthPayInputMethodService() {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final tech.healthpay.keyboard.security.AuthenticationManager getAuthManager() {
        return null;
    }
    
    public final void setAuthManager(@org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.security.AuthenticationManager p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final tech.healthpay.keyboard.api.HealthPayApiClient getApiClient() {
        return null;
    }
    
    public final void setApiClient(@org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.api.HealthPayApiClient p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final tech.healthpay.keyboard.security.BiometricHelper getBiometricHelper() {
        return null;
    }
    
    public final void setBiometricHelper(@org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.security.BiometricHelper p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final tech.healthpay.keyboard.viewmodel.WalletViewModel getWalletViewModel() {
        return null;
    }
    
    public final void setWalletViewModel(@org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.viewmodel.WalletViewModel p0) {
    }
    
    @org.jetbrains.annotations.NotNull
    public final tech.healthpay.keyboard.viewmodel.KeyboardViewModel getKeyboardViewModel() {
        return null;
    }
    
    public final void setKeyboardViewModel(@org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.viewmodel.KeyboardViewModel p0) {
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public androidx.lifecycle.Lifecycle getLifecycle() {
        return null;
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public androidx.lifecycle.ViewModelStore getViewModelStore() {
        return null;
    }
    
    @java.lang.Override
    public void onCreate() {
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public android.view.View onCreateInputView() {
        return null;
    }
    
    @java.lang.Override
    public void onStartInput(@org.jetbrains.annotations.Nullable
    android.view.inputmethod.EditorInfo attribute, boolean restarting) {
    }
    
    @java.lang.Override
    public void onFinishInput() {
    }
    
    @java.lang.Override
    public void onDestroy() {
    }
    
    private final void observeViewModels() {
    }
    
    private final void loadWalletData() {
    }
    
    @java.lang.Override
    public void onKeyPressed(int keyCode, @org.jetbrains.annotations.NotNull
    java.lang.String keyText) {
    }
    
    @java.lang.Override
    public void onPayButtonClicked() {
    }
    
    @java.lang.Override
    public void onRequestPaymentClicked() {
    }
    
    @java.lang.Override
    public void onBalanceClicked() {
    }
    
    @java.lang.Override
    public void onQRScanClicked() {
    }
    
    @java.lang.Override
    public void onSettingsClicked() {
    }
    
    private final void commitText(java.lang.String text) {
    }
    
    private final void handleDelete() {
    }
    
    private final void handleEnter() {
    }
    
    private final void handleSpace() {
    }
    
    private final void handleShift() {
    }
    
    private final void handleLanguageSwitch() {
    }
    
    private final void handlePayAction() {
    }
    
    private final void showPaymentDialog() {
    }
    
    private final void processPayment(double amount, java.lang.String recipientPhone, java.lang.String note) {
    }
    
    private final void handlePaymentResult(tech.healthpay.keyboard.model.PaymentResult result) {
    }
    
    private final void insertPaymentConfirmation(tech.healthpay.keyboard.model.Transaction transaction) {
    }
    
    private final java.lang.String buildPaymentMessage(tech.healthpay.keyboard.model.Transaction transaction) {
        return null;
    }
    
    private final void handleRequestPayment() {
    }
    
    private final void generatePaymentRequest(double amount, java.lang.String note) {
    }
    
    private final void insertPaymentRequest(double amount, java.lang.String link, java.lang.String note) {
    }
    
    private final void handleWalletAction() {
    }
    
    private final void showWalletBalance() {
    }
    
    private final void handleQRAction() {
    }
    
    private final void launchQRScanner() {
    }
    
    private final void generateMyQR() {
    }
    
    private final void handleHistoryAction() {
    }
    
    private final void handleSettingsAction() {
    }
    
    private final void showAuthenticationRequired() {
    }
    
    private final void showLoginPrompt() {
    }
    
    private final void performLogin(java.lang.String username, java.lang.String password) {
    }
    
    private final void handleLogout() {
    }
    
    private final void initializeVibrator() {
    }
    
    private final void vibrate() {
    }
}