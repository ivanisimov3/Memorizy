package com.example.memorizy.ui.screens.addstudyset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorizy.data.source.local.studyset.StudySet
import com.example.memorizy.data.repository.studysetrepository.StudySetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AddStudySetViewModel  @Inject constructor(
    private val studySetRepository: StudySetRepository
) : ViewModel(){

    private val _uiState = MutableStateFlow(AddStudySetState())

    val uiState: StateFlow<AddStudySetState> = _uiState.asStateFlow()

    // Изменение в строке имени
    fun onNameChanged(newName: String){
        _uiState.update { currentState ->
            currentState.copy(
                name = newName,
                isNameEmptyError = false
            )
        }
    }

    // Изменение в строке описания
    fun onDescriptionChanged(newDescription: String) {
        _uiState.update { currentState ->
            currentState.copy(description = newDescription)
        }
    }

    // нажали на иконку
    fun onIconSelected(iconId: Int) {
        _uiState.update { currentState ->
            currentState.copy(selectedIconId = iconId)
        }
    }

    // Нажали кнопку создать
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

        viewModelScope.launch { // обращаемся к бд поэтому корутина
            studySetRepository.insertSet(newSet)
            _uiState.update { it.copy(isSetCreated = true) }
        }
    }
}