package com.ludoblitz.app.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.ludoblitz.app.data.local.PreferenceManager
import com.ludoblitz.app.data.local.LocalUserRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Settings ViewModel - Manages app settings and preferences
 */
@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val preferenceManager: PreferenceManager,
    private val userRepository: LocalUserRepository
) : ViewModel() {

    // Events
    private val _events = MutableSharedFlow<SettingsEvent>()
    val events: SharedFlow<SettingsEvent> = _events

    // Settings flows
    val isSoundEnabled = preferenceManager.isSoundEnabled
    val isMusicEnabled = preferenceManager.isMusicEnabled
    val isVibrationEnabled = preferenceManager.isVibrationEnabled
    val isDarkMode = preferenceManager.isDarkMode
    val isNotificationsEnabled = preferenceManager.isNotificationsEnabled
    val isPremium = preferenceManager.isPremiumUser

    fun setSoundEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setSoundEnabled(enabled)
        }
    }

    fun setMusicEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setMusicEnabled(enabled)
        }
    }

    fun setVibrationEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setVibrationEnabled(enabled)
        }
    }

    fun setDarkMode(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setDarkMode(enabled)
        }
    }

    fun setNotificationsEnabled(enabled: Boolean) {
        viewModelScope.launch {
            preferenceManager.setNotificationsEnabled(enabled)
        }
    }

    fun setSoundVolume(volume: Float) {
        viewModelScope.launch {
            // Store volume in preferences
        }
    }

    fun setMusicVolume(volume: Float) {
        viewModelScope.launch {
            // Store volume in preferences
        }
    }

    fun setLanguage(language: String) {
        viewModelScope.launch {
            preferenceManager.setLanguage(language)
            _events.emit(SettingsEvent.LanguageChanged(language))
        }
    }

    fun purchaseRemoveAds() {
        viewModelScope.launch {
            // In a real app, this would trigger billing flow
            // For now, we'll simulate it
            _events.emit(SettingsEvent.PurchaseStarted)
        }
    }

    fun restorePurchases() {
        viewModelScope.launch {
            // Restore purchases from billing
            _events.emit(SettingsEvent.RestoreComplete)
        }
    }

    fun resetProgress() {
        viewModelScope.launch {
            userRepository.clearUserData()
            preferenceManager.clearAll()
            _events.emit(SettingsEvent.ProgressReset)
        }
    }
}

sealed class SettingsEvent {
    data class LanguageChanged(val language: String) : SettingsEvent()
    object PurchaseStarted : SettingsEvent()
    object PurchaseComplete : SettingsEvent()
    object RestoreComplete : SettingsEvent()
    object ProgressReset : SettingsEvent()
    data class Error(val message: String) : SettingsEvent()
}
