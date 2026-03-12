package com.example.memorizy.ui.screens.statistics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.memorizy.data.repository.CardRepository
import com.example.memorizy.data.repository.SessionRepository
import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.source.local.room.entity.SessionRecord
import com.example.memorizy.ui.navigation.Routes
import com.example.memorizy.ui.utils.DateUtils.dayKey
import com.example.memorizy.ui.utils.SESSION_TYPE_LEARNING
import com.example.memorizy.ui.utils.SESSION_TYPE_TESTING
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import java.util.SortedMap

@HiltViewModel
class StatisticsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val cardRepository: CardRepository,
    private val sessionRepository: SessionRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Routes.Statistics>()
    private val setId = route.setId

    val uiState: StateFlow<StatisticsState> = combine(
        cardRepository.getAllCardsForSet(setId),
        sessionRepository.getSessionsForSet(setId)
    ) { cards, sessions ->
        StatisticsState(
            isLoading = false,
            levelDistribution = calculateLevelDistribution(cards),
            isLevelDistributionEmpty = cards.isEmpty(),
            sessionChartData = calculateSessionChartData(sessions)
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = StatisticsState()
    )

    private fun calculateLevelDistribution(cards: List<Card>): List<Int> {
        if (cards.isEmpty()) return emptyList()

        // Соотносим 0..7 и количество карточек каждого из этих уровней, получаем List
        return (0..7).map { level ->
            cards.count { it.level == level }
        }
    }

    private fun calculateSessionChartData(sessions: List<SessionRecord>): SessionChartData {
        if (sessions.isEmpty()) return SessionChartData(isEmpty = true)

        val thirtyDaysAgo = System.currentTimeMillis() - 30L * 24 * 60 * 60 * 1000
        val recentRecords = sessions.filter { it.timestamp >= thirtyDaysAgo }

        val learningByDay = groupAndCalculatePercentages(recentRecords, SESSION_TYPE_LEARNING)
        val testingByDay = groupAndCalculatePercentages(recentRecords, SESSION_TYPE_TESTING)

        val hasLearning = learningByDay.size >= 2
        val hasTesting = testingByDay.size >= 2

        if (!hasLearning && !hasTesting) {
            return SessionChartData(isEmpty = true)
        }

        val allDays = (learningByDay.keys + testingByDay.keys).toSortedSet().toList()

        return SessionChartData(
            isEmpty = false,
            hasLearning = hasLearning,
            hasTesting = hasTesting,
            learningX = if (hasLearning) createXAxis(allDays, learningByDay) else emptyList(),
            learningY = if (hasLearning) createYAxis(allDays, learningByDay) else emptyList(),
            testingX = if (hasTesting) createXAxis(allDays, testingByDay) else emptyList(),
            testingY = if (hasTesting) createYAxis(allDays, testingByDay) else emptyList(),
            dateLabels = allDays
        )
    }

    private fun groupAndCalculatePercentages(
        records: List<SessionRecord>,
        type: String
    ): SortedMap<Long, Float> {
        return records
            .filter { it.type == type }
            .groupBy { dayKey(it.timestamp) }
            .mapValues { (_, dayRecords) ->
                val totalQuestions = dayRecords.sumOf { it.totalCount }
                if (totalQuestions > 0) {
                    (dayRecords.sumOf { it.correctCount }.toFloat() / totalQuestions) * 100f
                } else {
                    0f
                }
            }.toSortedMap()
    }

    private fun createXAxis(allDays: List<Long>, dataMap: Map<Long, Float>): List<Float> {
        return allDays.mapIndexedNotNull { index, day ->
            if (dataMap.containsKey(day)) index.toFloat() else null
        }
    }

    private fun createYAxis(allDays: List<Long>, dataMap: Map<Long, Float>): List<Float> {
        return allDays.mapIndexedNotNull { _, day ->
            dataMap[day]
        }
    }
}