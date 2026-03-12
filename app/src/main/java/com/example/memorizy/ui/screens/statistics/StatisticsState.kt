package com.example.memorizy.ui.screens.statistics

import com.example.memorizy.data.source.local.room.entity.SessionRecord

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
    val sessionChartData: SessionChartData = SessionChartData()
)
