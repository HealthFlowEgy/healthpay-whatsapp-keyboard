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
 * Key code constants for special keyboard actions
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u0014\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\b\n\u0002\b\n\b\u00c6\u0002\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0006\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\t\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\n\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u000b\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\f\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\r\u001a\u00020\u0004X\u0086T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u000e"}, d2 = {"Ltech/healthpay/keyboard/ime/KeyboardKeys;", "", "()V", "DELETE", "", "ENTER", "HISTORY", "LANGUAGE", "PAY", "QR", "SETTINGS", "SHIFT", "SPACE", "WALLET", "app_debug"})
public final class KeyboardKeys {
    public static final int DELETE = -1;
    public static final int ENTER = -2;
    public static final int SPACE = -3;
    public static final int SHIFT = -4;
    public static final int LANGUAGE = -5;
    public static final int PAY = -100;
    public static final int WALLET = -101;
    public static final int QR = -102;
    public static final int HISTORY = -103;
    public static final int SETTINGS = -104;
    @org.jetbrains.annotations.NotNull
    public static final tech.healthpay.keyboard.ime.KeyboardKeys INSTANCE = null;
    
    private KeyboardKeys() {
        super();
    }
}