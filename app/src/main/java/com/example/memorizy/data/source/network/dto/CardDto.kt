package com.example.memorizy.data.source.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class CardDto (
    val id: Long? = null,   // ID может не быть при создании
    val term: String,
    val definition: String,
    val studySetId: Long
)