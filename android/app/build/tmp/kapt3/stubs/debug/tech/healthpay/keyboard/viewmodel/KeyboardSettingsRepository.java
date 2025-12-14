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
 * Settings Repository Interface
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u0002\n\u0002\b\u0003\bf\u0018\u00002\u00020\u0001J\u0011\u0010\u0002\u001a\u00020\u0003H\u00a6@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\u0004J\u0019\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0003H\u00a6@\u00f8\u0001\u0000\u00a2\u0006\u0002\u0010\b\u0082\u0002\u0004\n\u0002\b\u0019\u00a8\u0006\t"}, d2 = {"Ltech/healthpay/keyboard/viewmodel/KeyboardSettingsRepository;", "", "getSettings", "Ltech/healthpay/keyboard/model/KeyboardSettings;", "(Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "saveSettings", "", "settings", "(Ltech/healthpay/keyboard/model/KeyboardSettings;Lkotlin/coroutines/Continuation;)Ljava/lang/Object;", "app_debug"})
public abstract interface KeyboardSettingsRepository {
    
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object getSettings(@org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super tech.healthpay.keyboard.model.KeyboardSettings> $completion);
    
    @org.jetbrains.annotations.Nullable
    public abstract java.lang.Object saveSettings(@org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.model.KeyboardSettings settings, @org.jetbrains.annotations.NotNull
    kotlin.coroutines.Continuation<? super kotlin.Unit> $completion);
}