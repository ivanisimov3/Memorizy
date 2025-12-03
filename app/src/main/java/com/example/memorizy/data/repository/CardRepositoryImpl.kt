package com.example.memorizy.data.repository

import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.source.local.room.dao.CardDao
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Конкретная реализация для работы с Card (Default implementation)
class CardRepositoryImpl @Inject constructor(   // Inject позволяет связать создание этого репозитория с dao
    private val dao: CardDao
) : CardRepository {
    override suspend fun insertCard(card: Card) {
        return dao.insertCard(card)
    }

    override suspend fun deleteCard(card: Card) {
        return dao.deleteCard(card)
    }

    override fun getAllCardsForSet(setId: Long): Flow<List<Card>> {
        return dao.getAllCardsForSet(setId)
    }
}