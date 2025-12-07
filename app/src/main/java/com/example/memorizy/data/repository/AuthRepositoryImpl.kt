package com.example.memorizy.data.repository

import com.example.memorizy.data.source.local.datastore.SettingsDataStore
import com.example.memorizy.data.source.local.room.dao.StudySetDao
import com.example.memorizy.data.source.network.MemorizyApiService
import com.example.memorizy.data.source.network.dto.AuthRequest
import com.example.memorizy.data.source.network.dto.AuthResponse
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first

// Конкретная реализация для работы с AuthRequest (Default implementation)
class AuthRepositoryImpl @Inject constructor(   // Inject coonstructor связывает с MemorizyApiService и SettingsDataStore
    private val api: MemorizyApiService,
    private val settingsDataStore: SettingsDataStore,
    private val studySetDao: StudySetDao,
) : AuthRepository {

    override suspend fun register(request: AuthRequest): Result<Unit> {
        return authAction(request) { api.register(it) }
    }

    override suspend fun login(request: AuthRequest): Result<Unit> {
        return authAction(request) { api.login(it) }
    }

    private suspend fun authAction(
        request: AuthRequest,
        apiCall: suspend (AuthRequest) -> AuthResponse
    ): Result<Unit> {
        return try {
            val response = apiCall(request)

            val newUserId = response.userId
            val lastUserId = settingsDataStore.userId.first()

            // Если уже кто то был залогинен и это не он сейчас входит
            if (lastUserId != newUserId) {
                studySetDao.clearSyncedData()   // Очищаем все наборы, связанные с другим аккаунтом
            }

            settingsDataStore.saveUserUtilInfo(response.token, newUserId)
            settingsDataStore.saveUsername(request.username)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()

            Result.failure(e)
        }
    }

    override suspend fun logout() {
        settingsDataStore.deleteToken()
    }
}