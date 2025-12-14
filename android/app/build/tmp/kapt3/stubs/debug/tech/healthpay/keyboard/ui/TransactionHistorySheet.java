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
 * Transaction History Bottom Sheet
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000>\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0004\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u000e\u001a\u00020\u000fH\u0002J\u0012\u0010\u0010\u001a\u00020\u00112\b\u0010\u0012\u001a\u0004\u0018\u00010\u0013H\u0014J\u0014\u0010\u0014\u001a\u00020\u00112\f\u0010\u0015\u001a\b\u0012\u0004\u0012\u00020\t0\bJ\b\u0010\u0016\u001a\u00020\u0011H\u0002R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082.\u00a2\u0006\u0002\n\u0000R\u0014\u0010\u0007\u001a\b\u0012\u0004\u0012\u00020\t0\bX\u0082\u000e\u00a2\u0006\u0002\n\u0000R\u0018\u0010\n\u001a\u00020\u000b*\u00020\u000b8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\f\u0010\r\u00a8\u0006\u0017"}, d2 = {"Ltech/healthpay/keyboard/ui/TransactionHistorySheet;", "Lcom/google/android/material/bottomsheet/BottomSheetDialog;", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "recyclerView", "Landroidx/recyclerview/widget/RecyclerView;", "transactions", "", "Ltech/healthpay/keyboard/model/Transaction;", "dp", "", "getDp", "(I)I", "createContentView", "Landroid/view/View;", "onCreate", "", "savedInstanceState", "Landroid/os/Bundle;", "setTransactions", "list", "setupRecyclerView", "app_debug"})
public final class TransactionHistorySheet extends com.google.android.material.bottomsheet.BottomSheetDialog {
    @org.jetbrains.annotations.NotNull
    private java.util.List<tech.healthpay.keyboard.model.Transaction> transactions;
    private androidx.recyclerview.widget.RecyclerView recyclerView;
    
    public TransactionHistorySheet(@org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super(null);
    }
    
    public final void setTransactions(@org.jetbrains.annotations.NotNull
    java.util.List<tech.healthpay.keyboard.model.Transaction> list) {
    }
    
    @java.lang.Override
    protected void onCreate(@org.jetbrains.annotations.Nullable
    android.os.Bundle savedInstanceState) {
    }
    
    private final android.view.View createContentView() {
        return null;
    }
    
    private final void setupRecyclerView() {
    }
    
    private final int getDp(int $this$dp) {
        return 0;
    }
}