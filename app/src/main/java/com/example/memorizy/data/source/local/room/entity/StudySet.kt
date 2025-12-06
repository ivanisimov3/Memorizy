package com.example.memorizy.data.source.local.room.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
Internal model used to represent a task stored locally in a Room database. This is used inside
the data layer only.
*/
@Entity(tableName = "study_sets")
data class StudySet(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val name: String,
    val description: String?,   // Необязательно поле
    val iconId: Int,
    val createdAt: Long = System.currentTimeMillis(),
    val remoteId: Long? = null, // Если remoteId == null, тогда набор не синхронизирован
    val isDeleted: Boolean = false,
    val isEdited: Boolean = false
)