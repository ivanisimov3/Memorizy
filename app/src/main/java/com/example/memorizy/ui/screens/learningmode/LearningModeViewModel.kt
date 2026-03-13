package com.example.memorizy.ui.screens.learningmode

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.memorizy.data.repository.CardRepository
import com.example.memorizy.data.repository.SessionRepository
import com.example.memorizy.data.repository.StudySetRepository
import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.source.local.room.entity.SessionRecord
import com.example.memorizy.data.sync.SyncManager
import com.example.memorizy.domain.spacedrepetition.SpacedRepetitionScheduler
import com.example.memorizy.ui.navigation.Routes
import com.example.memorizy.ui.utils.SESSION_TYPE_LEARNING
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class LearningModeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cardRepository: CardRepository,
    private val studySetRepository: StudySetRepository,
    private val syncManager: SyncManager,
    private val sessionRepository: SessionRepository
) : ViewModel(){

    private val route = savedStateHandle.toRoute<Routes.LearningMode>()
    private val setId = route.setId

    private val _uiState = MutableStateFlow(LearningModeState())
    val uiState = _uiState.asStateFlow()

    private var allCards: List<Card> = emptyList()
    private var targetDate: Long? = null

    // Блок инициализации
    init {
        viewModelScope.launch {
            val studySet = studySetRepository.getSet(setId).first()
            targetDate = studySet.targetDate

            val now = System.currentTimeMillis()
            if (targetDate != null && targetDate!! < now) {
                targetDate = null
                studySetRepository.updateSet(studySet.copy(targetDate = null))
                syncManager.scheduleOneTimeSync()
            }

            allCards = cardRepository.getAllCardsForSet(setId).first()

            startLearningSession(withShuffle = false)
        }
    }

    // Нажали на Карточку
    fun onFlipCard() {
        _uiState.update { state ->
            state.copy(isFlipped = !state.isFlipped)
        }
    }

    // Свайпнули Вправо
    fun onSwipeRight() {
        processSwipe(isCorrect = true)
    }

    // Свайпнули Влево
    fun onSwipeLeft() {
        processSwipe(isCorrect = false)
    }

    private fun processSwipe(isCorrect: Boolean) {
        val currentState = _uiState.value
        val currentCard = currentState.currentCard ?: return

        if (currentState.isReviewMode) {
            val now = System.currentTimeMillis()
            val updatedCard = SpacedRepetitionScheduler.processAnswer(
                card = currentCard,
                isCorrect = isCorrect,
                now = now,
                targetDate = targetDate
            ).copy(isEdited = true) // Пометить как изменённый, чтобы синхронизация отправила на сервер

            viewModelScope.launch {
                cardRepository.updateCard(updatedCard)
                syncManager.scheduleOneTimeSync()
            }
        }

        _uiState.update { state ->
            val nextIndex = state.currentIndex + 1
            val isFinished = (nextIndex >= state.cards.size)
            val nextCard = if (isFinished) null else state.cards[nextIndex]

            val newCorrect = if (isCorrect) state.correctCount + 1 else state.correctCount
            val newIncorrect = if (!isCorrect) state.incorrectCount + 1 else state.incorrectCount

            // Сохраняем результат сессии при завершении
            if (isFinished) {
                val total = state.cards.size
                val percentage = if (total > 0) newCorrect.toFloat() / total * 100f else 0f
                viewModelScope.launch {
                    sessionRepository.saveSession(
                        SessionRecord(
                            setId = setId,
                            type = SESSION_TYPE_LEARNING,
                            correctCount = newCorrect,
                            totalCount = total,
                            percentage = percentage
                        )
                    )
                }
            }

            state.copy(
                correctCount = newCorrect,
                incorrectCount = newIncorrect,
                currentIndex = nextIndex,
                currentCard = nextCard,
                isFinished = isFinished,
                isFlipped = false
            )
        }
    }

    // Нажали Переключатель режима
    fun toggleReviewMode() {
        val newMode = !_uiState.value.isReviewMode
        _uiState.update { it.copy(isReviewMode = newMode) }
        viewModelScope.launch {
            allCards = cardRepository.getAllCardsForSet(setId).first()
            startLearningSession(withShuffle = _uiState.value.isShuffleOn)
        }
    }

    // Нажали Переключатель перемешивания
    fun toggleShuffle() {
        val currentShuffle = _uiState.value.isShuffleOn
        // Перезагружаем карточки, чтобы не смотреть на уже повторенные
        viewModelScope.launch {
            allCards = cardRepository.getAllCardsForSet(setId).first()
            startLearningSession(withShuffle = !currentShuffle)
        }
    }

    // Нажали Учить снова
    fun restartLearning() {
        viewModelScope.launch {
            allCards = cardRepository.getAllCardsForSet(setId).first()
            startLearningSession(withShuffle = _uiState.value.isShuffleOn)
        }
    }

    private fun startLearningSession(withShuffle: Boolean) {
        val now = System.currentTimeMillis()
        val isReviewMode = _uiState.value.isReviewMode

        val reviewCards = SpacedRepetitionScheduler.getCardsForReview(allCards, now)
        val sessionCards = if (isReviewMode) reviewCards else allCards

        if (sessionCards.isEmpty()) {
            val nextReviewIn = if (allCards.isEmpty()) null
                else SpacedRepetitionScheduler.getTimeUntilNextReview(allCards, now)

            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    isEmpty = true,
                    isShuffleOn = withShuffle,
                    isReviewMode = isReviewMode,
                    reviewCardsCount = reviewCards.size,
                    totalCardsCount = allCards.size,
                    nextReviewInMs = nextReviewIn
                )
            }
            return
        }

        val orderedCards = if (withShuffle) {
            if (isReviewMode) {
                SpacedRepetitionScheduler.shuffleWithinLevels(sessionCards)
            } else {
                sessionCards.shuffled()
            }
        } else {
            sessionCards
        }

        _uiState.value = LearningModeState(
            isLoading = false,
            cards = orderedCards,
            currentIndex = 0,
            currentCard = orderedCards.first(),
            isShuffleOn = withShuffle,
            isEmpty = false,
            isReviewMode = isReviewMode,
            reviewCardsCount = reviewCards.size,
            totalCardsCount = allCards.size,
            nextReviewInMs = null
        )
    }
}