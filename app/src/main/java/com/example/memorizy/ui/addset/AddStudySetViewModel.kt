package com.example.memorizy.ui.addset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorizy.data.studyset.StudySet
import com.example.memorizy.domain.studyset.StudySetRepository
import com.example.memorizy.ui.studysets.StudySetsScreenUIState
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AddStudySetViewModel  @Inject constructor(
    private val studySetRepository: StudySetRepository
) : ViewModel(){

    private val _uiState = MutableStateFlow(AddSetScreenUIState())

    val uiState = _uiState.asStateFlow()

    fun onNameChanged(newName: String){
        _uiState.update { currentState ->
            currentState.copy(
                name = newName,
                isNameEmptyError = false
            )
        }
    }

    fun onDescriptionChanged(newDescription: String) {
        _uiState.update { currentState ->
            currentState.copy(description = newDescription)
        }
    }

    fun onIconSelected(iconId: Int) {
        _uiState.update { currentState ->
            currentState.copy(selectedIconId = iconId)
        }
    }

    fun onCreateButtonClicked() {
        val currentState = _uiState.value

        if (currentState.name.isBlank()) {
            _uiState.update { it.copy(isNameEmptyError = true) }
            return
        }

        val newSet = StudySet(
            name = currentState.name,
            description = currentState.description,
            iconId = currentState.selectedIconId
        )

        viewModelScope.launch {
            studySetRepository.insertSet(newSet)
            _uiState.update { it.copy(isSetCreated = true) }
        }
    }
}