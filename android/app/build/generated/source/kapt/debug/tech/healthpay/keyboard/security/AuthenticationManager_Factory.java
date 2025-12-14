package tech.healthpay.keyboard.security;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import tech.healthpay.keyboard.api.HealthPayApiClient;

@ScopeMetadata("javax.inject.Singleton")
@QualifierMetadata("dagger.hilt.android.qualifiers.ApplicationContext")
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
public final class AuthenticationManager_Factory implements Factory<AuthenticationManager> {
  private final Provider<Context> contextProvider;

  private final Provider<TokenManager> tokenManagerProvider;

  private final Provider<HealthPayApiClient> apiClientProvider;

  public AuthenticationManager_Factory(Provider<Context> contextProvider,
      Provider<TokenManager> tokenManagerProvider, Provider<HealthPayApiClient> apiClientProvider) {
    this.contextProvider = contextProvider;
    this.tokenManagerProvider = tokenManagerProvider;
    this.apiClientProvider = apiClientProvider;
  }

  @Override
  public AuthenticationManager get() {
    return newInstance(contextProvider.get(), tokenManagerProvider.get(), apiClientProvider.get());
  }

  public static AuthenticationManager_Factory create(Provider<Context> contextProvider,
      Provider<TokenManager> tokenManagerProvider, Provider<HealthPayApiClient> apiClientProvider) {
    return new AuthenticationManager_Factory(contextProvider, tokenManagerProvider, apiClientProvider);
  }

  public static AuthenticationManager newInstance(Context context, TokenManager tokenManager,
      HealthPayApiClient apiClient) {
    return new AuthenticationManager(context, tokenManager, apiClient);
  }
}
