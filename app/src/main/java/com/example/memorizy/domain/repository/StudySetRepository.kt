package com.example.memorizy.domain.repository

import com.example.memorizy.data.entity.StudySet
import com.example.memorizy.data.entity.StudySetWithCardNumber
import kotlinx.coroutines.flow.Flow

// Общий шаблон работы со StudySet
interface StudySetRepository {

    suspend fun insertSet(studySet: StudySet)

    suspend fun deleteSet(studySet: StudySet)

    fun getSet(setId: Int): Flow<StudySet>

    fun getAllSetsWithCardNumber(): Flow<List<StudySetWithCardNumber>>
}