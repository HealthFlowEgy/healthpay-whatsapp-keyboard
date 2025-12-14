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
 * Transaction List Adapter
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u00002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010 \n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\u0004\n\u0002\u0010\u0002\n\u0002\b\u0004\n\u0002\u0018\u0002\n\u0002\b\u0003\u0018\u00002\b\u0012\u0004\u0012\u00020\u00020\u0001:\u0001\u0014B\u0013\u0012\f\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004\u00a2\u0006\u0002\u0010\u0006J\b\u0010\u000b\u001a\u00020\bH\u0016J\u0018\u0010\f\u001a\u00020\r2\u0006\u0010\u000e\u001a\u00020\u00022\u0006\u0010\u000f\u001a\u00020\bH\u0016J\u0018\u0010\u0010\u001a\u00020\u00022\u0006\u0010\u0011\u001a\u00020\u00122\u0006\u0010\u0013\u001a\u00020\bH\u0016R\u0014\u0010\u0003\u001a\b\u0012\u0004\u0012\u00020\u00050\u0004X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0018\u0010\u0007\u001a\u00020\b*\u00020\b8BX\u0082\u0004\u00a2\u0006\u0006\u001a\u0004\b\t\u0010\n\u00a8\u0006\u0015"}, d2 = {"Ltech/healthpay/keyboard/ui/TransactionAdapter;", "Landroidx/recyclerview/widget/RecyclerView$Adapter;", "Ltech/healthpay/keyboard/ui/TransactionAdapter$ViewHolder;", "transactions", "", "Ltech/healthpay/keyboard/model/Transaction;", "(Ljava/util/List;)V", "dp", "", "getDp", "(I)I", "getItemCount", "onBindViewHolder", "", "holder", "position", "onCreateViewHolder", "parent", "Landroid/view/ViewGroup;", "viewType", "ViewHolder", "app_debug"})
public final class TransactionAdapter extends androidx.recyclerview.widget.RecyclerView.Adapter<tech.healthpay.keyboard.ui.TransactionAdapter.ViewHolder> {
    @org.jetbrains.annotations.NotNull
    private final java.util.List<tech.healthpay.keyboard.model.Transaction> transactions = null;
    
    public TransactionAdapter(@org.jetbrains.annotations.NotNull
    java.util.List<tech.healthpay.keyboard.model.Transaction> transactions) {
        super();
    }
    
    @java.lang.Override
    @org.jetbrains.annotations.NotNull
    public tech.healthpay.keyboard.ui.TransactionAdapter.ViewHolder onCreateViewHolder(@org.jetbrains.annotations.NotNull
    android.view.ViewGroup parent, int viewType) {
        return null;
    }
    
    @java.lang.Override
    public void onBindViewHolder(@org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.ui.TransactionAdapter.ViewHolder holder, int position) {
    }
    
    @java.lang.Override
    public int getItemCount() {
        return 0;
    }
    
    private final int getDp(int $this$dp) {
        return 0;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\b\u000b\u0018\u00002\u00020\u0001B\r\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004R\u0011\u0010\u0005\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0007\u0010\bR\u0011\u0010\t\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\n\u0010\bR\u0011\u0010\u000b\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\f\u0010\bR\u0011\u0010\r\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\bR\u0011\u0010\u000f\u001a\u00020\u0006\u00a2\u0006\b\n\u0000\u001a\u0004\b\u0010\u0010\b\u00a8\u0006\u0011"}, d2 = {"Ltech/healthpay/keyboard/ui/TransactionAdapter$ViewHolder;", "Landroidx/recyclerview/widget/RecyclerView$ViewHolder;", "view", "Landroid/view/View;", "(Landroid/view/View;)V", "amount", "Landroid/widget/TextView;", "getAmount", "()Landroid/widget/TextView;", "avatar", "getAvatar", "date", "getDate", "name", "getName", "status", "getStatus", "app_debug"})
    public static final class ViewHolder extends androidx.recyclerview.widget.RecyclerView.ViewHolder {
        @org.jetbrains.annotations.NotNull
        private final android.widget.TextView avatar = null;
        @org.jetbrains.annotations.NotNull
        private final android.widget.TextView name = null;
        @org.jetbrains.annotations.NotNull
        private final android.widget.TextView date = null;
        @org.jetbrains.annotations.NotNull
        private final android.widget.TextView amount = null;
        @org.jetbrains.annotations.NotNull
        private final android.widget.TextView status = null;
        
        public ViewHolder(@org.jetbrains.annotations.NotNull
        android.view.View view) {
            super(null);
        }
        
        @org.jetbrains.annotations.NotNull
        public final android.widget.TextView getAvatar() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final android.widget.TextView getName() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final android.widget.TextView getDate() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final android.widget.TextView getAmount() {
            return null;
        }
        
        @org.jetbrains.annotations.NotNull
        public final android.widget.TextView getStatus() {
            return null;
        }
    }
}