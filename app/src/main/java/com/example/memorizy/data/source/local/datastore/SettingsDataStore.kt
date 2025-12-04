package com.example.memorizy.data.source.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.memorizy.data.source.local.datastore.SettingsDataStore.PreferencesKeys.IS_DARK_THEME_KEY
import com.example.memorizy.data.source.local.datastore.SettingsDataStore.PreferencesKeys.LAST_SYNC_KEY
import com.example.memorizy.data.source.local.datastore.SettingsDataStore.PreferencesKeys.TOKEN_KEY
import com.example.memorizy.data.source.local.datastore.SettingsDataStore.PreferencesKeys.USERNAME_KEY
import com.example.memorizy.data.source.local.datastore.SettingsDataStore.PreferencesKeys.USER_ID_KEY
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/*
To create a DataStore instance we use the preferencesDataStore delegate,
with the Context as receiver.
*/
private val Context.dataStore by preferencesDataStore(name = "settings")

// SettingsDataStore should get a DataStore instance as a constructor parameter (@inject constructor)
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
        val USER_ID_KEY = longPreferencesKey("user_id")
        val USERNAME_KEY = stringPreferencesKey("username")
        val LAST_SYNC_KEY = longPreferencesKey("last_sync_time")
        val IS_DARK_THEME_KEY = booleanPreferencesKey("is_dark_theme")
    }

    // Reading data from Preferences DataStore
    val token: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[TOKEN_KEY]
        }

    val userId: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID_KEY]
        }

    val username: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[USERNAME_KEY]
        }

    val lastSyncTime: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[LAST_SYNC_KEY]
        }

    val isDarkTheme: Flow<Boolean> = context.dataStore.data
        .map { preferences ->
            preferences[IS_DARK_THEME_KEY] ?: false
        }

    // Writing data to Preferences DataStore
    suspend fun saveUserUtilInfo(token: String, userId: Long) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
        }
    }

    suspend fun saveUsername(username: String) {
        context.dataStore.edit { preferences ->
            preferences[USERNAME_KEY] = username
        }
    }

    suspend fun updateLastSyncTime() {
        context.dataStore.edit { preferences ->
            preferences[LAST_SYNC_KEY] = System.currentTimeMillis()
        }
    }

    suspend fun setDarkTheme(isDarkTheme: Boolean) {
        context.dataStore.edit { preferences ->
            preferences[IS_DARK_THEME_KEY] = isDarkTheme
        }
    }

    // Deleting data from Preferences DataStore
    suspend fun deleteToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
            preferences.remove(USERNAME_KEY)
            preferences.remove(LAST_SYNC_KEY)
        }
    }
}