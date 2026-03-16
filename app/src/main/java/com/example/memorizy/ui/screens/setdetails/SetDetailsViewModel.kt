package com.example.memorizy.ui.screens.setdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.memorizy.data.repository.CardRepository
import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.repository.StudySetRepository
import com.example.memorizy.data.sync.SyncManager
import com.example.memorizy.domain.spacedrepetition.SpacedRepetitionScheduler
import com.example.memorizy.ui.navigation.Routes
import com.example.memorizy.ui.utils.DateUtils
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

private const val DEADLINE_REFRESH_INTERVAL_MS = 60_000L

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

    // Блок инициализации
    init {
        viewModelScope.launch {
            studySetRepository.getSet(setId).collect { set ->
                _uiState.update { state ->
                    state.copy(
                        studySet = set,
                        isLoading = (set == null),
                        deadline = set?.targetDate?.let(::buildDeadlineUiState)
                    )
                }
            }
        }

        viewModelScope.launch {
            cardRepository.getAllCardsForSet(setId).collect { cards ->
                val avgLevel =
                    if (cards.isEmpty())
                        0f
                    else
                        cards.map { it.level }.average().toFloat()
                val progress = avgLevel / SpacedRepetitionScheduler.MAX_LEVEL   // Рассчитываем процент знания набора

                _uiState.update { state ->
                    state.copy(
                        cards = cards,
                        overallProgress = progress
                    )
                }
            }
        }

        viewModelScope.launch {
            while (isActive) {  // Пока корутина работает
                refreshDeadlineState()
                delay(DEADLINE_REFRESH_INTERVAL_MS)
            }
        }
    }

    // Нажали Удалить карточку
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

    // Нажали Редактировать
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

    // Изменили Название
    fun updateDraftName(name: String) {
        _uiState.update { state ->
            state.copy(
                draftSet = state.draftSet?.copy(name = name)
            )
        }
    }

    // Изменили Описание
    fun updateDraftDescription(description: String) {
        _uiState.update { state ->
            state.copy(
                draftSet = state.draftSet?.copy(description = description)
            )
        }
    }

    // Изменили Иконку
    fun updateDraftIcon(iconId: Int) {
        _uiState.update { state ->
            state.copy(
                draftSet = state.draftSet?.copy(iconId = iconId)
            )
        }
    }

    // Изменили Дедлайн
    fun updateDraftTargetDate(targetDate: Long?) {
        _uiState.update { state ->
            state.copy(
                draftSet = state.draftSet?.copy(targetDate = targetDate)
            )
        }
    }

    // Изменили Данные карточки
    fun updateDraftCard(index: Int, term: String, definition: String) {
        _uiState.update { state ->
            val updatedCards = state.draftCards.toMutableList()
            updatedCards[index] = updatedCards[index].copy(term = term, definition = definition)
            state.copy(draftCards = updatedCards)
        }
    }

    // Нажали Отменить редактирование
    fun onCancelEditing() {
        _uiState.update { state ->
            state.copy(
                isEditing = false,
                draftSet = null,
                draftCards = emptyList()
            )
        }
    }

    // Нажали Подтвердить редактирование
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

            val targetDateChanged = draftSet.targetDate != state.studySet?.targetDate
            if (targetDateChanged) {
                val now = System.currentTimeMillis()
                val freshCards = cardRepository.getAllCardsForSet(setId).first()
                freshCards.filter { it.level > 0 }.forEach { card ->
                    val interval = SpacedRepetitionScheduler.calculateInterval(
                        level = card.level, now = now, targetDate = updatedSet.targetDate
                    )
                    val recalculated = card.copy(
                        nextReviewDate = now + interval,
                        isEdited = true
                    )
                    cardRepository.updateCard(recalculated)
                }
            }

            syncManager.scheduleOneTimeSync()
        }
    }

    private fun refreshDeadlineState() {
        _uiState.update { state ->
            state.copy(
                deadline = state.studySet?.targetDate?.let { buildDeadlineUiState(it) }
            )
        }
    }

    private fun buildDeadlineUiState(targetDate: Long): DeadlineUiState {
        val countdown = DateUtils.buildDeadlineCountdown(
            targetDate = targetDate,
            now = System.currentTimeMillis()
        )

        return DeadlineUiState(
            remainingDays = countdown.remainingDays,
            remainingHours = countdown.remainingHours
        )
    }
}