package com.example.memorizy.data.source.network.dto

import kotlinx.serialization.Serializable

// Параметры набора для передачи данных

@Serializable
data class StudySetDto(
    val id: Long? = null,
    val name: String,
    val description: String?,
    val iconId: Int,
    val createdAt: Long? = null,
    val targetDate: Long? = null
)