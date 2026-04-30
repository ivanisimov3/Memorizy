package com.example.memorizy.data.sync

import com.example.memorizy.data.repository.CardRepository
import com.example.memorizy.data.repository.SessionRepository
import com.example.memorizy.data.repository.SettingsRepository
import com.example.memorizy.data.repository.StudySetRepository
import jakarta.inject.Inject
import jakarta.inject.Singleton
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock

@Singleton
class SyncCoordinator @Inject constructor(
    private val studySetRepository: StudySetRepository,
    private val cardRepository: CardRepository,
    private val sessionRepository: SessionRepository,
    private val settingsRepository: SettingsRepository
) {
    private val syncMutex = Mutex() // Гарантировать только одну корутину одновременно

    suspend fun uploadLocalChanges() {
        syncMutex.withLock {
            uploadLocalChangesLocked()
            settingsRepository.updateLastSyncTime()
        }
    }

    suspend fun fetchRemoteChanges() {
        syncMutex.withLock {
            fetchRemoteChangesLocked()
            settingsRepository.updateLastSyncTime()
        }
    }

    suspend fun syncAll() {
        syncMutex.withLock {    // Sync'и ждут друг друга
            uploadLocalChangesLocked()
            fetchRemoteChangesLocked()
            settingsRepository.updateLastSyncTime()
        }
    }

    private suspend fun uploadLocalChangesLocked() {
        studySetRepository.syncLocalChanges()
        cardRepository.syncLocalChanges()
        sessionRepository.syncLocalChanges()
    }

    private suspend fun fetchRemoteChangesLocked() {
        studySetRepository.fetchRemoteChanges()
        cardRepository.fetchRemoteChanges()
        sessionRepository.fetchRemoteChanges()
    }
}