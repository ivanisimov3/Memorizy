package com.example.memorizy.data.source.local.datastore

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.memorizy.data.source.local.datastore.TokenManager.PreferencesKeys.TOKEN_KEY
import com.example.memorizy.data.source.local.datastore.TokenManager.PreferencesKeys.USER_ID_KEY
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

// TokenManager should get a DataStore instance as a constructor parameter (@inject constructor)
@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    private object PreferencesKeys {
        val TOKEN_KEY = stringPreferencesKey("jwt_token")
        val USER_ID_KEY = longPreferencesKey("user_id")
    }

    // Reading data from Preferences DataStore
    val tokenKey: Flow<String?> = context.dataStore.data
        .map { preferences ->
            preferences[TOKEN_KEY]
        }

    // Reading data from Preferences DataStore
    val userId: Flow<Long?> = context.dataStore.data
        .map { preferences ->
            preferences[USER_ID_KEY]
        }

    // Writing data to Preferences DataStore
    suspend fun saveToken(token: String, userId: Long) {
        context.dataStore.edit { preferences ->
            preferences[TOKEN_KEY] = token
            preferences[USER_ID_KEY] = userId
        }
    }

    // Deleting data from Preferences DataStore
    suspend fun deleteToken() {
        context.dataStore.edit { preferences ->
            preferences.remove(TOKEN_KEY)
        }
    }
}