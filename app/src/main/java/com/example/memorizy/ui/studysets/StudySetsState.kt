package com.example.memorizy.ui.studysets

import com.example.memorizy.data.source.local.StudySetWithCardNumber

data class StudySetsState(
    val isLoading: Boolean = true,
    val studySetsWithCardNumber: List<StudySetWithCardNumber> = emptyList(),
    val searchQuery: String = ""
)