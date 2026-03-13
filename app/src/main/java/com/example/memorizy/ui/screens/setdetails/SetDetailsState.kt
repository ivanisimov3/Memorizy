package com.example.memorizy.ui.screens.setdetails

import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.source.local.room.entity.StudySet

data class SetDetailsState(
    val isLoading: Boolean = true,
    val studySet: StudySet? = null,
    val cards: List<Card> = emptyList(),
    val isEditing: Boolean = false,
    val draftSet: StudySet? = null,
    val draftCards: List<Card> = emptyList(),
    val overallProgress: Float = 0f
)
