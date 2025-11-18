package com.example.memorizy.ui.addcard

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.memorizy.data.cardrepository.CardRepository
import com.example.memorizy.data.source.local.card.Card
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
    private val cardRepository: CardRepository
) : ViewModel(){
    private val route = savedStateHandle.toRoute<Routes.AddCard>()
    private val setId = route.setId

    private val _uiState = MutableStateFlow(AddCardState())

    val uiState: StateFlow<AddCardState> = _uiState.asStateFlow()

    fun onTermChanged(newTerm: String){
        _uiState.update { currentState ->
            currentState.copy(
                term = newTerm,
                isTermEmptyError = false
            )
        }
    }

    fun onDefinitionChanged(newDefinition: String){
        _uiState.update { currentState ->
            currentState.copy(
                definition = newDefinition,
                isDefinitionEmptyError = false
            )
        }
    }

    // Нажали кнопку создать
    fun onCreateButtonClicked() {
        val currentState = _uiState.value

        if (currentState.term.isBlank() || currentState.definition.isBlank()) {
            _uiState.update { it.copy(isTermEmptyError = true, isDefinitionEmptyError = true) }
            return
        }

        val newCard = Card(
            setId = setId,
            term = currentState.term,
            definition = currentState.definition
        )

        viewModelScope.launch { // обращаемся к бд поэтому корутина
            cardRepository.insertCard(newCard)
            _uiState.update { it.copy(isCardCreated = true) }
        }
    }
}