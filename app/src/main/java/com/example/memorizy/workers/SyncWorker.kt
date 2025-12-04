package com.example.memorizy.workers

import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.memorizy.data.repository.CardRepository
import com.example.memorizy.data.repository.StudySetRepository
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject

/*
Worker is a class that performs work synchronously on a background thread.
As we are interested in asynchronous work, we can use CoroutineWorker,
which has interoperability with Kotlin Coroutines.
*/

/*
The Context and WorkerParameters are provided by WorkManager at runtime,
so they must be marked with @Assisted to indicate they are not managed by Hilt
and should be passed as constructor parameters.
*/

@HiltWorker
class SyncWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val studySetRepository: StudySetRepository,
    private val cardRepository: CardRepository
    // private val cardRepository: CardRepository
) : CoroutineWorker(ctx , params) {

    // This method is where you put the code for the actual work you want to perform in the background.
    override suspend fun doWork(): Result {
        return try {
            studySetRepository.syncLocalChanges()
            studySetRepository.fetchRemoteChanges()

            cardRepository.syncLocalChanges()
            cardRepository.fetchRemoteChanges()

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()

            Result.retry()  // Indicates that the work needs to be retried
        }
    }
}