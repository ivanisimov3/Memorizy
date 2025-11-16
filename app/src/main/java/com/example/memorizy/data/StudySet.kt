package com.example.memorizy.data

import androidx.room.Entity
import androidx.room.PrimaryKey

// сущность набор
@Entity(tableName = "study_sets")
data class StudySet(
    @PrimaryKey val id: Int,

    val name: String,
    val description: String?,   // необязательно поле
    val iconId: Int
)
