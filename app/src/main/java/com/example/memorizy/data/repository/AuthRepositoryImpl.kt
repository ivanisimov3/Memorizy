package com.example.memorizy.data.repository

import com.example.memorizy.data.source.local.datastore.TokenManager
import com.example.memorizy.data.source.local.room.dao.CardDao
import com.example.memorizy.data.source.local.room.dao.StudySetDao
import com.example.memorizy.data.source.network.MemorizyApiService
import com.example.memorizy.data.source.network.dto.AuthRequest
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first

// Конкретная реализация для работы с AuthRequest (Default implementation)
class AuthRepositoryImpl @Inject constructor(   // Inject coonstructor связывает с MemorizyApiService и TokenManager
    private val api: MemorizyApiService,
    private val tokenManager: TokenManager,
    private val studySetDao: StudySetDao,
) : AuthRepository {

    override suspend fun register(request: AuthRequest): Result<Unit> {
        return try {
            val response = api.register(request)
            val newUserId = response.userId

            val lastUserId = tokenManager.userId.first()

            // Если уже кто то был залогинен и это не он сейчас входит
            if (lastUserId != null && lastUserId != newUserId){
                studySetDao.clearAll()  // Очищаем все наборы на устройстве
            }

            tokenManager.saveToken(response.token, newUserId)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()

            Result.failure(e)
        }
    }

    override suspend fun login(request: AuthRequest): Result<Unit> {
        return try {
            val response = api.login(request)

            val newUserId = response.userId

            val lastUserId = tokenManager.userId.first()

            // Если уже кто то был залогинен и это не он сейчас входит
            if (lastUserId != null && lastUserId != newUserId){
                studySetDao.clearAll()  // Очищаем все наборы на устройстве
            }

            tokenManager.saveToken(response.token, newUserId)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()

            Result.failure(e)
        }
    }

    override suspend fun logout() {
        tokenManager.deleteToken()
    }
}