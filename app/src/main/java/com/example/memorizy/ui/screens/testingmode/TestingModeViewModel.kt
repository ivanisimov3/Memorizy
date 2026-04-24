package com.example.memorizy.ui.screens.testingmode

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.memorizy.data.repository.CardRepository
import com.example.memorizy.data.repository.SessionRepository
import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.source.local.room.entity.SessionRecord
import com.example.memorizy.domain.textcomparison.TextComparator
import com.example.memorizy.ui.navigation.Routes
import com.example.memorizy.ui.utils.SESSION_TYPE_TESTING
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
    private val cardRepository: CardRepository,
    private val sessionRepository: SessionRepository,
    private val textComparator: TextComparator
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Routes.TestingMode>()
    private val setId = route.setId

    private val _uiState = MutableStateFlow(TestingModeState())
    val uiState = _uiState.asStateFlow()

    private var originalCards: List<Card> = emptyList()

    // Блок инициализации
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
                        isLoading = false
                    )
                }
                startTestingSession()
            }
        }
    }

    // Изменили Ответ
    fun onAnswerChanged(answer: String) {
        _uiState.update { state ->
            state.copy(userAnswer = answer)
        }
    }

    // Нажали к Следующему
    fun onSubmitAnswer() {
        _uiState.update { state ->
            val currentCard = state.currentCard ?: return@update state  // Вернуть текущее состояние без изменений

            val isCorrect = matchesExpectedAnswer(
                currentCard = currentCard,
                userAnswer = state.userAnswer
            )

            val testAnswer = TestAnswer(
                card = currentCard,
                userAnswer = state.userAnswer.trim(),
                isCorrect = isCorrect
            )

            val updatedAnswers = state.userAnswers + testAnswer
            val nextIndex = state.currentIndex + 1
            val isFinished = (nextIndex >= state.cards.size)
            val nextCard = if (isFinished) null else state.cards[nextIndex]

            val newCorrect = if (isCorrect) state.correctCount + 1 else state.correctCount
            val newIncorrect = if (!isCorrect) state.incorrectCount + 1 else state.incorrectCount

            if (isFinished) {
                val total = state.cards.size
                val percentage = if (total > 0) newCorrect.toFloat() / total * 100f else 0f
                viewModelScope.launch {
                    sessionRepository.saveSession(
                        SessionRecord(
                            setId = setId,
                            type = SESSION_TYPE_TESTING,
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
                userAnswer = "",
                userAnswers = updatedAnswers
            )
        }
    }

    private fun matchesExpectedAnswer(
        currentCard: Card,
        userAnswer: String
    ): Boolean {
        val acceptedDefinitions = buildList {
            add(currentCard.definition)
            addAll(currentCard.definitionVariants)
        }

        return acceptedDefinitions.any { expectedDefinition ->
            textComparator.compare(
                expected = expectedDefinition,
                actual = userAnswer
            )
        }
    }

    // Нажали Пройти тестирование снова
    fun restartTesting() {
        startTestingSession()
    }

    // Нажали Посмотреть ответы
    fun showAnswers() {
        _uiState.update { state ->
            state.copy(isShowingAnswers = true)
        }
    }

    private fun startTestingSession() {
        val cards = originalCards.shuffled()

        _uiState.value = TestingModeState(
            isLoading = false,
            cards = cards,
            currentIndex = 0,
            currentCard = cards.first(),
            isEmpty = false
        )
    }
}