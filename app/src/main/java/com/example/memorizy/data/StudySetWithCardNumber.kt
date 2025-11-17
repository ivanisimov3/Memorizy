package com.example.memorizy.data

import androidx.room.Embedded
import com.example.memorizy.data.studyset.StudySet

data class StudySetWithCardNumber(
    @Embedded
    val studySet: StudySet, // встраиваем столбцы StudySet

    val cardNumber: Int
)