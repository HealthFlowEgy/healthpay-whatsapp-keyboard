package tech.healthpay.keyboard.ui;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.bottomsheet.BottomSheetBehavior;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import tech.healthpay.keyboard.R;
import tech.healthpay.keyboard.model.SavedRecipient;
import tech.healthpay.keyboard.model.Transaction;
import java.text.DecimalFormat;
import java.util.Locale;

/**
 * Quick Pay Dialog
 *
 * A compact overlay dialog for quick payment within the keyboard view.
 * Supports:
 * - Amount entry with EGP formatting
 * - Phone number input
 * - Optional note
 * - Recent recipients list
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0098\u0001\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0010\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0007\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u000b\n\u0002\b\u0002\n\u0002\u0010 \n\u0002\b\u0007\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u00100\u001a\u000201H\u0002J\u0010\u00102\u001a\u0002012\u0006\u00103\u001a\u00020)H\u0002J\b\u00104\u001a\u00020\u0011H\u0002J\u0012\u00105\u001a\u00020\u00112\b\u00106\u001a\u0004\u0018\u000107H\u0014J\b\u00108\u001a\u00020\u0018H\u0002J\u000e\u00109\u001a\u00020\u00112\u0006\u0010:\u001a\u00020;J\u0014\u0010<\u001a\u00020\u00112\f\u0010=\u001a\b\u0012\u0004\u0012\u00020)0>J\b\u0010?\u001a\u00020\u0011H\u0002J\b\u0010@\u001a\u00020\u0011H\u0002J\u0010\u0010A\u001a\u00020\u00112\u0006\u0010B\u001a\u00020\u001cH\u0002J\b\u0010C\u001a\u00020\u0011H\u0002J\b\u0010D\u001a\u00020\u0011H\u0002R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\nX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\fX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u000eX\u0082.\u00a2\u0006\u0002\n\u0000R\"\u0010\u000f\u001a\n\u0012\u0004\u0012\u00020\u0011\u0018\u00010\u0010X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0012\u0010\u0013\"\u0004\b\u0014\u0010\u0015Rc\u0010\u0016\u001aK\u0012\u0013\u0012\u00110\u0018\u00a2\u0006\f\b\u0019\u0012\b\b\u001a\u0012\u0004\b\b(\u001b\u0012\u0013\u0012\u00110\u001c\u00a2\u0006\f\b\u0019\u0012\b\b\u001a\u0012\u0004\b\b(\u001d\u0012\u0015\u0012\u0013\u0018\u00010\u001c\u00a2\u0006\f\b\u0019\u0012\b\b\u001a\u0012\u0004\b\b(\u001e\u0012\u0004\u0012\u00020\u0011\u0018\u00010\u0017X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010 \"\u0004\b!\u0010\"R\u000e\u0010#\u001a\u00020$X\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010%\u001a\u00020\u000eX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010&\u001a\u00020\'X\u0082.\u00a2\u0006\u0002\n\u0000R\u0010\u0010(\u001a\u0004\u0018\u00010)X\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0018\u0010*\u001a\u00020+*\u00020+8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b,\u0010-R\u0018\u0010*\u001a\u00020.*\u00020.8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b,\u0010/\u00a8\u0006E"}, d2 = {"Ltech/healthpay/keyboard/ui/QuickPayDialog;", "Landroid/app/Dialog;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "amountInput", "Landroid/widget/EditText;", "closeButton", "Landroid/widget/ImageButton;", "errorText", "Landroid/widget/TextView;", "loadingIndicator", "Landroid/widget/ProgressBar;", "noteInput", "Lcom/google/android/material/textfield/TextInputEditText;", "onDismiss", "Lkotlin/Function0;", "", "getOnDismiss", "()Lkotlin/jvm/functions/Function0;", "setOnDismiss", "(Lkotlin/jvm/functions/Function0;)V", "onPaymentSubmit", "Lkotlin/Function3;", "", "Lkotlin/ParameterName;", "name", "amount", "", "recipientPhone", "note", "getOnPaymentSubmit", "()Lkotlin/jvm/functions/Function3;", "setOnPaymentSubmit", "(Lkotlin/jvm/functions/Function3;)V", "payButton", "Landroid/widget/Button;", "phoneInput", "recentRecipientsContainer", "Landroid/widget/LinearLayout;", "selectedRecipient", "Ltech/healthpay/keyboard/model/SavedRecipient;", "dp", "", "getDp", "(F)F", "", "(I)I", "createDialogView", "Landroid/view/View;", "createRecipientChip", "recipient", "hideError", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "parseAmount", "setLoading", "loading", "", "setRecentRecipients", "recipients", "", "setupAmountFormatter", "setupListeners", "showError", "message", "validateAndSubmit", "validateInputs", "app_debug"})
public final class QuickPayDialog extends android.app.Dialog {
    @org.jetbrains.annotations.Nullable
    private kotlin.jvm.functions.Function3<? super java.lang.Double, ? super java.lang.String, ? super java.lang.String, kotlin.Unit> onPaymentSubmit;
    @org.jetbrains.annotations.Nullable
    private kotlin.jvm.functions.Function0<kotlin.Unit> onDismiss;
    private android.widget.EditText amountInput;
    private com.google.android.material.textfield.TextInputEditText phoneInput;
    private com.google.android.material.textfield.TextInputEditText noteInput;
    private android.widget.Button payButton;
    private android.widget.ImageButton closeButton;
    private android.widget.ProgressBar loadingIndicator;
    private android.widget.LinearLayout recentRecipientsContainer;
    private android.widget.TextView errorText;
    @org.jetbrains.annotations.Nullable
    private tech.healthpay.keyboard.model.SavedRecipient selectedRecipient;
    
    public QuickPayDialog(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super(null);
    }
    
    @org.jetbrains.annotations.Nullable
    public final kotlin.jvm.functions.Function3<java.lang.Double, java.lang.String, java.lang.String, kotlin.Unit> getOnPaymentSubmit() {
        return null;
    }
    
    public final void setOnPaymentSubmit(@org.jetbrains.annotations.Nullable
    kotlin.jvm.functions.Function3<? super java.lang.Double, ? super java.lang.String, ? super java.lang.String, kotlin.Unit> p0) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final kotlin.jvm.functions.Function0<kotlin.Unit> getOnDismiss() {
        return null;
    }
    
    public final void setOnDismiss(@org.jetbrains.annotations.Nullable
    kotlin.jvm.functions.Function0<kotlin.Unit> p0) {
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    private final android.view.View createDialogView() {
        return null;
    }
    
    private final void setupListeners() {
    }
    
    private final void setupAmountFormatter() {
    }
    
    private final void validateInputs() {
    }
    
    private final void validateAndSubmit() {
    }
    
    private final double parseAmount() {
        return 0.0;
    }
    
    private final void showError(java.lang.String message) {
    }
    
    private final void hideError() {
    }
    
    public final void setLoading(boolean loading) {
    }
    
    public final void setRecentRecipients(@org.jetbrains.annotations.NotNull
    java.util.List<tech.healthpay.keyboard.model.SavedRecipient> recipients) {
    }
    
    private final android.view.View createRecipientChip(tech.healthpay.keyboard.model.SavedRecipient recipient) {
        return null;
    }
    
    private final int getDp(int $this$dp) {
        return 0;
    }
    
    private final float getDp(float $this$dp) {
        return 0.0F;
    }
}