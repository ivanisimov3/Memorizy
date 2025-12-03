package com.example.memorizy.data.source.local.room

import androidx.room.Embedded
import com.example.memorizy.data.source.local.room.entity.StudySet

// Custom data class to connect entities
data class StudySetWithCardNumber(
    @Embedded
    val studySet: StudySet, // встраиваем столбцы StudySet

    val cardNumber: Long
)