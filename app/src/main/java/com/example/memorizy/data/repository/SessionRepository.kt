package com.example.memorizy.data.repository

import com.example.memorizy.data.source.local.room.entity.SessionRecord
import kotlinx.coroutines.flow.Flow

// Интерфейс репозитория учебные сессии

interface SessionRepository {

    suspend fun saveSession(record: SessionRecord)

    fun getSessionsForSet(setId: Long): Flow<List<SessionRecord>>
}
