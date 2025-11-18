package com.example.memorizy.ui.setdetails

import com.example.memorizy.data.source.local.card.Card
import com.example.memorizy.data.source.local.studyset.StudySet

data class SetDetailsState(
    val isLoading: Boolean = true,
    val studySet: StudySet? = null,
    val cards: List<Card> = emptyList(),
)
