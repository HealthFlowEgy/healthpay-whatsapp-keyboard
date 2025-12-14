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
 * Payment Bottom Sheet
 *
 * Full-featured bottom sheet for payments and requests
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000Z\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\u0010\u0002\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\u0010\u0006\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0007\n\u0002\u0018\u0002\n\u0002\b\u0005\n\u0002\u0010\b\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0002\b\u0002\u0018\u00002\u00020\u0001:\u0001+B\u0017\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u0012\b\b\u0002\u0010\u0004\u001a\u00020\u0005\u00a2\u0006\u0002\u0010\u0006J\b\u0010\'\u001a\u00020\bH\u0002J\u0012\u0010(\u001a\u00020\u000b2\b\u0010)\u001a\u0004\u0018\u00010*H\u0014R\u000e\u0010\u0007\u001a\u00020\bX\u0082.\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0004\u001a\u00020\u0005X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\"\u0010\t\u001a\n\u0012\u0004\u0012\u00020\u000b\u0018\u00010\nX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\f\u0010\r\"\u0004\b\u000e\u0010\u000fRc\u0010\u0010\u001aK\u0012\u0013\u0012\u00110\u0012\u00a2\u0006\f\b\u0013\u0012\b\b\u0014\u0012\u0004\b\b(\u0015\u0012\u0013\u0012\u00110\u0016\u00a2\u0006\f\b\u0013\u0012\b\b\u0014\u0012\u0004\b\b(\u0017\u0012\u0015\u0012\u0013\u0018\u00010\u0016\u00a2\u0006\f\b\u0013\u0012\b\b\u0014\u0012\u0004\b\b(\u0018\u0012\u0004\u0012\u00020\u000b\u0018\u00010\u0011X\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u0019\u0010\u001a\"\u0004\b\u001b\u0010\u001cRN\u0010\u001d\u001a6\u0012\u0013\u0012\u00110\u0012\u00a2\u0006\f\b\u0013\u0012\b\b\u0014\u0012\u0004\b\b(\u0015\u0012\u0015\u0012\u0013\u0018\u00010\u0016\u00a2\u0006\f\b\u0013\u0012\b\b\u0014\u0012\u0004\b\b(\u0018\u0012\u0004\u0012\u00020\u000b\u0018\u00010\u001eX\u0086\u000e\u00a2\u0006\u000e\n\u0000\u001a\u0004\b\u001f\u0010 \"\u0004\b!\u0010\"R\u0018\u0010#\u001a\u00020$*\u00020$8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b%\u0010&\u00a8\u0006,"}, d2 = {"Ltech/healthpay/keyboard/ui/PaymentBottomSheet;", "Lcom/google/android/material/bottomsheet/BottomSheetDialog;", "context", "Landroid/content/Context;", "mode", "Ltech/healthpay/keyboard/ui/PaymentBottomSheet$Mode;", "(Landroid/content/Context;Ltech/healthpay/keyboard/ui/PaymentBottomSheet$Mode;)V", "contentView", "Landroid/view/View;", "onDismissed", "Lkotlin/Function0;", "", "getOnDismissed", "()Lkotlin/jvm/functions/Function0;", "setOnDismissed", "(Lkotlin/jvm/functions/Function0;)V", "onPaymentSubmit", "Lkotlin/Function3;", "", "Lkotlin/ParameterName;", "name", "amount", "", "recipientPhone", "note", "getOnPaymentSubmit", "()Lkotlin/jvm/functions/Function3;", "setOnPaymentSubmit", "(Lkotlin/jvm/functions/Function3;)V", "onRequestSubmit", "Lkotlin/Function2;", "getOnRequestSubmit", "()Lkotlin/jvm/functions/Function2;", "setOnRequestSubmit", "(Lkotlin/jvm/functions/Function2;)V", "dp", "", "getDp", "(I)I", "createContentView", "onCreate", "savedInstanceState", "Landroid/os/Bundle;", "Mode", "app_debug"})
public final class PaymentBottomSheet extends com.google.android.material.bottomsheet.BottomSheetDialog {
    @org.jetbrains.annotations.NotNull
    private final tech.healthpay.keyboard.ui.PaymentBottomSheet.Mode mode = null;
    @org.jetbrains.annotations.Nullable
    private kotlin.jvm.functions.Function3<? super java.lang.Double, ? super java.lang.String, ? super java.lang.String, kotlin.Unit> onPaymentSubmit;
    @org.jetbrains.annotations.Nullable
    private kotlin.jvm.functions.Function2<? super java.lang.Double, ? super java.lang.String, kotlin.Unit> onRequestSubmit;
    @org.jetbrains.annotations.Nullable
    private kotlin.jvm.functions.Function0<kotlin.Unit> onDismissed;
    private android.view.View contentView;
    
    public PaymentBottomSheet(@org.jetbrains.annotations.NotNull
    android.content.Context context, @org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.ui.PaymentBottomSheet.Mode mode) {
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
    public final kotlin.jvm.functions.Function2<java.lang.Double, java.lang.String, kotlin.Unit> getOnRequestSubmit() {
        return null;
    }
    
    public final void setOnRequestSubmit(@org.jetbrains.annotations.Nullable
    kotlin.jvm.functions.Function2<? super java.lang.Double, ? super java.lang.String, kotlin.Unit> p0) {
    }
    
    @org.jetbrains.annotations.Nullable
    public final kotlin.jvm.functions.Function0<kotlin.Unit> getOnDismissed() {
        return null;
    }
    
    public final void setOnDismissed(@org.jetbrains.annotations.Nullable
    kotlin.jvm.functions.Function0<kotlin.Unit> p0) {
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    private final android.view.View createContentView() {
        return null;
    }
    
    private final int getDp(int $this$dp) {
        return 0;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\f\n\u0002\u0018\u0002\n\u0002\u0010\u0010\n\u0002\b\u0004\b\u0086\u0081\u0002\u0018\u00002\b\u0012\u0004\u0012\u00020\u00000\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002j\u0002\b\u0003j\u0002\b\u0004\u00a8\u0006\u0005"}, d2 = {"Ltech/healthpay/keyboard/ui/PaymentBottomSheet$Mode;", "", "(Ljava/lang/String;I)V", "SEND", "REQUEST", "app_debug"})
    public static enum Mode {
        /*public static final*/ SEND /* = new SEND() */,
        /*public static final*/ REQUEST /* = new REQUEST() */;
        
        Mode() {
        }
        
        @org.jetbrains.annotations.NotNull
        public static kotlin.enums.EnumEntries<tech.healthpay.keyboard.ui.PaymentBottomSheet.Mode> getEntries() {
            return null;
        }
    }
}