package com.example.memorizy.data.repository

import com.example.memorizy.data.source.local.room.StudySetWithCardNumber
import com.example.memorizy.data.source.local.room.entity.StudySet
import kotlinx.coroutines.flow.Flow

// Interface to the data layer (study_set).
interface StudySetRepository {

    suspend fun insertSet(studySet: StudySet)

    suspend fun deleteSet(studySet: StudySet)

    fun getSet(setId: Long): Flow<StudySet>

    fun getAllSetsWithCardNumber(): Flow<List<StudySetWithCardNumber>>
}