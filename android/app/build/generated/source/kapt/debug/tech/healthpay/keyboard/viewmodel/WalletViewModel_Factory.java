package tech.healthpay.keyboard.viewmodel;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import tech.healthpay.keyboard.api.HealthPayApiClient;
import tech.healthpay.keyboard.security.AuthenticationManager;
import tech.healthpay.keyboard.security.EncryptionManager;

@ScopeMetadata
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
public final class WalletViewModel_Factory implements Factory<WalletViewModel> {
  private final Provider<HealthPayApiClient> apiClientProvider;

  private final Provider<AuthenticationManager> authManagerProvider;

  private final Provider<EncryptionManager> encryptionManagerProvider;

  public WalletViewModel_Factory(Provider<HealthPayApiClient> apiClientProvider,
      Provider<AuthenticationManager> authManagerProvider,
      Provider<EncryptionManager> encryptionManagerProvider) {
    this.apiClientProvider = apiClientProvider;
    this.authManagerProvider = authManagerProvider;
    this.encryptionManagerProvider = encryptionManagerProvider;
  }

  @Override
  public WalletViewModel get() {
    return newInstance(apiClientProvider.get(), authManagerProvider.get(), encryptionManagerProvider.get());
  }

  public static WalletViewModel_Factory create(Provider<HealthPayApiClient> apiClientProvider,
      Provider<AuthenticationManager> authManagerProvider,
      Provider<EncryptionManager> encryptionManagerProvider) {
    return new WalletViewModel_Factory(apiClientProvider, authManagerProvider, encryptionManagerProvider);
  }

  public static WalletViewModel newInstance(HealthPayApiClient apiClient,
      AuthenticationManager authManager, EncryptionManager encryptionManager) {
    return new WalletViewModel(apiClient, authManager, encryptionManager);
  }
}
