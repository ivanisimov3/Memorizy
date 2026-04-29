package com.example.memorizy.ui.screens.statistics

import com.example.memorizy.data.source.local.room.entity.SessionRecord
import com.example.memorizy.domain.cardrisk.CardRiskAnalyzer.CardRisk

enum class StatisticsCardsSortOption {
    LEVEL,
    DATE
}

data class StatisticsCardItemUi(
    val id: Long,
    val term: String,
    val level: Int,
    val risk: CardRisk,
    val nextReviewDate: Long
)

data class StatisticsCardsSortState(
    val option: StatisticsCardsSortOption,
    val isAscending: Boolean
)

data class StatisticsCardsState(
    val isLoading: Boolean = true,
    val cards: List<StatisticsCardItemUi> = emptyList(),
    val isEmpty: Boolean = false,
    val sortOption: StatisticsCardsSortOption = StatisticsCardsSortOption.LEVEL,
    val isAscending: Boolean = false
)

data class SessionChartData(
    val isEmpty: Boolean = true,
    val hasLearning: Boolean = false,
    val hasTesting: Boolean = false,
    val learningX: List<Float> = emptyList(),
    val learningY: List<Float> = emptyList(),
    val testingX: List<Float> = emptyList(),
    val testingY: List<Float> = emptyList(),
    val dateLabels: List<Long> = emptyList()
)

data class StatisticsState(
    val isLoading: Boolean = true,
    val levelDistribution: List<Int> = emptyList(),
    val isLevelDistributionEmpty: Boolean = true,
    val sessionRecords: List<SessionRecord> = emptyList(),
    val sessionChartData: SessionChartData = SessionChartData(),
    val cardsState: StatisticsCardsState = StatisticsCardsState()
)