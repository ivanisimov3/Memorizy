package com.example.memorizy.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorizy.data.notification.NotificationScheduler
import com.example.memorizy.data.repository.AuthRepository
import com.example.memorizy.data.repository.SettingsRepository
import com.example.memorizy.data.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    val uiState: StateFlow<SettingsState> = combine(
        settingsRepository.token,
        settingsRepository.username,
        settingsRepository.lastSyncTime,
        settingsRepository.isDarkTheme,
        settingsRepository.notificationsEnabled
    ) { token, username, syncTime, isDarkTheme, notificationsEnabled ->
        SettingsState(
            isLoggedIn = (token != null),
            username = username,
            lastSyncTime = syncTime,
            isDarkTheme = isDarkTheme,
            notificationsEnabled = notificationsEnabled
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsState()
    )

    // Нажали Выйти
    fun onLogout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    // Нажали Синхронизировать
    fun onSyncNow() {
        syncManager.scheduleOneTimeSync()
    }

    // Нажали Переключатель темы
    fun onThemeChanged(isDark: Boolean) {
        viewModelScope.launch {
            settingsRepository.setDarkTheme(isDark)
        }
    }

    // Нажали Переключатель уведомлений
    fun onNotificationsChanged(enabled: Boolean) {
        viewModelScope.launch {
            settingsRepository.setNotificationsEnabled(enabled)
            if (enabled) {
                notificationScheduler.startPeriodicReminders()
            } else {
                notificationScheduler.cancelPeriodicReminders()
            }
        }
    }
}