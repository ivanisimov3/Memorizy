package com.example.memorizy.data.repository

import com.example.memorizy.data.source.network.dto.AuthRequest

// Interface to the data layer (AuthRequest).
interface AuthRepository {

    // Result<Unit> - стандартный класс Kotlin, который хранит либо успех, либо ошибку.

    suspend fun register(request: AuthRequest): Result<Unit>

    suspend fun login(request: AuthRequest): Result<Unit>

    suspend fun logout()
}