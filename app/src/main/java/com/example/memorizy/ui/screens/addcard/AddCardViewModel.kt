package com.example.memorizy.ui.screens.addcard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.memorizy.data.repository.CardRepository
import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.sync.SyncManager
import com.example.memorizy.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AddCardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cardRepository: CardRepository,
    private val syncManager: SyncManager
) : ViewModel(){
    private val route = savedStateHandle.toRoute<Routes.AddCard>()
    private val setId = route.setId

    private val _uiState = MutableStateFlow(AddCardState())

    val uiState: StateFlow<AddCardState> = _uiState.asStateFlow()

    // Изменили Термин
    fun onTermChanged(newTerm: String){
        _uiState.update { currentState ->
            currentState.copy(
                term = newTerm,
                isTermEmptyError = false
            )
        }
    }

    // Изменили Определение
    fun onDefinitionChanged(newDefinition: String){
        _uiState.update { currentState ->
            currentState.copy(
                definition = newDefinition,
                isDefinitionEmptyError = false
            )
        }
    }

    // Нажали Создать
    fun onCreateButtonClicked() {
        val currentState = _uiState.value

        if (currentState.isSaving) return

        if (currentState.term.isBlank() || currentState.definition.isBlank()) {
            _uiState.update { it.copy(isTermEmptyError = true, isDefinitionEmptyError = true) }
            return
        }

        _uiState.update { it.copy(isSaving = true) }

        val newCard = Card(
            setId = setId,
            term = currentState.term,
            definition = currentState.definition
        )

        viewModelScope.launch {
            cardRepository.insertCard(newCard)

            syncManager.scheduleOneTimeSync()

            _uiState.update { it.copy(isCardCreated = true) }
        }
    }
}