package com.example.memorizy.ui.screens.setdetails

import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.source.local.room.entity.StudySet
import com.example.memorizy.domain.data_exchange.exporter.model.ExportedCsvFile

data class DeadlineUiState(
    val remainingDays: Long,
    val remainingHours: Long
)

data class SetDetailsState(
    val isLoading: Boolean = true,
    val studySet: StudySet? = null,
    val cards: List<Card> = emptyList(),
    val isEditing: Boolean = false,
    val draftSet: StudySet? = null,
    val draftCards: List<Card> = emptyList(),
    val overallProgress: Float = 0f,
    val overallProgressPercentage: String = "0 %",
    val deadline: DeadlineUiState? = null,
    val isExportingCsv: Boolean = false,
    val exportedCsvFile: ExportedCsvFile? = null,
    val exportCsvError: String? = null
)