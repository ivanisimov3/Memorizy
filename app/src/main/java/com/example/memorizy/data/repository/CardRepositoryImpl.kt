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

class CardRepositoryImpl @Inject constructor(   // Inject позволяет связать создание этого репозитория с cardDao
    private val cardDao: CardDao,
    private val studySetDao: StudySetDao,
    private val api: MemorizyApiService,
    private val settingsRepository: SettingsRepository
) : CardRepository {

    companion object {
        private const val TAG = "CardRepository"
    }

    override suspend fun insertCard(card: Card) {
        return cardDao.insertCard(card)
    }

    override suspend fun insertCards(cards: List<Card>) {
        cardDao.insertCards(cards)
    }

    override suspend fun updateCard(card: Card) {
        return cardDao.updateCard(card)
    }

    override suspend fun markAsDeleted(id: Long) {
        cardDao.markAsDeletedCard(id)
    }

    override suspend fun deleteCard(card: Card) {
        return cardDao.deleteCard(card)
    }

    override fun getAllCardsForSet(setId: Long): Flow<List<Card>> {
        return cardDao.getAllCardsForSet(setId)
    }

    override fun getAllNonDeletedCards(): Flow<List<Card>> {
        return cardDao.getAllNonDeletedCards()
    }

    override suspend fun getAllNonDeletedCardsSuspend(): List<Card> {
        return cardDao.getAllNonDeletedCardsSuspend()
    }

    override suspend fun syncLocalChanges() {
        val tokenString = settingsRepository.token.first() ?: return
        val authHeader = "Bearer $tokenString"
        var shouldRetry = false

        val unsyncedCards = cardDao.getUnsyncedCards()
        unsyncedCards.forEach { localCard ->
            val parentSet = studySetDao.getSetByIdSimple(localCard.setId)
            val parentRemoteId = parentSet?.remoteId ?: return@forEach  // Буквально аналог continue, переходим к следующей карточке если null

            try {
                val remoteDto = api.createCard(
                    token = authHeader,
                    dto = localCard.toDto(parentRemoteId)
                )

                val syncedCard = localCard.copy(
                    remoteId = remoteDto.id,
                    createdAt = remoteDto.createdAt ?: localCard.createdAt,
                    isEdited = false
                )
                cardDao.updateCard(syncedCard)
            } catch (e: Exception) {
                if (e.isNotFoundError()) {
                    parentSet?.let { studySetDao.deleteSet(it) }
                } else {
                    handleSyncException(
                        exception = e,
                        contextMessage = "Не удалось синхронизировать новую карточку id=${localCard.id}"
                    )
                    shouldRetry = true
                }
            }
        }

        val editedCards = cardDao.getEditedCards()
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
                cardDao.updateCard(syncedCard)
            } catch (e: Exception) {
                if (e.isNotFoundError()) {
                    cardDao.deleteCard(localCard)
                } else {
                    handleSyncException(
                        exception = e,
                        contextMessage = "Не удалось синхронизировать измененную карточку id=${localCard.id}"
                    )
                    shouldRetry = true
                }
            }
        }

        val deletedCards = cardDao.getCardsToDelete()
        deletedCards.forEach { localCard ->
            try{
                api.deleteCard(token = authHeader, id = localCard.remoteId!!)

                cardDao.deleteCard(localCard)
            } catch (e: Exception) {
                if (e.isNotFoundError()) {
                    cardDao.deleteCard(localCard)
                } else {
                    handleSyncException(
                        exception = e,
                        contextMessage = "Не удалось удалить карточку на сервере id=${localCard.id}"
                    )
                    shouldRetry = true
                }
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
                val remoteCards = api.getCardsBySet(
                    token = authHeader,
                    setId = localSet.remoteId!!
                )

                remoteCards.forEach { dto ->
                    val localCard = cardDao.getCardByRemoteId(dto.id!!)

                    if (localCard == null){
                        cardDao.insertCard(dto.toEntity(localSet.id))
                    } else if (!localCard.isEdited && !localCard.isDeleted) {
                        val updatedCard = localCard.copy(
                            term = dto.term,
                            definition = dto.definition,
                            definitionVariants = dto.definitionVariants,
                            createdAt = dto.createdAt ?: localCard.createdAt,
                            remoteId = dto.id,
                            isEdited = false,
                            level = dto.level,
                            nextReviewDate = dto.nextReviewDate ?: localCard.nextReviewDate,
                            reviewCount = dto.reviewCount,
                            mistakeCount = dto.mistakeCount,
                            recentAnswerHistory = dto.recentAnswerHistory
                        )
                        cardDao.updateCard(updatedCard)
                    }
                }

                // Множество всех Id карточек на сервере
                val remoteIds = remoteCards.mapNotNull { it.id }.toSet()
                val localSyncedCards = cardDao.getSyncedCardsBySet(localSet.id)

                localSyncedCards.forEach { localCard ->
                    if (localCard.remoteId!! !in remoteIds)
                        cardDao.deleteCard(localCard)
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

    private fun Exception.isNotFoundError(): Boolean {
        return this is HttpException && code() == 404
    }
}