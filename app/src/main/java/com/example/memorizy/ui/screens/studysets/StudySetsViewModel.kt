package com.example.memorizy.ui.screens.studysets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorizy.data.source.local.room.entity.StudySet
import com.example.memorizy.data.repository.CardRepository
import com.example.memorizy.data.repository.SettingsRepository
import com.example.memorizy.data.repository.StudySetRepository
import com.example.memorizy.data.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class StudySetsViewModel @Inject constructor(
    private val studySetRepository: StudySetRepository,
    private val cardRepository: CardRepository,
    private val settingsRepository: SettingsRepository,
    private val syncManager: SyncManager
) : ViewModel(){

    private val _isLoggedIn = settingsRepository.token.map { it != null }

    private val _searchQuery = MutableStateFlow("")

    private val _allSets = studySetRepository.getAllSetsWithCardNumber()

    private val _allCards = cardRepository.getAllNonDeletedCards()

    val uiState: StateFlow<StudySetsState> =
        // Четыре Flow влияют на этот экран, наблюдаем за ними
        combine(_searchQuery, _allSets, _isLoggedIn, _allCards)
            { query, sets, loggedIn, cards ->

                val now = System.currentTimeMillis()

                val reviewCountBySet = cards
                    .filter { it.nextReviewDate <= now }
                    .groupBy { it.setId }
                    .mapValues { it.value.size.toLong() }   // Трансформация словаря из
                                                            // { 1 -> [Card_A, Card_B], 2 -> [Card_C] }
                                                            // в { 1 -> 2L, 2 -> 1L }

                val filteredSets = if (query.isBlank()) {
                    sets
                } else{
                    sets.filter {
                        it.studySet.name.contains(query, ignoreCase = true)
                    }
                }

                StudySetsState(
                    isLoading = false,
                    studySetsWithCardNumber = filteredSets,
                    searchQuery = query,
                    isLoggedIn = loggedIn,
                    reviewCountBySet = reviewCountBySet
                )
            }.stateIn(  // В горячий поток
                scope = viewModelScope, // Пока живет ViewModel
                started = SharingStarted.WhileSubscribed(5000), // Не отключать StateFlow еще 5 секунд
                initialValue = StudySetsState()                                 // когда не работает .collectAsState
            )

    // Изменили Текст поиска
    fun onSearchQueryChanged(query: String){
        _searchQuery.value = query
    }

    // Нажали Удалить набор
    fun onDeleteSet(studySet: StudySet){
        viewModelScope.launch {
            if (studySet.remoteId == null) {
                studySetRepository.deleteSet(studySet)
            } else {
                studySetRepository.markAsDeleted(studySet.id)
            }

            syncManager.scheduleOneTimeSync()
        }
    }
}