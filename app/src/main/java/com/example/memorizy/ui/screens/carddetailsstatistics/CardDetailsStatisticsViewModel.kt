package com.example.memorizy.ui.screens.carddetailsstatistics

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.memorizy.data.repository.CardRepository
import com.example.memorizy.domain.cardrisk.CardRiskAnalyzer
import com.example.memorizy.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class CardDetailsStatisticsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    cardRepository: CardRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Routes.CardDetailsStatistics>()
    private val setId = route.setId

    private val _allCards = cardRepository.getAllCardsForSet(setId)

    private val _sortState = MutableStateFlow(
        SortState(
            option = CardDetailsSortOption.LEVEL,
            isAscending = false
        )
    )

    val uiState: StateFlow<CardDetailsStatisticsState> = combine(
        _allCards,
        _sortState
    ) { cards, sort ->
            val items = cards
                .map { card ->
                    CardRiskItemUi(
                        id = card.id,
                        term = card.term,
                        level = card.level,
                        risk = CardRiskAnalyzer.calculateRisk(card.level),
                        nextReviewDate = card.nextReviewDate
                    )
                }
                .sortedWith(buildComparator(sort))

            CardDetailsStatisticsState(
                isLoading = false,
                cards = items,
                isEmpty = items.isEmpty(),
                sortOption = sort.option,
                isAscending = sort.isAscending
            )
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = CardDetailsStatisticsState()
        )

    private fun buildComparator(sort: SortState): Comparator<CardRiskItemUi> {
        return when (sort.option) {
            CardDetailsSortOption.LEVEL -> {
                if (sort.isAscending) {
                    compareBy<CardRiskItemUi> { it.level }
                        .thenBy { it.nextReviewDate }
                        .thenBy { it.term.lowercase() }
                } else {
                    compareByDescending<CardRiskItemUi> { it.level }
                        .thenBy { it.nextReviewDate }
                        .thenBy { it.term.lowercase() }
                }
            }

            CardDetailsSortOption.DATE -> {
                if (sort.isAscending) {
                    compareBy<CardRiskItemUi> { it.nextReviewDate }
                        .thenByDescending { it.level }
                        .thenBy { it.term.lowercase() }
                } else {
                    compareByDescending<CardRiskItemUi> { it.nextReviewDate }
                        .thenByDescending { it.level }
                        .thenBy { it.term.lowercase() }
                }
            }
        }
    }

    // Нажали Уровень/Дата
    fun onSortOptionClicked(option: CardDetailsSortOption) {
        val current = _sortState.value
        _sortState.value = if (current.option == option) {
            current.copy(isAscending = !current.isAscending)
        } else {
            SortState(option = option, isAscending = false)
        }
    }
}