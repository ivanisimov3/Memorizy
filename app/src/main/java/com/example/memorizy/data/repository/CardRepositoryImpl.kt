package com.example.memorizy.data.repository

import com.example.memorizy.data.dao.CardDao
import com.example.memorizy.data.entity.Card
import com.example.memorizy.domain.repository.CardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Конкретная реализация для работы с Card
class CardRepositoryImpl @Inject constructor(
    private val dao: CardDao
) : CardRepository{
    override suspend fun insertCard(card: Card) {
        return dao.insertCard(card)
    }

    override suspend fun deleteCard(card: Card) {
        return dao.deleteCard(card)
    }

    override fun getAllCardsForSet(setId: Int): Flow<List<Card>> {
        return dao.getAllCardsForSet(setId)
    }
}