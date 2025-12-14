package tech.healthpay.keyboard.ime;

import dagger.MembersInjector;
import dagger.internal.DaggerGenerated;
import dagger.internal.InjectedFieldSignature;
import dagger.internal.QualifierMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import tech.healthpay.keyboard.api.HealthPayApiClient;
import tech.healthpay.keyboard.security.AuthenticationManager;
import tech.healthpay.keyboard.security.BiometricHelper;
import tech.healthpay.keyboard.viewmodel.KeyboardViewModel;
import tech.healthpay.keyboard.viewmodel.WalletViewModel;

@QualifierMetadata
@DaggerGenerated
@Generated(
    value = "dagger.internal.codegen.ComponentProcessor",
    comments = "https://dagger.dev"
)
@SuppressWarnings({
    "unchecked",
    "rawtypes",
    "KotlinInternal",
    "KotlinInternalInJava"
})
public final class HealthPayInputMethodService_MembersInjector implements MembersInjector<HealthPayInputMethodService> {
  private final Provider<AuthenticationManager> authManagerProvider;

  private final Provider<HealthPayApiClient> apiClientProvider;

  private final Provider<BiometricHelper> biometricHelperProvider;

  private final Provider<WalletViewModel> walletViewModelProvider;

  private final Provider<KeyboardViewModel> keyboardViewModelProvider;

  public HealthPayInputMethodService_MembersInjector(
      Provider<AuthenticationManager> authManagerProvider,
      Provider<HealthPayApiClient> apiClientProvider,
      Provider<BiometricHelper> biometricHelperProvider,
      Provider<WalletViewModel> walletViewModelProvider,
      Provider<KeyboardViewModel> keyboardViewModelProvider) {
    this.authManagerProvider = authManagerProvider;
    this.apiClientProvider = apiClientProvider;
    this.biometricHelperProvider = biometricHelperProvider;
    this.walletViewModelProvider = walletViewModelProvider;
    this.keyboardViewModelProvider = keyboardViewModelProvider;
  }

  public static MembersInjector<HealthPayInputMethodService> create(
      Provider<AuthenticationManager> authManagerProvider,
      Provider<HealthPayApiClient> apiClientProvider,
      Provider<BiometricHelper> biometricHelperProvider,
      Provider<WalletViewModel> walletViewModelProvider,
      Provider<KeyboardViewModel> keyboardViewModelProvider) {
    return new HealthPayInputMethodService_MembersInjector(authManagerProvider, apiClientProvider, biometricHelperProvider, walletViewModelProvider, keyboardViewModelProvider);
  }

  @Override
  public void injectMembers(HealthPayInputMethodService instance) {
    injectAuthManager(instance, authManagerProvider.get());
    injectApiClient(instance, apiClientProvider.get());
    injectBiometricHelper(instance, biometricHelperProvider.get());
    injectWalletViewModel(instance, walletViewModelProvider.get());
    injectKeyboardViewModel(instance, keyboardViewModelProvider.get());
  }

  @InjectedFieldSignature("tech.healthpay.keyboard.ime.HealthPayInputMethodService.authManager")
  public static void injectAuthManager(HealthPayInputMethodService instance,
      AuthenticationManager authManager) {
    instance.authManager = authManager;
  }

  @InjectedFieldSignature("tech.healthpay.keyboard.ime.HealthPayInputMethodService.apiClient")
  public static void injectApiClient(HealthPayInputMethodService instance,
      HealthPayApiClient apiClient) {
    instance.apiClient = apiClient;
  }

  @InjectedFieldSignature("tech.healthpay.keyboard.ime.HealthPayInputMethodService.biometricHelper")
  public static void injectBiometricHelper(HealthPayInputMethodService instance,
      BiometricHelper biometricHelper) {
    instance.biometricHelper = biometricHelper;
  }

  @InjectedFieldSignature("tech.healthpay.keyboard.ime.HealthPayInputMethodService.walletViewModel")
  public static void injectWalletViewModel(HealthPayInputMethodService instance,
      WalletViewModel walletViewModel) {
    instance.walletViewModel = walletViewModel;
  }

  @InjectedFieldSignature("tech.healthpay.keyboard.ime.HealthPayInputMethodService.keyboardViewModel")
  public static void injectKeyboardViewModel(HealthPayInputMethodService instance,
      KeyboardViewModel keyboardViewModel) {
    instance.keyboardViewModel = keyboardViewModel;
  }
}
