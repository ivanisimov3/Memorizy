package com.example.memorizy.ui.screens.studysets

import com.example.memorizy.data.source.local.room.StudySetWithCardNumber

data class StudySetsState(
    val isLoading: Boolean = true,
    val studySetsWithCardNumber: List<StudySetWithCardNumber> = emptyList(),
    val searchQuery: String = "",
    val isLoggedIn: Boolean = false,
    val reviewCountBySet: Map<Long, Long> = emptyMap()
)