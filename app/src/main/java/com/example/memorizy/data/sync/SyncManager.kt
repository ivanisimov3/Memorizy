package com.example.memorizy.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.example.memorizy.workers.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.concurrent.TimeUnit

// В WorkRequest определяем когда worker запускатся (единично или периодично)
// В Work Manager настраивается расписание и производится запуск воркера

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {
    companion object {
        private const val ONE_TIME_SYNC_WORK_NAME = "OneTimeSync"
        private const val DAILY_UPLOAD_WORK_NAME = "DailyUploadSync"
    }

    fun scheduleOneTimeSync() {
        val request = OneTimeWorkRequestBuilder<SyncWorker>()   // WorkRequest
            .setConstraints(syncConstraints())
            .setInputData(workDataOf(SyncWorker.KEY_SYNC_MODE to SyncMode.FULL.name))
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork( // WorkManager
            ONE_TIME_SYNC_WORK_NAME,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    fun scheduleDailySync() {
        val request = PeriodicWorkRequestBuilder<SyncWorker>(
            24,
            TimeUnit.HOURS
        )
            .setConstraints(syncConstraints())
            .setInputData(workDataOf(SyncWorker.KEY_SYNC_MODE to SyncMode.UPLOAD_ONLY.name))
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            DAILY_UPLOAD_WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }

    private fun syncConstraints(): Constraints {
        return Constraints.Builder()    // Требования, чтобы WorkRequest выполнился
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
    }
}