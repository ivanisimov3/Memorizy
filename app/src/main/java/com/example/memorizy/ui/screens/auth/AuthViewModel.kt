package com.example.memorizy.ui.screens.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorizy.data.repository.AuthRepository
import com.example.memorizy.data.source.network.dto.AuthRequest
import com.example.memorizy.data.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val repository: AuthRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val _uiState = MutableStateFlow(AuthState())

    val uiState = _uiState.asStateFlow()

    // Изменение в строке имени пользователя
    fun onUsernameChanged(value: String) {
        _uiState.update { currentState ->
            currentState.copy(
                username = value,
                error = null
            )
        }
    }

    // Изменение в строке пароля
    fun onPasswordChanged(value: String) {
        _uiState.update { currentState ->
            currentState.copy(
                password = value,
                error = null
            )
        }
    }

    // Нажали кнопку Логин
    fun onLoginClick() {
        val action: suspend (AuthRequest) -> Result<Unit> = { request ->
            repository.login(request)
        }
        performAuthAction(action)
    }

    // Нажали кнопку зарегестироваться
    fun onRegisterClick() {
        val action: suspend (AuthRequest) -> Result<Unit> = { request ->
            repository.register(request)
        }
        performAuthAction(action)
    }

    private fun performAuthAction(
        action: suspend (AuthRequest) -> Result<Unit>
    ) {
        val currentState = _uiState.value

        if (currentState.username.isBlank() || currentState.password.isBlank()) {
            _uiState.update { it.copy(error = "Заполните все поля") }
            return
        }

        viewModelScope.launch { // Обращаемся к серверу
            _uiState.update { it.copy(isLoading = true, error = null) }

            val request = AuthRequest(currentState.username, currentState.password)
            val result = action(request)

            if (result.isSuccess) {
                syncManager.scheduleOneTimeSync()

                _uiState.update { it.copy(isLoading = false, isAuthenticated = true) }
            } else {
                val errorMsg = result.exceptionOrNull()?.message ?: "Ошибка сети"
                _uiState.update { it.copy(isLoading = false, error = errorMsg) }
            }
        }
    }
}