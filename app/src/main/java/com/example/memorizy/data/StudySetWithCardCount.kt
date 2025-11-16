package com.example.memorizy.data

import androidx.room.Embedded

data class StudySetWithCardCount(
    @Embedded
    val studySet: StudySet, // встраиваем столбцы StudySet

    val cardsNumber: Int
)
