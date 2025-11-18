package com.example.memorizy.data.source.local

import androidx.room.Embedded
import com.example.memorizy.data.source.local.studyset.StudySet

data class StudySetWithCardNumber(
    @Embedded
    val studySet: StudySet, // встраиваем столбцы StudySet

    val cardNumber: Int
)