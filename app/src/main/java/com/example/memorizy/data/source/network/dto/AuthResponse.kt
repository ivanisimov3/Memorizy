package com.example.memorizy.data.source.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class AuthResponse(
    val token: String
)