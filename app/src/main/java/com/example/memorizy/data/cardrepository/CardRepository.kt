package com.example.memorizy.data.cardrepository

import com.example.memorizy.data.source.local.card.Card
import kotlinx.coroutines.flow.Flow

// Общий шаблон работы с Card
interface CardRepository {

    suspend fun insertCard(card: Card)

    suspend fun deleteCard(card: Card)

    fun getAllCardsForSet(setId: Int): Flow<List<Card>>
}