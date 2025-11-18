package com.example.memorizy.data.cardrepository

import com.example.memorizy.data.source.local.card.Card
import com.example.memorizy.data.source.local.card.CardDao
import com.example.memorizy.data.cardrepository.CardRepository
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

// Конкретная реализация для работы с Card
class CardRepositoryImpl @Inject constructor(   // Inject позволяет связать создание этого репозитория с dao
    private val dao: CardDao
) : CardRepository {
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