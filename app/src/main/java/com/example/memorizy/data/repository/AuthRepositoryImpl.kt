package com.example.memorizy.data.repository

import com.example.memorizy.data.source.local.datastore.TokenManager
import com.example.memorizy.data.source.network.MemorizyApiService
import com.example.memorizy.data.source.network.dto.AuthRequest
import jakarta.inject.Inject

// Конкретная реализация для работы с AuthRequest (Default implementation)
class AuthRepositoryImpl @Inject constructor(   // Inject coonstructor связывает с MemorizyApiService и TokenManager
    private val api: MemorizyApiService,
    private val tokenManager: TokenManager
) : AuthRepository {

    override suspend fun register(request: AuthRequest): Result<Unit> {
        return try {
            val response = api.register(request)
            tokenManager.saveToken(response.token)

            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()

            Result.failure(e)
        }
    }

    override suspend fun login(request: AuthRequest): Result<Unit> {
        return try {
            val response = api.login(request)
            tokenManager.saveToken(response.token)

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