package com.example.memorizy.data.repository

import com.example.memorizy.data.mapper.toDto
import com.example.memorizy.data.mapper.toEntity
import com.example.memorizy.data.source.local.datastore.SettingsDataStore
import com.example.memorizy.data.source.local.room.StudySetWithCardNumber
import com.example.memorizy.data.source.local.room.entity.StudySet
import com.example.memorizy.data.source.local.room.dao.StudySetDao
import com.example.memorizy.data.source.network.MemorizyApiService
import jakarta.inject.Inject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

// Конкретная реализация для работы с StudySetDao (Default implementation)
class StudySetRepositoryImpl @Inject constructor(   // Inject позволяет связать создание этого репозитория с dao
    private val dao: StudySetDao,
    private val api: MemorizyApiService,
    private val settingsDataStore: SettingsDataStore
) : StudySetRepository {

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

    override suspend fun syncLocalChanges() {
        val tokenString = settingsDataStore.token.first() ?: return
        val authHeader = "Bearer $tokenString"

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
                e.printStackTrace()
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
                e.printStackTrace()
            }
        }

        val deletedSets = dao.getSetsToDelete()
        deletedSets.forEach { localSet ->
            try{
                api.deleteSet(token = authHeader, id = localSet.remoteId!!)

                dao.deleteSet(localSet)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun fetchRemoteChanges() {
        val tokenString = settingsDataStore.token.first() ?: return
        val authHeader = "Bearer $tokenString"

        try {
            val remoteSets = api.getAllSets(authHeader)

            // Смотрим все наборы в сети
            remoteSets.forEach { dto ->
                val localSet = dao.getSetByRemoteId(dto.id!!)

                if (localSet == null) { // Если такого набора нет локально (с таким remoteId) - добавляем
                    dao.insertSet(dto.toEntity())
                } else if (!localSet.isEdited) {    // Обновляем только если нет pending изменений
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
                if (localSet.remoteId!! !in remoteIds)  // Если такого Id нет на сервере, то удаляем и локально
                    dao.deleteSet(localSet)
            }

            settingsDataStore.updateLastSyncTime()
        } catch (e: Exception) {
            e.printStackTrace()
        }

    }
}