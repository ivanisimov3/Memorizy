package com.example.memorizy.ui.screens.settings

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorizy.data.notification.NotificationScheduler
import com.example.memorizy.data.repository.AuthRepository
import com.example.memorizy.data.repository.SettingsRepository
import com.example.memorizy.data.sync.SyncCoordinator
import com.example.memorizy.data.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

private data class LogoutSyncState(
    val isInProgress: Boolean = false,
    val showErrorDialog: Boolean = false
)

@HiltViewModel
class SettingsViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val authRepository: AuthRepository,
    private val syncCoordinator: SyncCoordinator,
    private val syncManager: SyncManager,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {
    
    private val logoutSyncState = MutableStateFlow(LogoutSyncState())

    private val accountState = combine(
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
    }

    val uiState: StateFlow<SettingsState> = combine(
        accountState,
        logoutSyncState
    ) { state, logoutState ->
        state.copy(
            isLogoutInProgress = logoutState.isInProgress,
            showLogoutSyncErrorDialog = logoutState.showErrorDialog
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsState()
    )

    // Нажали Выйти
    fun onLogout() {
        if (logoutSyncState.value.isInProgress) return

        viewModelScope.launch {
            logoutSyncState.update {
                it.copy(isInProgress = true, showErrorDialog = false)
            }

            val syncResult = runCatching {
                syncCoordinator.syncAll()
            }

            if (syncResult.isSuccess) {
                authRepository.logout()
            } else {
                logoutSyncState.update {
                    it.copy(showErrorDialog = true)
                }
            }

            logoutSyncState.update {
                it.copy(isInProgress = false)
            }
        }
    }

    // Нажали Выйти без синхронизации
    fun onLogoutAnyway() {
        viewModelScope.launch {
            logoutSyncState.update {
                it.copy(showErrorDialog = false)
            }
            authRepository.logout()
        }
    }

    // Нажали Остаться
    fun onDismissLogoutSyncError() {
        logoutSyncState.update {
            it.copy(showErrorDialog = false)
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