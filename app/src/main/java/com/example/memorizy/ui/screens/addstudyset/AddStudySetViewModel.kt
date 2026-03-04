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

    // Изменили Название
    fun onNameChanged(newName: String){
        _uiState.update { currentState ->
            currentState.copy(
                name = newName,
                isNameEmptyError = false
            )
        }
    }

    // Изменили Описание
    fun onDescriptionChanged(newDescription: String) {
        _uiState.update { currentState ->
            currentState.copy(description = newDescription)
        }
    }

    // Измененили Иконку
    fun onIconSelected(iconId: Int) {
        _uiState.update { currentState ->
            currentState.copy(selectedIconId = iconId)
        }
    }

    // Измененили Дедлайн
    fun onTargetDateChanged(targetDate: Long?) {
        _uiState.update { currentState ->
            currentState.copy(targetDate = targetDate)
        }
    }

    // Нажали Создать
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

        viewModelScope.launch {
            studySetRepository.insertSet(newSet)

            syncManager.scheduleOneTimeSync()

            _uiState.update { it.copy(isSetCreated = true) }
        }
    }
}