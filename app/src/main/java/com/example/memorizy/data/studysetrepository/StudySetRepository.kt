package com.example.memorizy.data.studysetrepository

import com.example.memorizy.data.source.local.StudySetWithCardNumber
import com.example.memorizy.data.source.local.studyset.StudySet
import kotlinx.coroutines.flow.Flow

// Общий шаблон работы со StudySet
interface StudySetRepository {

    suspend fun insertSet(studySet: StudySet)

    suspend fun deleteSet(studySet: StudySet)

    fun getSet(setId: Int): Flow<StudySet>

    fun getAllSetsWithCardNumber(): Flow<List<StudySetWithCardNumber>>
}