package com.example.memorizy.data.repository

import android.util.Log
import com.example.memorizy.data.mapper.toDto
import com.example.memorizy.data.mapper.toEntity
import com.example.memorizy.data.source.local.room.dao.SessionRecordDao
import com.example.memorizy.data.source.local.room.dao.StudySetDao
import com.example.memorizy.data.source.local.room.entity.SessionRecord
import com.example.memorizy.data.source.network.MemorizyApiService
import com.example.memorizy.data.sync.SyncAuthException
import com.example.memorizy.data.sync.SyncRetryException
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException

// Реализация репозитория учебные сессии

class SessionRepositoryImpl @Inject constructor(
    private val sessionRecordDao: SessionRecordDao,
    private val studySetDao: StudySetDao,
    private val api: MemorizyApiService,
    private val settingsRepository: SettingsRepository
) : SessionRepository {

    companion object {
        private const val TAG = "SessionRepository"
    }

    override suspend fun saveSession(record: SessionRecord) {
        sessionRecordDao.insertRecord(record)
    }

    override fun getSessionsForSet(setId: Long): Flow<List<SessionRecord>> {
        return sessionRecordDao.getRecordsForSet(setId)
    }

    override suspend fun syncLocalChanges() {
        val tokenString = settingsRepository.token.first() ?: return
        val authHeader = "Bearer $tokenString"
        var shouldRetry = false

        val unsyncedRecords = sessionRecordDao.getUnsyncedRecords()
        unsyncedRecords.forEach { localRecord ->
            val parentSet = studySetDao.getSetByIdSimple(localRecord.setId)
            val parentRemoteId = parentSet?.remoteId ?: return@forEach

            try {
                val remoteDto = api.createSessionRecord(
                    token = authHeader,
                    dto = localRecord.toDto(parentRemoteId)
                )

                val syncedRecord = localRecord.copy(
                    remoteId = remoteDto.id,
                    timestamp = remoteDto.timestamp ?: localRecord.timestamp
                )
                sessionRecordDao.updateRecord(syncedRecord)
            } catch (e: Exception) {
                if (e.isNotFoundError()) {
                    parentSet?.let { studySetDao.deleteSet(it) }
                } else {
                    handleSyncException(
                        exception = e,
                        contextMessage = "Не удалось синхронизировать запись сессии id=${localRecord.id}"
                    )
                    shouldRetry = true
                }
            }
        }

        if (shouldRetry) {
            throw SyncRetryException("Часть операций синхронизации сессий завершилась с ошибкой")
        }
    }

    override suspend fun fetchRemoteChanges() {
        val tokenString = settingsRepository.token.first() ?: return
        val authHeader = "Bearer $tokenString"

        val syncedSets = studySetDao.getSyncedSets()
        var shouldRetry = false

        syncedSets.forEach { localSet ->
            try {
                val remoteRecords = api.getSessionRecordsBySet(
                    token = authHeader,
                    setId = localSet.remoteId!!
                )

                remoteRecords.forEach { dto ->
                    val localRecord = sessionRecordDao.getRecordByRemoteId(dto.id!!)

                    if (localRecord == null) {
                        sessionRecordDao.insertRecord(dto.toEntity(localSet.id))
                    }
                }
            } catch (e: Exception) {
                handleSyncException(
                    exception = e,
                    contextMessage = "Не удалось получить записи сессий набора id=${localSet.id}"
                )
                shouldRetry = true
            }
        }

        if (shouldRetry) {
            throw SyncRetryException("Не удалось получить часть записей сессий с сервера")
        }
    }

    private fun handleSyncException(
        exception: Exception,
        contextMessage: String
    ) {
        when {
            exception.isAuthError() -> {
                Log.w(TAG, contextMessage, exception)
                throw SyncAuthException(contextMessage, exception)
            }

            else -> {
                Log.w(TAG, contextMessage, exception)
            }
        }
    }

    private fun Exception.isAuthError(): Boolean {
        return this is HttpException && (code() == 401 || code() == 403)
    }

    private fun Exception.isNotFoundError(): Boolean {
        return this is HttpException && code() == 404
    }
}