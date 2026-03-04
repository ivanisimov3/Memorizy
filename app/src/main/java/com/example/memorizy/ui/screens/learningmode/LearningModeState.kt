package com.example.memorizy.ui.screens.learningmode

import com.example.memorizy.data.source.local.room.entity.Card

data class LearningModeState(
    val isLoading: Boolean = true,
    val cards: List<Card> = emptyList(),
    val currentCard: Card? = null,
    val currentIndex: Int = 0,
    val correctCount: Int = 0,
    val incorrectCount: Int = 0,
    val isFlipped: Boolean = false,
    val isFinished: Boolean = false,
    val isShuffleOn: Boolean = false,
    val isEmpty: Boolean = false,
    val isReviewMode: Boolean = true,
    val reviewCardsCount: Int = 0,
    val totalCardsCount: Int = 0,
    val nextReviewInMs: Long? = null
)