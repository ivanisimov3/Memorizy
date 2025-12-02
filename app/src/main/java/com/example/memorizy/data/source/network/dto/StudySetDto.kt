package com.example.memorizy.data.source.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class StudySetDto(
    val id: Long? = null,   // ID может не быть при создании
    val name: String,
    val description: String?,
    val iconId: Int
)