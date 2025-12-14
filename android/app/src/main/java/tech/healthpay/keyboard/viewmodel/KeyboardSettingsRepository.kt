package tech.healthpay.keyboard.viewmodel

import tech.healthpay.keyboard.model.KeyboardSettings

/**
 * Repository interface for keyboard settings
 */
interface KeyboardSettingsRepository {
    
    /**
     * Get current keyboard settings
     */
    fun getSettings(): KeyboardSettings
    
    /**
     * Save keyboard settings
     */
    fun saveSettings(settings: KeyboardSettings)
    
    /**
     * Check if biometric auth is enabled
     */
    fun isBiometricEnabled(): Boolean
    
    /**
     * Set biometric auth enabled state
     */
    fun setBiometricEnabled(enabled: Boolean)
    
    /**
     * Check if sounds are enabled
     */
    fun isSoundEnabled(): Boolean
    
    /**
     * Set sounds enabled state
     */
    fun setSoundEnabled(enabled: Boolean)
    
    /**
     * Check if vibration is enabled
     */
    fun isVibrationEnabled(): Boolean
    
    /**
     * Set vibration enabled state
     */
    fun setVibrationEnabled(enabled: Boolean)
    
    /**
     * Check if balance should be shown
     */
    fun isShowBalance(): Boolean
    
    /**
     * Set show balance state
     */
    fun setShowBalance(show: Boolean)
    
    /**
     * Get selected language
     */
    fun getLanguage(): String
    
    /**
     * Set selected language
     */
    fun setLanguage(language: String)
    
    /**
     * Clear all settings
     */
    fun clearAll()
}
