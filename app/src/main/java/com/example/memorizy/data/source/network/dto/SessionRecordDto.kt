package com.example.memorizy.data.source.network.dto

import kotlinx.serialization.Serializable

// Параметры записи учебной сессии для передачи данных

@Serializable
data class SessionRecordDto(
    val id: Long? = null,
    val studySetId: Long,
    val type: String,
    val correctCount: Int,
    val totalCount: Int,
    val percentage: Float,
    val timestamp: Long? = null
)