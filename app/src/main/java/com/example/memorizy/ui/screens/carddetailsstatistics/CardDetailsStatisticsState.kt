package com.example.memorizy.ui.screens.carddetailsstatistics

import com.example.memorizy.domain.cardrisk.CardRiskAnalyzer.CardRisk

enum class CardDetailsSortOption {
    LEVEL,
    DATE
}

data class CardRiskItemUi(
    val id: Long,
    val term: String,
    val level: Int,
    val risk: CardRisk,
    val nextReviewDate: Long
)

data class SortState(
    val option: CardDetailsSortOption,
    val isAscending: Boolean
)

data class CardDetailsStatisticsState(
    val isLoading: Boolean = true,
    val cards: List<CardRiskItemUi> = emptyList(),
    val isEmpty: Boolean = false,
    val sortOption: CardDetailsSortOption = CardDetailsSortOption.LEVEL,
    val isAscending: Boolean = false
)