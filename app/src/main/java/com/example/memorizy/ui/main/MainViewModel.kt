package com.example.memorizy.ui.main

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorizy.data.notification.NotificationScheduler
import com.example.memorizy.data.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class MainViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository,
    private val notificationScheduler: NotificationScheduler
) : ViewModel() {

    private val _shouldRequestPermission = MutableStateFlow(false)

    init {
        viewModelScope.launch {
            val hasPrompted = settingsRepository.hasPromptedForNotifications.first()
            if (!hasPrompted) {
                _shouldRequestPermission.value = true
            }
        }
    }

    val uiState: StateFlow<MainState> = combine(
        settingsRepository.isDarkTheme,
        _shouldRequestPermission
    ) { isDark, shouldRequest ->
        MainState(
            isDarkTheme = isDark,
            shouldRequestNotificationPermission = shouldRequest
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = MainState()
    )

    // Обработка результата запроса разрешения на уведомления
    fun onNotificationPermissionResult(granted: Boolean) {
        _shouldRequestPermission.value = false
        viewModelScope.launch {
            settingsRepository.setHasPromptedForNotifications(true)
            
            if (granted) {
                settingsRepository.setNotificationsEnabled(true)
                notificationScheduler.startPeriodicReminders()
            } else {
                settingsRepository.setNotificationsEnabled(false)
            }
        }
    }
}