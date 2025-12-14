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
 * ViewModel for Keyboard settings and state
 */
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000B\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0018\u0002\n\u0002\u0018\u0002\n\u0000\n\u0002\u0010\u000b\n\u0002\b\u0003\n\u0002\u0018\u0002\n\u0002\b\u0003\n\u0002\u0010\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\u0005\n\u0002\u0018\u0002\n\u0000\b\u0007\u0018\u00002\u00020\u0001B\u000f\b\u0007\u0012\u0006\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\b\u0010\u0010\u001a\u00020\u0011H\u0002J\u000e\u0010\u0012\u001a\u00020\u00112\u0006\u0010\u0013\u001a\u00020\u0014J\u0006\u0010\u0015\u001a\u00020\u0011J\u0006\u0010\u0016\u001a\u00020\u0011J\u0006\u0010\u0017\u001a\u00020\u0011J\u001a\u0010\u0018\u001a\u00020\u00112\u0012\u0010\u0019\u001a\u000e\u0012\u0004\u0012\u00020\u0007\u0012\u0004\u0012\u00020\u00070\u001aR\u0014\u0010\u0005\u001a\b\u0012\u0004\u0012\u00020\u00070\u0006X\u0082\u0004\u00a2\u0006\u0002\n\u0000R\u0011\u0010\b\u001a\u00020\t8F\u00a2\u0006\u0006\u001a\u0004\b\b\u0010\nR\u0011\u0010\u000b\u001a\u00020\t8F\u00a2\u0006\u0006\u001a\u0004\b\u000b\u0010\nR\u0017\u0010\f\u001a\b\u0012\u0004\u0012\u00020\u00070\r\u00a2\u0006\b\n\u0000\u001a\u0004\b\u000e\u0010\u000fR\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u001b"}, d2 = {"Ltech/healthpay/keyboard/viewmodel/KeyboardViewModel;", "Landroidx/lifecycle/ViewModel;", "settingsRepository", "Ltech/healthpay/keyboard/viewmodel/KeyboardSettingsRepository;", "(Ltech/healthpay/keyboard/viewmodel/KeyboardSettingsRepository;)V", "_settings", "Lkotlinx/coroutines/flow/MutableStateFlow;", "Ltech/healthpay/keyboard/model/KeyboardSettings;", "isHapticFeedbackEnabled", "", "()Z", "isSoundFeedbackEnabled", "settings", "Lkotlinx/coroutines/flow/StateFlow;", "getSettings", "()Lkotlinx/coroutines/flow/StateFlow;", "loadSettings", "", "setDefaultLanguage", "language", "", "toggleBiometricRequired", "toggleHapticFeedback", "toggleSoundFeedback", "updateSetting", "update", "Lkotlin/Function1;", "app_debug"})
@dagger.hilt.android.lifecycle.HiltViewModel
public final class KeyboardViewModel extends androidx.lifecycle.ViewModel {
    @org.jetbrains.annotations.NotNull
    private final tech.healthpay.keyboard.viewmodel.KeyboardSettingsRepository settingsRepository = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.MutableStateFlow<tech.healthpay.keyboard.model.KeyboardSettings> _settings = null;
    @org.jetbrains.annotations.NotNull
    private final kotlinx.coroutines.flow.StateFlow<tech.healthpay.keyboard.model.KeyboardSettings> settings = null;
    
    @javax.inject.Inject
    public KeyboardViewModel(@org.jetbrains.annotations.NotNull
    tech.healthpay.keyboard.viewmodel.KeyboardSettingsRepository settingsRepository) {
        super();
    }
    
    @org.jetbrains.annotations.NotNull
    public final kotlinx.coroutines.flow.StateFlow<tech.healthpay.keyboard.model.KeyboardSettings> getSettings() {
        return null;
    }
    
    public final boolean isHapticFeedbackEnabled() {
        return false;
    }
    
    public final boolean isSoundFeedbackEnabled() {
        return false;
    }
    
    private final void loadSettings() {
    }
    
    public final void updateSetting(@org.jetbrains.annotations.NotNull
    kotlin.jvm.functions.Function1<? super tech.healthpay.keyboard.model.KeyboardSettings, tech.healthpay.keyboard.model.KeyboardSettings> update) {
    }
    
    public final void toggleHapticFeedback() {
    }
    
    public final void toggleSoundFeedback() {
    }
    
    public final void setDefaultLanguage(@org.jetbrains.annotations.NotNull
    java.lang.String language) {
    }
    
    public final void toggleBiometricRequired() {
    }
}