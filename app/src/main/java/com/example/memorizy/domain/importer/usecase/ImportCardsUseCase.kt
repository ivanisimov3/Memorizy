package com.example.memorizy.domain.importer.usecase

import com.example.memorizy.data.repository.CardRepository
import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.sync.SyncManager
import com.example.memorizy.domain.importer.model.ParsedCard
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ImportCardsUseCase @Inject constructor(
    private val cardRepository: CardRepository,
    private val syncManager: SyncManager
) {
    suspend operator fun invoke(setId: Long, parsedCards: List<ParsedCard>) = withContext(Dispatchers.IO) {
        if (parsedCards.isEmpty()) return@withContext
        
        val cardsToInsert = parsedCards.map {
            Card(
                setId = setId,
                term = it.term,
                definition = it.definition
            )
        }
        
        cardRepository.insertCards(cardsToInsert)
        syncManager.scheduleOneTimeSync()
    }
}
