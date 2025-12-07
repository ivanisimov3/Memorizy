package com.example.memorizy.ui.screens.learningmode

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.memorizy.data.repository.CardRepository
import com.example.memorizy.data.source.local.room.entity.Card
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
    savedStateHandle: SavedStateHandle, // Аргументы навигации
    private val cardRepository: CardRepository
) : ViewModel(){

    private val route = savedStateHandle.toRoute<Routes.LearningMode>()
    private val setId = route.setId

    private val _uiState = MutableStateFlow(LearningModeState())
    val uiState = _uiState.asStateFlow()

    private var originalCards: List<Card> = emptyList()

    init {
        viewModelScope.launch {
            val cards = cardRepository.getAllCardsForSet(setId).first()

            originalCards = cards

            startLearningSession(withShuffle = false)
        }
    }

    // Нажали на карточку
    fun onFlipCard() {
        _uiState.update { state ->
            state.copy(
                isFlipped = !state.isFlipped
            )
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

    // Обработчик переключения режима перемешивания
    fun toggleShuffle() {
        val currentShuffle = _uiState.value.isShuffleOn
        startLearningSession(withShuffle = !currentShuffle)
    }

    // Обработчик нажатия перезапуска режима заучивания
    fun restartLearning() {
        startLearningSession(withShuffle = _uiState.value.isShuffleOn)
    }

    // Обработчик запуска режима
    private fun startLearningSession(withShuffle: Boolean) {
        if (originalCards.isEmpty()) {
            _uiState.update { state ->
                state.copy(
                    isLoading = false,
                    isEmpty = true
                )
            }
            return
        }

        val cards = if (withShuffle) originalCards.shuffled() else originalCards

        _uiState.value = LearningModeState(
            isLoading = false,
            cards = cards,
            currentIndex = 0,
            currentCard = cards.first(),
            isShuffleOn = withShuffle,
            isEmpty = false
        )
    }
}