package com.example.memorizy.domain.repository

import com.example.memorizy.data.entity.Card
import kotlinx.coroutines.flow.Flow

// Общий шаблон работы с Card
interface CardRepository {

    suspend fun insertCard(card: Card)

    suspend fun deleteCard(card: Card)

    fun getAllCardsForSet(setId: Int): Flow<List<Card>>
}