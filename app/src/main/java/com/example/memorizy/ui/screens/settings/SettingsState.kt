package com.example.memorizy.ui.screens.settings

data class SettingsState(
    val isLoggedIn: Boolean = false,
    val username: String? = null,
    val lastSyncTime: Long? = null,
    val isDarkTheme: Boolean = false,
    val notificationsEnabled: Boolean = false
)