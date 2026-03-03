package com.example.memorizy.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import com.example.memorizy.workers.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton

// В WorkRequest определяем когда worker запускатся (единично или периодично)
// В Work Manager настраивается расписание и производится запуск воркера

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun scheduleOneTimeSync() {
        val constraints = Constraints.Builder() // Требования, чтобы WorkRequest выполнился
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val request = OneTimeWorkRequestBuilder<SyncWorker>()   // WorkRequest
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork( // WorkManager
            "OneTimeSync",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}