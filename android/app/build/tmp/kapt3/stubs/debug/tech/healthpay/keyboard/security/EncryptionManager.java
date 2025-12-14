package tech.healthpay.keyboard.security;

import android.content.Context;
import android.os.Build;
import android.security.keystore.KeyGenParameterSpec;
import android.security.keystore.KeyProperties;
import android.util.Base64;
import androidx.biometric.BiometricManager;
import androidx.biometric.BiometricPrompt;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKey;
import dagger.hilt.android.qualifiers.ApplicationContext;
import kotlinx.coroutines.Dispatchers;
import tech.healthpay.keyboard.api.ApiResult;
import tech.healthpay.keyboard.api.HealthPayApiClient;
import java.security.KeyStore;
import java.util.concurrent.Executor;
import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Encryption Manager
 *
 * Handles PIN and sensitive data encryption using Android Keystore
 */
@javax.inject.Singleton
@kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000(\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0002\b\b\n\u0002\u0010\u0002\n\u0000\n\u0002\u0018\u0002\n\u0002\b\u0002\b\u0007\u0018\u0000 \u00122\u00020\u0001:\u0001\u0012B\u0011\b\u0007\u0012\b\b\u0001\u0010\u0002\u001a\u00020\u0003\u00a2\u0006\u0002\u0010\u0004J\u000e\u0010\u0005\u001a\u00020\u00062\u0006\u0010\u0007\u001a\u00020\u0006J\u000e\u0010\b\u001a\u00020\u00062\u0006\u0010\t\u001a\u00020\u0006J\u000e\u0010\n\u001a\u00020\u00062\u0006\u0010\u000b\u001a\u00020\u0006J\u000e\u0010\f\u001a\u00020\u00062\u0006\u0010\r\u001a\u00020\u0006J\b\u0010\u000e\u001a\u00020\u000fH\u0002J\b\u0010\u0010\u001a\u00020\u0011H\u0002R\u000e\u0010\u0002\u001a\u00020\u0003X\u0082\u0004\u00a2\u0006\u0002\n\u0000\u00a8\u0006\u0013"}, d2 = {"Ltech/healthpay/keyboard/security/EncryptionManager;", "", "context", "Landroid/content/Context;", "(Landroid/content/Context;)V", "decrypt", "", "encryptedData", "decryptPin", "encryptedPin", "encrypt", "data", "encryptPin", "pin", "generateKeyIfNeeded", "", "getSecretKey", "Ljavax/crypto/SecretKey;", "Companion", "app_debug"})
public final class EncryptionManager {
    @org.jetbrains.annotations.NotNull
    private final android.content.Context context = null;
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String KEYSTORE_ALIAS = "HealthPayKeyAlias";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String ANDROID_KEYSTORE = "AndroidKeyStore";
    @org.jetbrains.annotations.NotNull
    private static final java.lang.String TRANSFORMATION = "AES/GCM/NoPadding";
    private static final int GCM_TAG_LENGTH = 128;
    @org.jetbrains.annotations.NotNull
    public static final tech.healthpay.keyboard.security.EncryptionManager.Companion Companion = null;
    
    @javax.inject.Inject
    public EncryptionManager(@dagger.hilt.android.qualifiers.ApplicationContext
    @org.jetbrains.annotations.NotNull
    android.content.Context context) {
        super();
    }
    
    private final void generateKeyIfNeeded() {
    }
    
    private final javax.crypto.SecretKey getSecretKey() {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String encryptPin(@org.jetbrains.annotations.NotNull
    java.lang.String pin) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String decryptPin(@org.jetbrains.annotations.NotNull
    java.lang.String encryptedPin) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String encrypt(@org.jetbrains.annotations.NotNull
    java.lang.String data) {
        return null;
    }
    
    @org.jetbrains.annotations.NotNull
    public final java.lang.String decrypt(@org.jetbrains.annotations.NotNull
    java.lang.String encryptedData) {
        return null;
    }
    
    @kotlin.Metadata(mv = {1, 9, 0}, k = 1, xi = 48, d1 = {"\u0000\u001a\n\u0002\u0018\u0002\n\u0002\u0010\u0000\n\u0002\b\u0002\n\u0002\u0010\u000e\n\u0000\n\u0002\u0010\b\n\u0002\b\u0003\b\u0086\u0003\u0018\u00002\u00020\u0001B\u0007\b\u0002\u00a2\u0006\u0002\u0010\u0002R\u000e\u0010\u0003\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0005\u001a\u00020\u0006X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\u0007\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000R\u000e\u0010\b\u001a\u00020\u0004X\u0082T\u00a2\u0006\u0002\n\u0000\u00a8\u0006\t"}, d2 = {"Ltech/healthpay/keyboard/security/EncryptionManager$Companion;", "", "()V", "ANDROID_KEYSTORE", "", "GCM_TAG_LENGTH", "", "KEYSTORE_ALIAS", "TRANSFORMATION", "app_debug"})
    public static final class Companion {
        
        private Companion() {
            super();
        }
    }
}