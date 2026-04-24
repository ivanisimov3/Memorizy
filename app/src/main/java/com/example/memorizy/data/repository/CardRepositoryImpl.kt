package com.example.memorizy.data.repository

import android.util.Log
import com.example.memorizy.data.mapper.toDto
import com.example.memorizy.data.mapper.toEntity
import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.source.local.room.dao.CardDao
import com.example.memorizy.data.source.local.room.dao.StudySetDao
import com.example.memorizy.data.source.network.MemorizyApiService
import com.example.memorizy.data.sync.SyncAuthException
import com.example.memorizy.data.sync.SyncRetryException
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import retrofit2.HttpException
import javax.inject.Inject

// Реализация репозитория карточка

class CardRepositoryImpl @Inject constructor(   // Inject позволяет связать создание этого репозитория с dao
    private val dao: CardDao,
    private val studySetDao: StudySetDao,
    private val api: MemorizyApiService,
    private val settingsRepository: SettingsRepository
) : CardRepository {

    companion object {
        private const val TAG = "CardRepository"
    }

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
        var shouldRetry = false

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
                handleSyncException(
                    exception = e,
                    contextMessage = "Не удалось синхронизировать новую карточку id=${localCard.id}"
                )
                shouldRetry = true
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
                handleSyncException(
                    exception = e,
                    contextMessage = "Не удалось синхронизировать измененную карточку id=${localCard.id}"
                )
                shouldRetry = true
            }
        }

        val deletedCards = dao.getCardsToDelete()
        deletedCards.forEach { localCard ->
            try{
                api.deleteCard(token = authHeader, id = localCard.remoteId!!)

                dao.deleteCard(localCard)
            } catch (e: Exception) {
                handleSyncException(
                    exception = e,
                    contextMessage = "Не удалось удалить карточку на сервере id=${localCard.id}"
                )
                shouldRetry = true
            }
        }

        if (shouldRetry) {
            throw SyncRetryException("Часть операций синхронизации карточек завершилась с ошибкой")
        }
    }

    override suspend fun fetchRemoteChanges() {
        val tokenString = settingsRepository.token.first() ?: return
        val authHeader = "Bearer $tokenString"

        val syncedSets = studySetDao.getSyncedSets()
        var shouldRetry = false

        syncedSets.forEach { localSet->
            try {
                val remoteCards = api.getCardsBySet(token = authHeader, setId = localSet.remoteId!!)

                remoteCards.forEach { dto ->
                    val localCard = dao.getCardByRemoteId(dto.id!!)

                    if (localCard == null){
                        dao.insertCard(dto.toEntity(localSet.id))
                    } else if (!localCard.isEdited && !localCard.isDeleted) {
                        val updatedCard = localCard.copy(
                            term = dto.term,
                            definition = dto.definition,
                            definitionVariants = dto.definitionVariants,
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
                handleSyncException(
                    exception = e,
                    contextMessage = "Не удалось получить карточки набора id=${localSet.id}"
                )
                shouldRetry = true
            }
        }

        if (shouldRetry) {
            throw SyncRetryException("Не удалось получить часть карточек с сервера")
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