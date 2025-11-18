package com.example.memorizy.data.repository.cardrepository

import com.example.memorizy.data.source.local.card.Card
import kotlinx.coroutines.flow.Flow

// Interface to the data layer (card).
interface CardRepository {

    suspend fun insertCard(card: Card)

    suspend fun deleteCard(card: Card)

    fun getAllCardsForSet(setId: Int): Flow<List<Card>>
}