package com.example.memorizy.data.repository

import android.util.Log
import com.example.memorizy.data.mapper.toDto
import com.example.memorizy.data.mapper.toEntity
import com.example.memorizy.data.source.local.room.StudySetWithCardNumber
import com.example.memorizy.data.source.local.room.entity.StudySet
import com.example.memorizy.data.sync.SyncAuthException
import com.example.memorizy.data.sync.SyncRetryException
import com.example.memorizy.data.source.local.room.dao.StudySetDao
import com.example.memorizy.data.source.network.MemorizyApiService
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException

// Реализация репозитория набор

class StudySetRepositoryImpl @Inject constructor(   // Inject позволяет связать создание этого репозитория с dao
    private val dao: StudySetDao,
    private val api: MemorizyApiService,
    private val settingsRepository: SettingsRepository
) : StudySetRepository {

    companion object {
        private const val TAG = "StudySetRepository"
    }

    override suspend fun insertSet(studySet: StudySet) {
        return dao.insertSet(studySet)
    }

    override suspend fun updateSet(studySet: StudySet) {
        return dao.updateSet(studySet)
    }

    override suspend fun markAsDeleted(id: Long) {
        dao.markAsDeletedSet(id)
    }

    override suspend fun deleteSet(studySet: StudySet) {
        return dao.deleteSet(studySet)
    }

    override fun getSet(setId: Long): Flow<StudySet> {
        return dao.getSet(setId)
    }

    override fun getAllSetsWithCardNumber(): Flow<List<StudySetWithCardNumber>> {
        return dao.getAllSetsWithCardNumber()
    }

    override suspend fun getSetName(setId: Long): String? {
        return dao.getSetName(setId)
    }

    override suspend fun syncLocalChanges() {
        val tokenString = settingsRepository.token.first() ?: return
        val authHeader = "Bearer $tokenString"
        var shouldRetry = false

        val unsyncedSets = dao.getUnsyncedSets()
        unsyncedSets.forEach { localSet ->
            try {
                val remoteDto = api.createSet(token = authHeader, dto = localSet.toDto())

                val syncedSet = localSet.copy(
                    remoteId = remoteDto.id,
                    createdAt = remoteDto.createdAt ?: localSet.createdAt
                )
                dao.updateSet(syncedSet)
            } catch (e: Exception) {
                handleSyncException(
                    exception = e,
                    contextMessage = "Не удалось синхронизировать новый набор id=${localSet.id}"
                )
                shouldRetry = true
            }
        }

        val editedSets = dao.getEditedSets()
        editedSets.forEach { localSet ->
            try {
                val remoteDto = api.updateSet(
                    token = authHeader,
                    id = localSet.remoteId!!,
                    dto = localSet.toDto()
                )

                val syncedSet = localSet.copy(
                    isEdited = false,
                    createdAt = remoteDto.createdAt ?: localSet.createdAt
                )
                dao.updateSet(syncedSet)
            } catch (e: Exception) {
                handleSyncException(
                    exception = e,
                    contextMessage = "Не удалось синхронизировать измененный набор id=${localSet.id}"
                )
                shouldRetry = true
            }
        }

        val deletedSets = dao.getSetsToDelete()
        deletedSets.forEach { localSet ->
            try{
                api.deleteSet(token = authHeader, id = localSet.remoteId!!)

                dao.deleteSet(localSet)
            } catch (e: Exception) {
                handleSyncException(
                    exception = e,
                    contextMessage = "Не удалось удалить набор на сервере id=${localSet.id}"
                )
                shouldRetry = true
            }
        }

        if (shouldRetry) {
            throw SyncRetryException("Часть операций синхронизации наборов завершилась с ошибкой")
        }
    }

    override suspend fun fetchRemoteChanges() {
        val tokenString = settingsRepository.token.first() ?: return
        val authHeader = "Bearer $tokenString"

        try {
            val remoteSets = api.getAllSets(authHeader)

            // Смотрим все наборы в сети
            remoteSets.forEach { dto ->
                val localSet = dao.getSetByRemoteId(dto.id!!)

                if (localSet == null) {
                    dao.insertSet(dto.toEntity())
                } else if (!localSet.isEdited && !localSet.isDeleted) {
                    val updatedSet = localSet.copy(
                        name = dto.name,
                        description = dto.description,
                        iconId = dto.iconId,
                        createdAt = dto.createdAt ?: localSet.createdAt,
                        remoteId = dto.id,
                        isEdited = false,
                        targetDate = dto.targetDate
                    )
                    dao.updateSet(updatedSet)
                }
            }

            // Множество всех Id наборов на сервере
            val remoteIds = remoteSets.mapNotNull { it.id }.toSet()
            val localSyncedSets = dao.getSyncedSets()

            // Смотрим все наборы локально
            localSyncedSets.forEach { localSet ->
                if (localSet.remoteId!! !in remoteIds)
                    dao.deleteSet(localSet)
            }

            settingsRepository.updateLastSyncTime()
        } catch (e: Exception) {
            handleSyncException(
                exception = e,
                contextMessage = "Не удалось получить актуальные наборы с сервера"
            )
            throw SyncRetryException("Не удалось получить изменения наборов", e)
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
}