package com.example.memorizy.data.sync

import com.example.memorizy.data.repository.CardRepository
import com.example.memorizy.data.repository.StudySetRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class SyncCoordinator @Inject constructor(
    private val studySetRepository: StudySetRepository,
    private val cardRepository: CardRepository
) {
    private val syncMutex = Mutex() // Гарантировать только одну корутину одновременно

    suspend fun syncAll() {
        syncMutex.withLock {    // Второй sync ждет первый
            studySetRepository.syncLocalChanges()
            studySetRepository.fetchRemoteChanges()

            cardRepository.syncLocalChanges()
            cardRepository.fetchRemoteChanges()
        }
    }
}