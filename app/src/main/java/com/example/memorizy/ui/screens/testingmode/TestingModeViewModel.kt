package com.example.memorizy.ui.screens.testingmode

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
class TestingModeViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle, // Аргументы навигации
    private val cardRepository: CardRepository
) : ViewModel(){

    private val route = savedStateHandle.toRoute<Routes.TestingMode>()
    private val setId = route.setId

    private val _uiState = MutableStateFlow(TestingModeState())
    val uiState = _uiState.asStateFlow()

    private var originalCards: List<Card> = emptyList()

    init {
        viewModelScope.launch {
            val cards = cardRepository.getAllCardsForSet(setId).first()

            originalCards = cards

            if (originalCards.isEmpty()) {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isEmpty = true
                    )
                }
            } else {
                _uiState.update { state ->
                    state.copy(
                        isLoading = false,
                        isChoosingMode = true
                    )
                }
            }
        }
    }

    // Выбрали термины - показываем определения
    fun onTermsSelected() {
        startTestingSession(isTermChecked = true)
    }

    /// Выбрали определения - показываем термины
    fun onDefinitionsSelected() {
        startTestingSession(isTermChecked = false)
    }

    // Обновление введённого ответа
    fun onAnswerChanged(answer: String) {
        _uiState.update { state ->
            state.copy(userAnswer = answer)
        }
    }

    // Отправка ответа и переход
    fun onSubmitAnswer() {
        _uiState.update { state ->
            val currentCard = state.currentCard ?: return@update state  // Вернуть текущее состояние без изменений

            val correctAnswer = if (state.isTermChecked)
                currentCard.term
            else
                currentCard.definition

            val isCorrect = state.userAnswer.trim()
                .equals(correctAnswer.trim(), ignoreCase = true)

            val testAnswer = TestAnswer(
                card = currentCard,
                userAnswer = state.userAnswer.trim(),
                isCorrect = isCorrect
            )

            val updatedAnswers = state.userAnswers + testAnswer // userAnswers - List, поэтому + вместо add
            val nextIndex = state.currentIndex + 1
            val isFinished = (nextIndex >= state.cards.size)
            val nextCard = if (isFinished) null else state.cards[nextIndex]

            state.copy(
                correctCount = if (isCorrect) state.correctCount + 1 else state.correctCount,
                incorrectCount = if (!isCorrect) state.incorrectCount + 1 else state.incorrectCount,
                currentIndex = nextIndex,
                currentCard = nextCard,
                isFinished = isFinished,
                userAnswer = "",
                userAnswers = updatedAnswers
            )
        }
    }

    // Перезапуск тестирования
    fun restartTesting() {
        _uiState.value = TestingModeState(
            isLoading = false,
            isChoosingMode = true
        )
    }

    // Показать ответы
    fun showAnswers() {
        _uiState.update { state ->
            state.copy(isShowingAnswers = true)
        }
    }

    // Запуск сессии тестирования
    private fun startTestingSession(isTermChecked: Boolean) {
        val cards = originalCards.shuffled()

        _uiState.value = TestingModeState(
            isLoading = false,
            cards = cards,
            currentIndex = 0,
            currentCard = cards.first(),
            isTermChecked = isTermChecked,
            isChoosingMode = false,
            isEmpty = false
        )
    }
}
