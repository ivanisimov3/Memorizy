package com.example.memorizy.ui.model

import com.example.memorizy.data.entity.StudySetWithCardNumber

data class UserSetScreenUIState(
    val isLoading: Boolean = true,
    val studySets: List<StudySetWithCardNumber> = emptyList(),
    val searchQuery: String = ""
)
