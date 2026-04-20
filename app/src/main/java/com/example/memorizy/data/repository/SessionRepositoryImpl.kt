package com.example.memorizy.data.repository

import com.example.memorizy.data.source.local.room.dao.SessionRecordDao
import com.example.memorizy.data.source.local.room.entity.SessionRecord
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow

// Реализация репозитория учебные сессии

class SessionRepositoryImpl @Inject constructor(
    private val sessionRecordDao: SessionRecordDao
) : SessionRepository {

    override suspend fun saveSession(record: SessionRecord) {
        sessionRecordDao.insertRecord(record)
    }

    override fun getSessionsForSet(setId: Long): Flow<List<SessionRecord>> {
        return sessionRecordDao.getRecordsForSet(setId)
    }
}