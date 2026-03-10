package com.example.memorizy.data.repository

import kotlinx.coroutines.flow.Flow

// Интерфейс репозитория настройки

interface SettingsRepository {
    val token: Flow<String?>
    val userId: Flow<Long?>
    val username: Flow<String?>
    val lastSyncTime: Flow<Long?>
    val isDarkTheme: Flow<Boolean>
    val notificationsEnabled: Flow<Boolean>
    val hasPromptedForNotifications: Flow<Boolean>

    suspend fun saveUserUtilInfo(token: String, userId: Long)
    suspend fun saveUsername(username: String)
    suspend fun updateLastSyncTime()
    suspend fun setDarkTheme(isDarkTheme: Boolean)
    suspend fun setNotificationsEnabled(enabled: Boolean)
    suspend fun setHasPromptedForNotifications(prompted: Boolean)
    suspend fun setLastNotificationTime(time: Long)
    suspend fun getLastNotificationTime(): Long
    suspend fun deleteToken()
}
