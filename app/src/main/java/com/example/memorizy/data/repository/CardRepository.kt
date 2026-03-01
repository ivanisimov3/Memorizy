package com.example.memorizy.data.repository

import com.example.memorizy.data.source.local.room.entity.Card
import kotlinx.coroutines.flow.Flow

// Interface to the data layer (card).
interface CardRepository {

    suspend fun insertCard(card: Card)

    suspend fun updateCard(card: Card)

    suspend fun markAsDeleted(id: Long)

    suspend fun deleteCard(card: Card)

    fun getAllCardsForSet(setId: Long): Flow<List<Card>>

    fun getAllNonDeletedCards(): Flow<List<Card>>

    suspend fun syncLocalChanges()

    suspend fun fetchRemoteChanges()
}