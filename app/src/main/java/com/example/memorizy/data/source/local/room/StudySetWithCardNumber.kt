package com.example.memorizy.data.source.local.room

import androidx.room.Embedded
import com.example.memorizy.data.source.local.room.entity.StudySet

// Класс для объедененных SQL операций

data class StudySetWithCardNumber(
    @Embedded
    val studySet: StudySet, // Встраиваем все столбцы из StudySet

    val cardNumber: Long    // Добавляем поле количество карточек в наборе
)