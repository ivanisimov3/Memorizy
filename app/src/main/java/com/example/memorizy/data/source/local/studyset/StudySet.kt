package com.example.memorizy.data.source.local.studyset

import androidx.room.Entity
import androidx.room.PrimaryKey

/*
Internal model used to represent a task stored locally in a Room database. This is used inside
the data layer only.
*/
@Entity(tableName = "study_sets")
data class StudySet(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,
    val description: String?,   // необязательно поле
    val iconId: Int
)