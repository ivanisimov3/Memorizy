package com.example.memorizy.ui.screens.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorizy.data.repository.AuthRepository
import com.example.memorizy.data.source.local.datastore.SettingsDataStore
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
    private val settingsDataStore: SettingsDataStore,
    private val authRepository: AuthRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    val uiState: StateFlow<SettingsState> = combine(
        settingsDataStore.token,
        settingsDataStore.username,
        settingsDataStore.lastSyncTime,
        settingsDataStore.isDarkTheme
    ) { token, username, syncTime, isDarkTheme ->
        SettingsState(
            isLoggedIn = (token != null),
            username = username,
            lastSyncTime = syncTime,
            isDarkTheme = isDarkTheme
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = SettingsState()
    )

    fun onLogout() {
        viewModelScope.launch {
            authRepository.logout()
        }
    }

    fun onSyncNow() {
        syncManager.scheduleOneTimeSync()
    }

    fun onThemeChanged(isDark: Boolean) {
        viewModelScope.launch {
            settingsDataStore.setDarkTheme(isDark)
        }
    }
}