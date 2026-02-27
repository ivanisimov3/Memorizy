package com.example.memorizy.ui.screens.setdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.memorizy.data.repository.CardRepository
import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.repository.StudySetRepository
import com.example.memorizy.data.sync.SyncManager
import com.example.memorizy.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class SetDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle, // Аргументы навигации
    private val studySetRepository: StudySetRepository,
    private val cardRepository: CardRepository,
    private val syncManager: SyncManager
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Routes.SetDetails>()
    private val setId = route.setId

    private val _uiState = MutableStateFlow(SetDetailsState())
    val uiState = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            studySetRepository.getSet(setId).collect { set ->
                _uiState.update { state ->
                    state.copy(
                        studySet = set,
                        isLoading = (set == null)
                    )
                }
            }
        }

        viewModelScope.launch {
            cardRepository.getAllCardsForSet(setId).collect { cards ->
                _uiState.update { state ->
                    state.copy(
                        cards = cards
                    )
                }
            }
        }
    }

    // Удержали палец на наборе
    fun onDeleteCard(card: Card){
        viewModelScope.launch {

            if (card.remoteId == null) {
                cardRepository.deleteCard(card)
            } else {
                cardRepository.markAsDeleted(card.id)
            }

            syncManager.scheduleOneTimeSync()
        }
    }

    // Нажали редактировать набор
    fun onStartEditing() {
        val currentState = _uiState.value
        val currentSet = currentState.studySet ?: return

        _uiState.update { state ->
            state.copy(
                isEditing = true,
                draftSet = currentSet.copy(),
                draftCards = currentState.cards.map { it.copy() }
            )
        }
    }

    // Меняем название набора
    fun updateDraftName(name: String) {
        _uiState.update { state ->
            state.copy(
                draftSet = state.draftSet?.copy(name = name)
            )
        }
    }

    // Меняем описание набора
    fun updateDraftDescription(description: String) {
        _uiState.update { state ->
            state.copy(
                draftSet = state.draftSet?.copy(description = description)
            )
        }
    }

    // Меняем иконку набора
    fun updateDraftIcon(iconId: Int) {
        _uiState.update { state ->
            state.copy(
                draftSet = state.draftSet?.copy(iconId = iconId)
            )
        }
    }

    // Меняем данные карточки
    fun updateDraftCard(index: Int, term: String, definition: String) {
        _uiState.update { state ->
            val updatedCards = state.draftCards.toMutableList()
            updatedCards[index] = updatedCards[index].copy(term = term, definition = definition)
            state.copy(draftCards = updatedCards)
        }
    }

    // Нажали отменить редактирование
    fun onCancelEditing() {
        _uiState.update { state ->
            state.copy(
                isEditing = false,
                draftSet = null,
                draftCards = emptyList()
            )
        }
    }

    // Нажали подтвердить редактирование
    fun onSaveChanges() {
        val state = _uiState.value
        val draftSet = state.draftSet ?: return
        val draftCards = state.draftCards

        if (draftSet.name.isBlank()) return
        if (draftCards.any {
            it.term.isBlank() || it.definition.isBlank()
        }) return

        viewModelScope.launch {
            val updatedSet = draftSet.copy(
                isEdited = true
            )
            studySetRepository.updateSet(updatedSet)

            val originalCards = state.cards
            draftCards.forEachIndexed { index, draftCard ->
                val original = originalCards.getOrNull(index)
                // Если term или definition изменились — сбрасываем прогресс
                val contentChanged = original != null &&
                        (original.term != draftCard.term || original.definition != draftCard.definition)

                val cardToSave = if (contentChanged) {
                    draftCard.copy(
                        isEdited = true,
                        level = 0,
                        nextReviewDate = System.currentTimeMillis()
                    )
                } else {
                    draftCard.copy(isEdited = true)
                }

                cardRepository.updateCard(cardToSave)
            }

            onCancelEditing()
            syncManager.scheduleOneTimeSync()
        }
    }
}