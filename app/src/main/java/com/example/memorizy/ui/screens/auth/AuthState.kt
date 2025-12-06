package com.example.memorizy.ui.screens.auth

data class AuthState(
    val username: String = "",
    val password: String = "",
    val isLoading: Boolean = false,
    val error: Int? = null,
    val isAuthenticated: Boolean = false
)