package com.example.memorizy.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

// сущность набор
@Entity(tableName = "study_sets")
data class StudySet(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,

    val name: String,
    val description: String?,   // необязательно поле
    val iconId: Int
)