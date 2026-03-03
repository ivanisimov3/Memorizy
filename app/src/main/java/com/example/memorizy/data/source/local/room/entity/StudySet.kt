package com.example.memorizy.data.source.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// Модель набора для Room

@Entity(tableName = "study_sets")
data class StudySet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val description: String?,
    val iconId: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val remoteId: Long? = null,
    val isDeleted: Boolean = false,
    val isEdited: Boolean = false,
    val targetDate: Long? = null
)