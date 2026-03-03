package com.example.memorizy.data.source.network.dto

import kotlinx.serialization.Serializable

// Входные данные на сервер при авторизации

@Serializable
data class AuthRequest(
    val username: String,
    val password: String
)