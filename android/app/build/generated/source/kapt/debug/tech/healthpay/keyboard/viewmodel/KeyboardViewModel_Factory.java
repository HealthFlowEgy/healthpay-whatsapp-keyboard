package tech.healthpay.keyboard.viewmodel;

import dagger.internal.DaggerGenerated;
import dagger.internal.Factory;
import dagger.internal.QualifierMetadata;
import dagger.internal.ScopeMetadata;
import javax.annotation.processing.Generated;
import javax.inject.Provider;

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
public final class KeyboardViewModel_Factory implements Factory<KeyboardViewModel> {
  private final Provider<KeyboardSettingsRepository> settingsRepositoryProvider;

  public KeyboardViewModel_Factory(
      Provider<KeyboardSettingsRepository> settingsRepositoryProvider) {
    this.settingsRepositoryProvider = settingsRepositoryProvider;
  }

  @Override
  public KeyboardViewModel get() {
    return newInstance(settingsRepositoryProvider.get());
  }

  public static KeyboardViewModel_Factory create(
      Provider<KeyboardSettingsRepository> settingsRepositoryProvider) {
    return new KeyboardViewModel_Factory(settingsRepositoryProvider);
  }

  public static KeyboardViewModel newInstance(KeyboardSettingsRepository settingsRepository) {
    return new KeyboardViewModel(settingsRepository);
  }
}
