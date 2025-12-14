package tech.healthpay.keyboard.ui;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.progressindicator.CircularProgressIndicator;
import tech.healthpay.keyboard.R;
import tech.healthpay.keyboard.model.Transaction;
import tech.healthpay.keyboard.model.WalletBalance;

/**
 * Custom Keyboard View for HealthPay
 *
 * Provides:
 * - QWERTY keyboard with Arabic support
 * - Payment toolbar with quick actions
 * - Balance display
 * - Animations and haptic feedback
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u009a\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\u0010\u000e\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0010\u0002\n\u0002\b\u0006\n\u0002\u0018\u0002\n\u0002\b\u000b\n\u0002\u0018\u0002\n\u0002\b\u0017\n\u0002\u0018\u0002\n\u0002\b\t\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u000b\u0018\u00002\u00020\u0001:\u0001rB%\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\n\b\u0002\u0010\u0004\u001a\u0004\u0018\u00010\u0005\u0012\b\b\u0002\u0010\u0006\u001a\u00020\u0007\u00a2\u0006\u0002\u0010\bJ\u0018\u0010.\u001a\u00020/2\u0006\u00100\u001a\u00020%2\u0006\u00101\u001a\u00020\u0017H\u0002J\u001c\u00102\u001a\u00020/2\u0012\u00103\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\nH\u0002J\u0006\u00104\u001a\u00020/J\u0010\u00105\u001a\u0002062\u0006\u00107\u001a\u00020\u0017H\u0002J(\u00108\u001a\u00020%2\u0006\u00109\u001a\u00020\u000b2\u0006\u0010:\u001a\u00020\u00072\u0006\u0010;\u001a\u00020\u00072\u0006\u0010<\u001a\u00020\u0007H\u0002J\b\u0010=\u001a\u000206H\u0002J(\u0010>\u001a\u00020\u00012\u0006\u0010?\u001a\u00020\u000b2\b\u0010@\u001a\u0004\u0018\u00010\u000b2\f\u0010A\u001a\b\u0012\u0004\u0012\u00020/0BH\u0002J\u0010\u0010C\u001a\u00020/2\u0006\u00109\u001a\u00020\u000bH\u0002J\b\u0010D\u001a\u00020/H\u0002J\b\u0010E\u001a\u00020/H\u0002J\b\u0010F\u001a\u00020/H\u0002J\b\u0010G\u001a\u00020/H\u0002J\b\u0010H\u001a\u00020/H\u0002J\u000e\u0010I\u001a\u00020/2\u0006\u0010J\u001a\u00020\u0017J\u000e\u0010K\u001a\u00020/2\u0006\u0010 \u001a\u00020!J\u000e\u0010L\u001a\u00020/2\u0006\u0010M\u001a\u00020\u0017J\u0014\u0010N\u001a\u00020/2\f\u0010O\u001a\b\u0012\u0004\u0012\u00020/0BJ\u000e\u0010P\u001a\u00020/2\u0006\u0010Q\u001a\u00020\u0013J\u000e\u0010R\u001a\u00020/2\u0006\u0010S\u001a\u00020\u000bJ\u0018\u0010T\u001a\u00020/2\u0006\u0010U\u001a\u00020%2\u0006\u00109\u001a\u00020\u000bH\u0002J\u000e\u0010V\u001a\u00020/2\u0006\u0010W\u001a\u00020\u0017J \u0010X\u001a\u00020/2\u0018\u0010Y\u001a\u0014\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020\u000b\u0012\u0004\u0012\u00020/0ZJ\u000e\u0010[\u001a\u00020/2\u0006\u0010\\\u001a\u00020\u000bJ\"\u0010]\u001a\u00020/2\f\u0010^\u001a\b\u0012\u0004\u0012\u00020/0B2\f\u0010_\u001a\b\u0012\u0004\u0012\u00020/0BJ>\u0010`\u001a\u00020/2\u0006\u0010\u0018\u001a\u00020\u00172\f\u0010a\u001a\b\u0012\u0004\u0012\u00020/0B2\f\u0010b\u001a\b\u0012\u0004\u0012\u00020/0B2\u0012\u0010c\u001a\u000e\u0012\u0004\u0012\u00020\u0017\u0012\u0004\u0012\u00020/0dJ\u000e\u0010e\u001a\u00020/2\u0006\u0010S\u001a\u00020\u000bJ\u0014\u0010f\u001a\u00020/2\f\u0010g\u001a\b\u0012\u0004\u0012\u00020h0\nJ\u0006\u0010i\u001a\u00020/J\u0006\u0010j\u001a\u00020/J\b\u0010k\u001a\u00020/H\u0002J\u0010\u0010l\u001a\u00020/2\b\u0010Q\u001a\u0004\u0018\u00010\u0013J\b\u0010m\u001a\u00020/H\u0002J\u000e\u0010n\u001a\u00020/2\u0006\u0010o\u001a\u00020\u0017J\b\u0010p\u001a\u00020/H\u0002J\b\u0010q\u001a\u00020/H\u0002R\u001a\u0010\t\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\rX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000e\u001a\u00020\u000fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0010\u001a\u00020\u0011X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u0012\u001a\u0004\u0018\u00010\u0013X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0014\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u001a\u0010\u0015\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0016\u001a\u00020\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0018\u001a\u00020\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0019\u001a\u00020\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001a\u001a\u00020\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001b\u001a\u00020\u0017X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0010\u0010\u001c\u001a\u0004\u0018\u00010\u001dX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u001e\u001a\u00020\u001fX\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010 \u001a\u0004\u0018\u00010!X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u000e\u0010\"\u001a\u00020#X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010$\u001a\u00020%X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010&\u001a\u00020%X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\'\u001a\u00020%X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010(\u001a\u00020%X\u0082.\u00a2\u0006\u0002\n\u0000R\u001a\u0010)\u001a\u000e\u0012\n\u0012\b\u0012\u0004\u0012\u00020\u000b0\n0\nX\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u000e\u0010*\u001a\u00020\u0001X\u0082.\u00a2\u0006\u0002\n\u0000R\u0018\u0010+\u001a\u00020\u0007*\u00020\u00078BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b,\u0010-\u00a8\u0006s"}, d2 = {"Ltech/healthpay/keyboard/ui/HealthPayKeyboardView;", "Landroid/widget/LinearLayout;", "context", "Landroid/content/Context;", "attrs", "Landroid/util/AttributeSet;", "defStyleAttr", "", "(Landroid/content/Context;Landroid/util/AttributeSet;I)V", "arabicKeys", "", "", "authPromptContainer", "Landroid/widget/FrameLayout;", "balanceCard", "Landroidx/cardview/widget/CardView;", "balanceText", "Landroid/widget/TextView;", "currentBalance", "Ltech/healthpay/keyboard/model/WalletBalance;", "englishKeys", "englishKeysShift", "isArabic", "", "isAuthenticated", "isCapsLock", "isShiftActive", "isWhatsAppMode", "keyPopup", "Landroid/widget/PopupWindow;", "keyboardContainer", "Landroid/widget/GridLayout;", "listener", "Ltech/healthpay/keyboard/ui/HealthPayKeyboardView$KeyboardListener;", "loadingIndicator", "Lcom/google/android/material/progressindicator/CircularProgressIndicator;", "payButton", "Landroid/view/View;", "qrButton", "requestButton", "settingsButton", "symbolKeys", "toolbarContainer", "dp", "getDp", "(I)I", "animateKeyPress", "", "view", "pressed", "buildKeyboard", "layout", "clearWalletData", "createKeyBackground", "Landroid/graphics/drawable/GradientDrawable;", "isSpecial", "createKeyView", "key", "row", "col", "rowSize", "createRippleDrawable", "createToolbarButton", "emoji", "label", "onClick", "Lkotlin/Function0;", "handleKeyPress", "hideKeyPopup", "initAuthPrompt", "initKeyboard", "initLoadingIndicator", "initToolbar", "setAuthenticationState", "authenticated", "setKeyboardListener", "setWhatsAppMode", "enabled", "showAuthPrompt", "onLoginClicked", "showBalanceCard", "balance", "showError", "message", "showKeyPopup", "anchor", "showLoading", "show", "showLoginSheet", "onCredentialsSubmit", "Lkotlin/Function2;", "showQRCode", "qrData", "showQROptions", "onScan", "onGenerate", "showSettings", "onLogin", "onLogout", "onToggleNotifications", "Lkotlin/Function1;", "showSuccess", "showTransactionHistory", "transactions", "Ltech/healthpay/keyboard/model/Transaction;", "switchLanguage", "toggleShift", "toggleSymbols", "updateBalance", "updateKeyboardLayout", "updatePaymentButtonVisibility", "visible", "updateToolbarState", "updateToolbarVisibility", "KeyboardListener", "app_debug"})
public final class HealthPayKeyboardView extends android.widget.LinearLayout {
    private android.widget.LinearLayout toolbarContainer;
    private android.view.View payButton;
    private android.view.View requestButton;
    private androidx.cardview.widget.CardView balanceCard;
    private android.widget.TextView balanceText;
    private android.view.View qrButton;
    private android.view.View settingsButton;
    private android.widget.GridLayout keyboardContainer;
    private com.google.android.material.progressindicator.CircularProgressIndicator loadingIndicator;
    private android.widget.FrameLayout authPromptContainer;
    @org.jetbrains.annotations.Nullable
    private tech.healthpay.keyboard.ui.HealthPayKeyboardView.KeyboardListener listener;
    private boolean isShiftActive = false;
    private boolean isCapsLock = false;
    private boolean isArabic = false;
    private boolean isWhatsAppMode = false;
    private boolean isAuthenticated = false;
    @org.jetbrains.annotations.Nullable
    private tech.healthpay.keyboard.model.WalletBalance currentBalance;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<java.util.List<java.lang.String>> englishKeys = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<java.util.List<java.lang.String>> englishKeysShift = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<java.util.List<java.lang.String>> arabicKeys = null;
    @org.jetbrains.annotations.NotNull
    private final java.util.List<java.util.List<java.lang.String>> symbolKeys = null;
    @org.jetbrains.annotations.Nullable
    private android.widget.PopupWindow keyPopup;
    
    @kotlin.jvm.JvmOverloads
    public HealthPayKeyboardView(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.Nullable
    android.util.AttributeSet attrs, int defStyleAttr) {
        super(null);
    }
    
    private final void initToolbar() {
    }
    
    private final android.widget.LinearLayout createToolbarButton(java.lang.String emoji, java.lang.String label, kotlin.jvm.functions.Function0<kotlin.Unit> onClick) {
        return null;
    }
    
    private final void initKeyboard() {
    }
    
    private final void buildKeyboard(java.util.List<? extends java.util.List<java.lang.String>> layout) {
    }
    
    private final android.view.View createKeyView(java.lang.String key, int row, int col, int rowSize) {
        return null;
    }
    
    private final android.graphics.drawable.GradientDrawable createKeyBackground(boolean isSpecial) {
        return null;
    }
    
    private final android.graphics.drawable.GradientDrawable createRippleDrawable() {
        return null;
    }
    
    private final void showKeyPopup(android.view.View anchor, java.lang.String key) {
    }
    
    private final void hideKeyPopup() {
    }
    
    private final void animateKeyPress(android.view.View view, boolean pressed) {
    }
    
    private final void handleKeyPress(java.lang.String key) {
    }
    
    public final void setKeyboardListener(@org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.ui.HealthPayKeyboardView.KeyboardListener listener) {
    }
    
    public final void setWhatsAppMode(boolean enabled) {
    }
    
    public final void setAuthenticationState(boolean authenticated) {
    }
    
    public final void updatePaymentButtonVisibility(boolean visible) {
    }
    
    public final void updateBalance(@org.jetbrains.annotations.Nullable
    tech.healthpay.keyboard.model.WalletBalance balance) {
    }
    
    public final void toggleShift() {
    }
    
    public final void switchLanguage() {
    }
    
    private final void toggleSymbols() {
    }
    
    private final void updateKeyboardLayout() {
    }
    
    private final void updateToolbarVisibility() {
    }
    
    private final void updateToolbarState() {
    }
    
    private final void initAuthPrompt() {
    }
    
    private final void initLoadingIndicator() {
    }
    
    public final void showLoading(boolean show) {
    }
    
    public final void showError(@org.jetbrains.annotations.NotNull
    java.lang.String message) {
    }
    
    public final void showSuccess(@org.jetbrains.annotations.NotNull
    java.lang.String message) {
    }
    
    public final void showAuthPrompt(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onLoginClicked) {
    }
    
    public final void showBalanceCard(@org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.model.WalletBalance balance) {
    }
    
    public final void showQROptions(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onScan, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onGenerate) {
    }
    
    public final void showQRCode(@org.jetbrains.annotations.NotNull
    java.lang.String qrData) {
    }
    
    public final void showTransactionHistory(@org.jetbrains.annotations.NotNull
    java.util.List<tech.healthpay.keyboard.model.Transaction> transactions) {
    }
    
    public final void showSettings(boolean isAuthenticated, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onLogin, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function0<kotlin.Unit> onLogout, @org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super java.lang.Boolean, kotlin.Unit> onToggleNotifications) {
    }
    
    public final void showLoginSheet(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function2<? super java.lang.String, ? super java.lang.String, kotlin.Unit> onCredentialsSubmit) {
    }
    
    public final void clearWalletData() {
    }
    
    private final int getDp(int $this$dp) {
        return 0;
    }
    
    @kotlin.jvm.JvmOverloads
    public HealthPayKeyboardView(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super(null);
    }
    
    @kotlin.jvm.JvmOverloads
    public HealthPayKeyboardView(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.Nullable
    android.util.AttributeSet attrs) {
        super(null);
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000 \n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0000\n\u0002\u0010\u000e\n\u0002\b\u0005\bf\u0018\u00002\u00020\u0001J\b\u0010\u0002\u001a\u00020\u0003H&J\u0018\u0010\u0004\u001a\u00020\u00032\u0006\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\bH&J\b\u0010\t\u001a\u00020\u0003H&J\b\u0010\n\u001a\u00020\u0003H&J\b\u0010\u000b\u001a\u00020\u0003H&J\b\u0010\f\u001a\u00020\u0003H&\u00a8\u0006\r"}, d2 = {"Ltech/healthpay/keyboard/ui/HealthPayKeyboardView$KeyboardListener;", "", "onBalanceClicked", "", "onKeyPressed", "keyCode", "", "keyText", "", "onPayButtonClicked", "onQRScanClicked", "onRequestPaymentClicked", "onSettingsClicked", "app_debug"})
    public static abstract interface KeyboardListener {
        
        public abstract void onKeyPressed(int keyCode, @org.jetbrains.annotations.NotNull
        java.lang.String keyText);
        
        public abstract void onPayButtonClicked();
        
        public abstract void onRequestPaymentClicked();
        
        public abstract void onBalanceClicked();
        
        public abstract void onQRScanClicked();
        
        public abstract void onSettingsClicked();
    }
}