package com.example.memorizy.ui.studysets

import com.example.memorizy.data.StudySetWithCardNumber

data class StudySetsScreenUIState(
    val isLoading: Boolean = true,
    val studySets: List<StudySetWithCardNumber> = emptyList(),
    val searchQuery: String = ""
)
