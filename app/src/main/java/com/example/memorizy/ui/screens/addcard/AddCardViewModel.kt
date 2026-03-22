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
import android.net.Uri
import com.example.memorizy.domain.importer.usecase.ParseCardsFileUseCase

@HiltViewModel
class AddCardViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cardRepository: CardRepository,
    private val syncManager: SyncManager,
    private val parseCardsFileUseCase: ParseCardsFileUseCase
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

    // Нажали Добавить определение
    fun onDefinitionVariantAdd() {
        _uiState.update { currentState ->
            currentState.copy(
                definitionVariants = currentState.definitionVariants + ""
            )
        }
    }

    // Изменили Дополнительное определение
    fun onDefinitionVariantChanged(index: Int, newValue: String) {
        _uiState.update { currentState ->
            val updatedVariants = currentState.definitionVariants.toMutableList()
            if (index in updatedVariants.indices) {
                updatedVariants[index] = newValue
            }
            currentState.copy(definitionVariants = updatedVariants)
        }
    }

    // Удалили Дополнительное определение
    fun onDefinitionVariantRemove(index: Int) {
        _uiState.update { currentState ->
            val updatedVariants = currentState.definitionVariants.toMutableList()
            if (index in updatedVariants.indices) {
                updatedVariants.removeAt(index)
            }
            currentState.copy(definitionVariants = updatedVariants)
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

        val normalizedDefinitionVariants = normalizeDefinitionVariants(
            primaryDefinition = currentState.definition,
            rawVariants = currentState.definitionVariants
        )

        val newCard = Card(
            setId = setId,
            term = currentState.term,
            definition = currentState.definition,
            definitionVariants = normalizedDefinitionVariants
        )

        viewModelScope.launch {
            cardRepository.insertCard(newCard)

            syncManager.scheduleOneTimeSync()

            _uiState.update { it.copy(isCardCreated = true) }
        }
    }

    // Выбрали файл
    fun onFileSelected(uri: Uri?) {
        if (uri == null) return

        _uiState.update { it.copy(isImporting = true) }
        viewModelScope.launch {
            val result = parseCardsFileUseCase(uri)
            _uiState.update {
                it.copy(
                    isImporting = false,
                    importSummary = result,
                    showImportSummaryDialog = true
                )
            }
        }
    }

    // Нажали отменить (импорт)
    fun dismissImportSummary() {
        _uiState.update {
            it.copy(
                showImportSummaryDialog = false,
                importSummary = null
            )
        }
    }

    // Нажали Сохранить (импорт)
    fun confirmImport() {
        val result = _uiState.value.importSummary ?: return
        
        _uiState.update {
            it.copy(
                isSaving = true,
                showImportSummaryDialog = false
            )
        }
        
        viewModelScope.launch {
            val cardsToInsert = result.successfulCards.map {
                Card(
                    setId = setId,
                    term = it.term,
                    definition = it.definition,
                    definitionVariants = normalizeDefinitionVariants(
                        primaryDefinition = it.definition,
                        rawVariants = it.definitionVariants
                    )
                )
            }

            if (cardsToInsert.isNotEmpty()) {
                cardRepository.insertCards(cardsToInsert)
                syncManager.scheduleOneTimeSync()
            }
            
            _uiState.update {
                it.copy(
                    isSaving = false,
                    isCardCreated = true,
                    importSummary = null
                )
            }
        }
    }

    private fun normalizeDefinitionVariants(
        primaryDefinition: String,
        rawVariants: List<String>
    ): List<String> {
        val primaryNormalized = primaryDefinition.trim()

        return rawVariants
            .map { it.trim() }
            .filter { it.isNotEmpty() }
            .filter { it != primaryNormalized }
            .distinct() // Убираем дубликаты
    }
}