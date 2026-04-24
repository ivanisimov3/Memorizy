package com.example.memorizy.workers

import android.util.Log
import android.content.Context
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.memorizy.data.sync.SyncAuthException
import com.example.memorizy.data.sync.SyncCoordinator
import com.example.memorizy.data.sync.SyncRetryException
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
    private val syncCoordinator: SyncCoordinator
) : CoroutineWorker(ctx , params) {

    companion object {
        private const val TAG = "SyncWorker"
    }

    // Метод, где код работы, воспроизввдимой на заднем плане
    override suspend fun doWork(): Result {
        return try {
            syncCoordinator.syncAll()

            Result.success()
        } catch (e: SyncAuthException) {
            Log.w(TAG, "Синхронизация остановлена: требуется повторная авторизация", e)
            Result.failure()
        } catch (e: SyncRetryException) {
            Log.w(TAG, "Синхронизация завершилась частично, будет повторена", e)
            Result.retry()
        } catch (e: Exception) {
            Log.e(TAG, "Неожиданная ошибка синхронизации", e)

            Result.retry()  // Должна быть попытка повторить работу
        }
    }
}