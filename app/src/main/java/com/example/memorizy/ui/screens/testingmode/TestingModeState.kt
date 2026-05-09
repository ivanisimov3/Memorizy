package com.example.memorizy.ui.screens.testingmode

import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.domain.text_comparison.TextComparisonCategory

// UI может иметь свои собственные модели, разработанные специально для представления состояния интерфейса.
data class TestAnswer(  // Модель, зависящая от UI
    val card: Card,
    val userAnswer: String,
    val expectedAnswer: String,
    val category: TextComparisonCategory,
    val isCorrect: Boolean
)

data class TestingModeState(
    val isLoading: Boolean = true,
    val cards: List<Card> = emptyList(),
    val currentCard: Card? = null,
    val currentIndex: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val isFinished: Boolean = false,
    val isEmpty: Boolean = false,
    val userAnswer: String = "",
    val userAnswers: List<TestAnswer> = emptyList(),
    val isShowingAnswers: Boolean = false,
    val isCheckingAnswer: Boolean = false
)