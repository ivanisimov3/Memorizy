package com.example.memorizy.ui.screens.setdetails

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.toRoute
import com.example.memorizy.data.repository.CardRepository
import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.repository.StudySetRepository
import com.example.memorizy.ui.navigation.Routes
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class SetDetailsViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle, // аргументы навигации
    private val studySetRepository: StudySetRepository,
    private val cardRepository: CardRepository
) : ViewModel() {

    private val route = savedStateHandle.toRoute<Routes.SetDetails>()
    private val setId = route.setId

    private val _studySet = studySetRepository.getSet(setId)

    private val _cards = cardRepository.getAllCardsForSet(setId)

    val uiState: StateFlow<SetDetailsState> =
        combine(_studySet, _cards) { studySet, cards ->    // два Flow влияют на этот экран,
            SetDetailsState(
                isLoading = false,
                studySet = studySet,
                cards = cards
            )
        }.stateIn(  // Аналог .asStateFlow
            scope = viewModelScope, // Пока живет ViewModel
            started = SharingStarted.WhileSubscribed(5000), // Не отключать StateFlow еще 5 секунд
            initialValue = SetDetailsState()                                // когда не работает .collectAsState
        )

    fun onDeleteCard(card: Card){
        viewModelScope.launch {
            cardRepository.deleteCard(card)
        }
    }
}