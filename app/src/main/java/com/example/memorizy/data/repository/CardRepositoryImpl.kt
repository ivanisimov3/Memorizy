package com.example.memorizy.data.repository

import com.example.memorizy.data.mapper.toDto
import com.example.memorizy.data.mapper.toEntity
import com.example.memorizy.data.source.local.datastore.TokenManager
import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.source.local.room.dao.CardDao
import com.example.memorizy.data.source.local.room.dao.StudySetDao
import com.example.memorizy.data.source.network.MemorizyApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

// Конкретная реализация для работы с Card (Default implementation)
class CardRepositoryImpl @Inject constructor(   // Inject позволяет связать создание этого репозитория с dao
    private val dao: CardDao,
    private val studySetDao: StudySetDao,
    private val api: MemorizyApiService,
    private val tokenManager: TokenManager
) : CardRepository {
    override suspend fun insertCard(card: Card) {
        return dao.insertCard(card)
    }

    override suspend fun updateCard(card: Card) {
        return dao.updateCard(card)
    }

    override suspend fun deleteCard(card: Card) {
        return dao.deleteCard(card)
    }

    override fun getAllCardsForSet(setId: Long): Flow<List<Card>> {
        return dao.getAllCardsForSet(setId)
    }

    override suspend fun syncLocalChanges() {
        val tokenString = tokenManager.tokenKey.first() ?: return
        val authHeader = "Bearer $tokenString"

        val unsyncedCards = dao.getUnsyncedCards()

        unsyncedCards.forEach { localCard ->
            val parentSet = studySetDao.getSetByIdSimple(localCard.setId)
            val parentRemoteId = parentSet?.remoteId ?: return@forEach  // Буквально аналог continue, переходим к следующей карточке

            try {
                val remoteDto = api.createCard(token = authHeader, dto = localCard.toDto(parentRemoteId))

                val syncedCard = localCard.copy(
                    remoteId = remoteDto.id,
                    createdAt = remoteDto.createdAt ?: localCard.createdAt
                )
                dao.updateCard(syncedCard)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun fetchRemoteChanges() {
        val tokenString = tokenManager.tokenKey.first() ?: return
        val authHeader = "Bearer $tokenString"

        val syncedSets = studySetDao.getSyncedSets()

        syncedSets.forEach { localSet->
            try {
                val remoteCards = api.getCardsBySet(token = authHeader, setId = localSet.remoteId!!)

                remoteCards.forEach { dto ->
                    val localCard = dao.getCardByRemoteId(dto.id!!)

                    if (localCard == null){
                        dao.insertCard(dto.toEntity(localSet.id))
                    } else{
                        val updatedCard = localCard.copy(
                            term = dto.term,
                            definition = dto.definition,
                            createdAt = dto.createdAt ?: localCard.createdAt,
                            remoteId = dto.id
                        )
                        dao.updateCard(updatedCard)
                    }
                }

                // Множество всех Id карточек на сервере
                val remoteIds = remoteCards.mapNotNull { it.id }.toSet()
                val localSyncedCards = dao.getSyncedCardsBySet(localSet.id)

                // Смотрим все карточки локально
                localSyncedCards.forEach { localCard ->
                    if (localCard.remoteId!! !in remoteIds)  // Если такого Id нет на сервере, то удаляем и локально
                        dao.deleteCard(localCard)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}