package com.example.memorizy.data.repository

import com.example.memorizy.data.source.local.datastore.SettingsDataStore
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Реализация репозитория настройки

class SettingsRepositoryImpl @Inject constructor(
    private val settingsDataStore: SettingsDataStore
) : SettingsRepository {

    override val token: Flow<String?> = settingsDataStore.token
    override val userId: Flow<Long?> = settingsDataStore.userId
    override val username: Flow<String?> = settingsDataStore.username
    override val lastSyncTime: Flow<Long?> = settingsDataStore.lastSyncTime
    override val isDarkTheme: Flow<Boolean> = settingsDataStore.isDarkTheme
    override val notificationsEnabled: Flow<Boolean> = settingsDataStore.notificationsEnabled
    override val hasPromptedForNotifications: Flow<Boolean> = settingsDataStore.hasPromptedForNotifications

    override suspend fun saveUserUtilInfo(token: String, userId: Long) {
        settingsDataStore.saveUserUtilInfo(token, userId)
    }

    override suspend fun saveUsername(username: String) {
        settingsDataStore.saveUsername(username)
    }

    override suspend fun updateLastSyncTime() {
        settingsDataStore.updateLastSyncTime()
    }

    override suspend fun setDarkTheme(isDarkTheme: Boolean) {
        settingsDataStore.setDarkTheme(isDarkTheme)
    }

    override suspend fun setNotificationsEnabled(enabled: Boolean) {
        settingsDataStore.setNotificationsEnabled(enabled)
    }

    override suspend fun setHasPromptedForNotifications(prompted: Boolean) {
        settingsDataStore.setHasPromptedForNotifications(prompted)
    }

    override suspend fun setLastNotificationTime(time: Long) {
        settingsDataStore.setLastNotificationTime(time)
    }

    override suspend fun getLastNotificationTime(): Long {
        return settingsDataStore.getLastNotificationTime()
    }

    override suspend fun deleteToken() {
        settingsDataStore.deleteToken()
    }
}
