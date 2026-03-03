package com.example.memorizy.data.source.network.dto

import kotlinx.serialization.Serializable

// Выходные данные из сервера при авторизации

@Serializable
data class AuthResponse(
    val token: String,
    val userId: Long
)