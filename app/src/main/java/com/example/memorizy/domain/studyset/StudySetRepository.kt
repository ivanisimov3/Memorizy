package com.example.memorizy.domain.studyset

import com.example.memorizy.data.StudySetWithCardNumber
import com.example.memorizy.data.studyset.StudySet
import kotlinx.coroutines.flow.Flow

// Общий шаблон работы со StudySet
interface StudySetRepository {

    suspend fun insertSet(studySet: StudySet)

    suspend fun deleteSet(studySet: StudySet)

    fun getSet(setId: Int): Flow<StudySet>

    fun getAllSetsWithCardNumber(): Flow<List<StudySetWithCardNumber>>
}