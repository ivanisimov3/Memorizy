package com.example.memorizy.data.studyset

import androidx.room.Embedded

data class StudySetWithCardNumber(
    @Embedded
    val studySet: StudySet, // встраиваем столбцы StudySet

    val cardNumber: Int
)