package com.example.memorizy.data.repository

import com.example.memorizy.data.mapper.toDto
import com.example.memorizy.data.mapper.toEntity
import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.source.local.room.dao.CardDao
import com.example.memorizy.data.source.local.room.dao.StudySetDao
import com.example.memorizy.data.source.network.MemorizyApiService
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

// Реализация репозитория карточка

class CardRepositoryImpl @Inject constructor(   // Inject позволяет связать создание этого репозитория с dao
    private val dao: CardDao,
    private val studySetDao: StudySetDao,
    private val api: MemorizyApiService,
    private val settingsRepository: SettingsRepository
) : CardRepository {

    override suspend fun insertCard(card: Card) {
        return dao.insertCard(card)
    }

    override suspend fun insertCards(cards: List<Card>) {
        dao.insertCards(cards)
    }

    override suspend fun updateCard(card: Card) {
        return dao.updateCard(card)
    }

    override suspend fun markAsDeleted(id: Long) {
        dao.markAsDeletedCard(id)
    }

    override suspend fun deleteCard(card: Card) {
        return dao.deleteCard(card)
    }

    override fun getAllCardsForSet(setId: Long): Flow<List<Card>> {
        return dao.getAllCardsForSet(setId)
    }

    override fun getAllNonDeletedCards(): Flow<List<Card>> {
        return dao.getAllNonDeletedCards()
    }

    override suspend fun getAllNonDeletedCardsSuspend(): List<Card> {
        return dao.getAllNonDeletedCardsSuspend()
    }

    override suspend fun syncLocalChanges() {
        val tokenString = settingsRepository.token.first() ?: return
        val authHeader = "Bearer $tokenString"

        val unsyncedCards = dao.getUnsyncedCards()
        unsyncedCards.forEach { localCard ->
            val parentSet = studySetDao.getSetByIdSimple(localCard.setId)
            val parentRemoteId = parentSet?.remoteId ?: return@forEach  // Буквально аналог continue, переходим к следующей карточке если null

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

        val editedCards = dao.getEditedCards()
        editedCards.forEach { localCard ->
            val parentSet = studySetDao.getSetByIdSimple(localCard.setId)
            val parentRemoteId = parentSet?.remoteId ?: return@forEach

            try {
                val remoteDto = api.updateCard(
                    token = authHeader,
                    id = localCard.remoteId!!,
                    dto = localCard.toDto(parentRemoteId)
                )

                val syncedCard = localCard.copy(
                    isEdited = false,
                    createdAt = remoteDto.createdAt ?: localCard.createdAt
                )
                dao.updateCard(syncedCard)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }

        val deletedCards = dao.getCardsToDelete()
        deletedCards.forEach { localCard ->
            try{
                api.deleteCard(token = authHeader, id = localCard.remoteId!!)

                dao.deleteCard(localCard)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    override suspend fun fetchRemoteChanges() {
        val tokenString = settingsRepository.token.first() ?: return
        val authHeader = "Bearer $tokenString"

        val syncedSets = studySetDao.getSyncedSets()

        syncedSets.forEach { localSet->
            try {
                val remoteCards = api.getCardsBySet(token = authHeader, setId = localSet.remoteId!!)

                remoteCards.forEach { dto ->
                    val localCard = dao.getCardByRemoteId(dto.id!!)

                    if (localCard == null){
                        dao.insertCard(dto.toEntity(localSet.id))
                    } else if (!localCard.isEdited) {
                        val updatedCard = localCard.copy(
                            term = dto.term,
                            definition = dto.definition,
                            createdAt = dto.createdAt ?: localCard.createdAt,
                            remoteId = dto.id,
                            isEdited = false,
                            level = dto.level,
                            nextReviewDate = dto.nextReviewDate ?: localCard.nextReviewDate
                        )
                        dao.updateCard(updatedCard)
                    }
                }

                // Множество всех Id карточек на сервере
                val remoteIds = remoteCards.mapNotNull { it.id }.toSet()
                val localSyncedCards = dao.getSyncedCardsBySet(localSet.id)

                localSyncedCards.forEach { localCard ->
                    if (localCard.remoteId!! !in remoteIds)
                        dao.deleteCard(localCard)
                }

            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
}