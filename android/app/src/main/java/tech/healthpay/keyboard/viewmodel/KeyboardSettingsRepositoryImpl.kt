package tech.healthpay.keyboard.viewmodel

import android.content.SharedPreferences
import tech.healthpay.keyboard.model.KeyboardSettings

/**
 * Implementation of KeyboardSettingsRepository using SharedPreferences
 */
class KeyboardSettingsRepositoryImpl(
    private val prefs: SharedPreferences
) : KeyboardSettingsRepository {

    override fun getSettings(): KeyboardSettings {
        return KeyboardSettings(
            biometricEnabled = isBiometricEnabled(),
            soundEnabled = isSoundEnabled(),
            vibrationEnabled = isVibrationEnabled(),
            showBalance = isShowBalance(),
            language = getLanguage()
        )
    }

    override fun saveSettings(settings: KeyboardSettings) {
        prefs.edit().apply {
            putBoolean(KEY_BIOMETRIC_ENABLED, settings.biometricEnabled)
            putBoolean(KEY_SOUND_ENABLED, settings.soundEnabled)
            putBoolean(KEY_VIBRATION_ENABLED, settings.vibrationEnabled)
            putBoolean(KEY_SHOW_BALANCE, settings.showBalance)
            putString(KEY_LANGUAGE, settings.language)
            apply()
        }
    }

    override fun isBiometricEnabled(): Boolean {
        return prefs.getBoolean(KEY_BIOMETRIC_ENABLED, true)
    }

    override fun setBiometricEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_BIOMETRIC_ENABLED, enabled).apply()
    }

    override fun isSoundEnabled(): Boolean {
        return prefs.getBoolean(KEY_SOUND_ENABLED, true)
    }

    override fun setSoundEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    override fun isVibrationEnabled(): Boolean {
        return prefs.getBoolean(KEY_VIBRATION_ENABLED, true)
    }

    override fun setVibrationEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_VIBRATION_ENABLED, enabled).apply()
    }

    override fun isShowBalance(): Boolean {
        return prefs.getBoolean(KEY_SHOW_BALANCE, true)
    }

    override fun setShowBalance(show: Boolean) {
        prefs.edit().putBoolean(KEY_SHOW_BALANCE, show).apply()
    }

    override fun getLanguage(): String {
        return prefs.getString(KEY_LANGUAGE, DEFAULT_LANGUAGE) ?: DEFAULT_LANGUAGE
    }

    override fun setLanguage(language: String) {
        prefs.edit().putString(KEY_LANGUAGE, language).apply()
    }

    override fun clearAll() {
        prefs.edit().clear().apply()
    }

    companion object {
        private const val KEY_BIOMETRIC_ENABLED = "biometric_enabled"
        private const val KEY_SOUND_ENABLED = "sound_enabled"
        private const val KEY_VIBRATION_ENABLED = "vibration_enabled"
        private const val KEY_SHOW_BALANCE = "show_balance"
        private const val KEY_LANGUAGE = "language"
        private const val DEFAULT_LANGUAGE = "en"
    }
}
