package com.example.memorizy.data.source.network.dto

import kotlinx.serialization.Serializable

// Параметры карточки для передачи данных

@Serializable
data class CardDto (
    val id: Long? = null,
    val term: String,
    val definition: String,
    val definitionVariants: List<String> = emptyList(),
    val studySetId: Long,
    val createdAt: Long? = null,
    val level: Int = 0,
    val nextReviewDate: Long? = null
)