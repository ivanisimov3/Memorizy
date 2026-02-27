package com.example.memorizy.ui.screens.learningmode

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.memorizy.data.repository.CardRepository
import com.example.memorizy.data.repository.StudySetRepository
import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.sync.SyncManager
import com.example.memorizy.domain.spacedrepetition.SpacedRepetitionScheduler
import com.example.memorizy.ui.navigation.Routes
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
    private val syncManager: SyncManager
) : ViewModel(){

    private val route = savedStateHandle.toRoute<Routes.LearningMode>()
    private val setId = route.setId

    private val _uiState = MutableStateFlow(LearningModeState())
    val uiState = _uiState.asStateFlow()

    private var allCards: List<Card> = emptyList()
    private var targetDate: Long? = null

    init {
        viewModelScope.launch {
            // Загружаем набор для получения targetDate
            val studySet = studySetRepository.getSet(setId).first()
            targetDate = studySet.targetDate

            // Проверяем: если дедлайн прошёл — сбрасываем в стандартный режим
            val now = System.currentTimeMillis()
            if (targetDate != null && targetDate!! < now) {
                targetDate = null
                studySetRepository.updateSet(studySet.copy(targetDate = null))
                syncManager.scheduleOneTimeSync()
            }

            // Загружаем карточки
            allCards = cardRepository.getAllCardsForSet(setId).first()

            startLearningSession(withShuffle = false)
        }
    }

    // Нажали на карточку
    fun onFlipCard() {
        _uiState.update { state ->
            state.copy(isFlipped = !state.isFlipped)
        }
    }

    // Свайпнули вправо карточку
    fun onSwipeRight() {
        processSwipe(isCorrect = true)
    }

    // Свайпнули влево карточку
    fun onSwipeLeft() {
        processSwipe(isCorrect = false)
    }

    // Обработчик свайпов
    private fun processSwipe(isCorrect: Boolean) {
        val currentState = _uiState.value
        val currentCard = currentState.currentCard ?: return

        // В режиме повторения — обновляем прогресс в БД
        if (currentState.isReviewMode) {
            val now = System.currentTimeMillis()
            val updatedCard = SpacedRepetitionScheduler.processAnswer(
                card = currentCard,
                isCorrect = isCorrect,
                now = now,
                targetDate = targetDate
            )

            viewModelScope.launch {
                cardRepository.updateCard(updatedCard)
                syncManager.scheduleOneTimeSync()
            }
        }

        _uiState.update { state ->
            val nextIndex = state.currentIndex + 1
            val isFinished = (nextIndex >= state.cards.size)
            val nextCard = if (isFinished) null else state.cards[nextIndex]

            state.copy(
                correctCount = if (isCorrect) state.correctCount + 1 else state.correctCount,
                incorrectCount = if (!isCorrect) state.incorrectCount + 1 else state.incorrectCount,
                currentIndex = nextIndex,
                currentCard = nextCard,
                isFinished = isFinished,
                isFlipped = false
            )
        }
    }

    // Переключение режима: повторение <-> все карточки
    fun toggleReviewMode() {
        val newMode = !_uiState.value.isReviewMode
        _uiState.update { it.copy(isReviewMode = newMode) }
        startLearningSession(withShuffle = _uiState.value.isShuffleOn)
    }

    // Обработчик переключения режима перемешивания
    fun toggleShuffle() {
        val currentShuffle = _uiState.value.isShuffleOn
        startLearningSession(withShuffle = !currentShuffle)
    }

    // Обработчик нажатия перезапуска режима заучивания
    fun restartLearning() {
        // Перезагружаем карточки из БД для актуального состояния
        viewModelScope.launch {
            allCards = cardRepository.getAllCardsForSet(setId).first()
            startLearningSession(withShuffle = _uiState.value.isShuffleOn)
        }
    }

    // Обработчик запуска режима
    private fun startLearningSession(withShuffle: Boolean) {
        val now = System.currentTimeMillis()
        val isReviewMode = _uiState.value.isReviewMode

        // Определяем набор карточек
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

        // Перемешивание: в режиме повторения — внутри уровней, иначе — полностью
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