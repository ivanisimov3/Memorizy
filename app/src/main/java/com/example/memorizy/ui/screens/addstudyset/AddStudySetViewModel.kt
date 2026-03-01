package com.example.memorizy.ui.screens.addstudyset

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.memorizy.data.source.local.room.entity.StudySet
import com.example.memorizy.data.repository.StudySetRepository
import com.example.memorizy.data.sync.SyncManager
import dagger.hilt.android.lifecycle.HiltViewModel
import jakarta.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

@HiltViewModel
class AddStudySetViewModel  @Inject constructor(
    private val studySetRepository: StudySetRepository,
    private val syncManager: SyncManager
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

    // Нажали на иконку
    fun onIconSelected(iconId: Int) {
        _uiState.update { currentState ->
            currentState.copy(selectedIconId = iconId)
        }
    }

    // Изменение дедлайна
    fun onTargetDateChanged(targetDate: Long?) {
        _uiState.update { currentState ->
            currentState.copy(targetDate = targetDate)
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
            iconId = currentState.selectedIconId,
            targetDate = currentState.targetDate
        )

        viewModelScope.launch { // Обращаемся к БД поэтому корутина
            studySetRepository.insertSet(newSet)

            syncManager.scheduleOneTimeSync()

            _uiState.update { it.copy(isSetCreated = true) }
        }
    }
}