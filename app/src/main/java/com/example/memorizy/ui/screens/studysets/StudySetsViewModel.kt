package com.example.memorizy.ui.screens.studysets

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorizy.data.source.local.room.entity.StudySet
import com.example.memorizy.data.repository.StudySetRepository
import com.example.memorizy.data.source.local.datastore.SettingsDataStore
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

// На главном экране пользователь может увидеть процесс:
// 1. Изменение значения в строке поиска
// 2. Удаление набора
// 3. Изменение числа наборов при использовании поиска
@HiltViewModel
class StudySetsViewModel @Inject constructor(
    private val studySetRepository: StudySetRepository,
    private val settingsDataStore: SettingsDataStore
) : ViewModel(){

    private val _isLoggedIn = settingsDataStore.token.map { it != null }

    private val _searchQuery = MutableStateFlow("")

    private val _allSets = studySetRepository.getAllSetsWithCardNumber()    // уже Flow

    val uiState: StateFlow<StudySetsState> =
        // три Flow влияют на этот экран, наблюдаем за ними
        combine(_searchQuery, _allSets, _isLoggedIn) { query, sets, loggedIn ->

            val filteredSets = if (query.isBlank()) {   // если пустой то берем все
                sets
            } else{
                sets.filter {
                    it.studySet.name.contains(query, ignoreCase = true) // если совпадает то берем только их
                }
            }

            StudySetsState(
                isLoading = false,
                studySetsWithCardNumber = filteredSets,
                searchQuery = query,
                isLoggedIn = loggedIn
            )
        }.stateIn(  // Аналог .asStateFlow
            scope = viewModelScope, // Пока живет ViewModel
            started = SharingStarted.WhileSubscribed(5000), // Не отключать StateFlow еще 5 секунд
            initialValue = StudySetsState()                                 // когда не работает .collectAsState
        )

    // Два события, которые могут повлиять на этот экран

    fun onSearchQueryChanged(query: String){
        _searchQuery.value = query
    }

    fun onDeleteSet(studySet: StudySet){
        viewModelScope.launch {
            studySetRepository.deleteSet(studySet)
        }
    }
}