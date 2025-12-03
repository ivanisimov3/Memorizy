package com.example.memorizy.data.repository

import com.example.memorizy.data.mapper.toDto
import com.example.memorizy.data.mapper.toEntity
import com.example.memorizy.data.source.local.datastore.TokenManager
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
    private val tokenManager: TokenManager
) : StudySetRepository {

    override suspend fun insertSet(studySet: StudySet) {
        return dao.insertSet(studySet)
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
        val tokenString = tokenManager.tokenKey.first() ?: return
        val authHeader = "Bearer $tokenString"

        val unsyncedSets = dao.getUnsyncedSets()

        unsyncedSets.forEach { localSet ->
            try {
                val remoteDto = api.createSet(token = authHeader, dto = localSet.toDto())

                val syncedSet = localSet.copy(
                    remoteId = remoteDto.id,
                    createdAt = remoteDto.createdAt ?: localSet.createdAt
                )
                dao.insertSet(syncedSet)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun fetchRemoteChanges() {
        val tokenString = tokenManager.tokenKey.first() ?: return
        val authHeader = "Bearer $tokenString"

        try {
            val remoteSets = api.getAllSets(authHeader)

            remoteSets.forEach { dto ->
                val localSet = dao.getSetByRemoteId(dto.id!!)

                if (localSet == null) {
                    dao.insertSet(dto.toEntity())
                } else {
                    val updatedSet = localSet.copy(
                        name = dto.name,
                        description = dto.description,
                        iconId = dto.iconId,
                        createdAt = dto.createdAt ?: localSet.createdAt,
                        remoteId = dto.id
                    )
                    dao.insertSet(updatedSet)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}