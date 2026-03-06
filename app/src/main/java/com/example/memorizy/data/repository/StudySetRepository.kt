package com.example.memorizy.data.repository

import com.example.memorizy.data.source.local.room.StudySetWithCardNumber
import com.example.memorizy.data.source.local.room.entity.StudySet
import kotlinx.coroutines.flow.Flow

// Интерфейс репозитория набор

interface StudySetRepository {

    suspend fun insertSet(studySet: StudySet)

    suspend fun updateSet(studySet: StudySet)

    suspend fun markAsDeleted(id: Long)

    suspend fun deleteSet(studySet: StudySet)

    fun getSet(setId: Long): Flow<StudySet>

    fun getAllSetsWithCardNumber(): Flow<List<StudySetWithCardNumber>>

    suspend fun getSetName(setId: Long): String?

    suspend fun syncLocalChanges()

    suspend fun fetchRemoteChanges()
}