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
        val state = _uiState.value
        val currentCard = state.currentCard ?: return

        if (state.isCheckingAnswer) return

        _uiState.update { current ->
            current.copy(isCheckingAnswer = true)
        }

        viewModelScope.launch {
            val isCorrect = matchesExpectedAnswer(
                currentCard = currentCard,
                userAnswer = state.userAnswer
            )

            _uiState.update { current ->
                if (current.currentCard?.id != currentCard.id) {
                    return@update current.copy(isCheckingAnswer = false)
                }

                val testAnswer = TestAnswer(
                    card = currentCard,
                    userAnswer = state.userAnswer.trim(),
                    isCorrect = isCorrect
                )

                val updatedAnswers = current.userAnswers + testAnswer
                val nextIndex = current.currentIndex + 1
                val isFinished = (nextIndex >= current.cards.size)
                val nextCard = if (isFinished) null else current.cards[nextIndex]

                val newCorrect = if (isCorrect) current.correctCount + 1 else current.correctCount
                val newIncorrect = if (!isCorrect) current.incorrectCount + 1 else current.incorrectCount

                if (isFinished) {
                    val total = current.cards.size
                    val percentage = if (total > 0) newCorrect.toFloat() / total * 100f else 0f
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

                current.copy(
                    correctCount = newCorrect,
                    incorrectCount = newIncorrect,
                    currentIndex = nextIndex,
                    currentCard = nextCard,
                    isFinished = isFinished,
                    userAnswer = "",
                    userAnswers = updatedAnswers,
                    isCheckingAnswer = false
                )
            }
        }
    }

    private suspend fun matchesExpectedAnswer(
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