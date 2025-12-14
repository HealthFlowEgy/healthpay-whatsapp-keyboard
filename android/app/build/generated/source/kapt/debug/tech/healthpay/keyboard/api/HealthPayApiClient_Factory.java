package tech.healthpay.keyboard.api;

import android.content.Context;
import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;
import tech.healthpay.keyboard.security.TokenManager;

@ScopeMetadata("javax.inject.Singleton")
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
public final class HealthPayApiClient_Factory implements Factory<HealthPayApiClient> {
  private final Provider<Context> contextProvider;

  private final Provider<TokenManager> tokenManagerProvider;

  public HealthPayApiClient_Factory(Provider<Context> contextProvider,
      Provider<TokenManager> tokenManagerProvider) {
    this.contextProvider = contextProvider;
    this.tokenManagerProvider = tokenManagerProvider;
  }

  @Override
  public HealthPayApiClient get() {
    return newInstance(contextProvider.get(), tokenManagerProvider.get());
  }

  public static HealthPayApiClient_Factory create(Provider<Context> contextProvider,
      Provider<TokenManager> tokenManagerProvider) {
    return new HealthPayApiClient_Factory(contextProvider, tokenManagerProvider);
  }

  public static HealthPayApiClient newInstance(Context context, TokenManager tokenManager) {
    return new HealthPayApiClient(context, tokenManager);
  }
}
