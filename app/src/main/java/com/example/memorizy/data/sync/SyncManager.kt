package com.example.memorizy.data.sync

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.memorizy.workers.SyncWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.concurrent.TimeUnit

// WorkRequest is where you define if the worker needs to be run once or periodically

// Work Manager is a class that actually schedules your WorkRequest and makes it run.

@Singleton
class SyncManager @Inject constructor(
    @ApplicationContext private val context: Context
) {

    fun scheduleOneTimeSync() {
        // A specification of the requirements that need to be met before a WorkRequest can run
        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)  // Нужен интернет
            .build()

        val request = OneTimeWorkRequestBuilder<SyncWorker>()
            .setConstraints(constraints)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            "OneTimeSync",
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}