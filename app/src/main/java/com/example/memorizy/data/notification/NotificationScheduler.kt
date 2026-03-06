package com.example.memorizy.data.notification

import android.content.Context
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.memorizy.data.repository.SettingsRepository
import com.example.memorizy.workers.ReviewReminderWorker
import dagger.hilt.android.qualifiers.ApplicationContext
import jakarta.inject.Inject
import jakarta.inject.Singleton
import java.util.concurrent.TimeUnit

// Планировщик уведомлений о повторении карточек

@Singleton
class NotificationScheduler @Inject constructor(
    @ApplicationContext private val context: Context,
    private val settingsRepository: SettingsRepository
) {

    companion object {
        const val WORK_NAME = "ReviewReminderPeriodic"
        const val REPEAT_INTERVAL_HOURS = 6L
    }

    suspend fun startPeriodicReminders() {
        settingsRepository.setLastNotificationTime(System.currentTimeMillis())

        val request = PeriodicWorkRequestBuilder<ReviewReminderWorker>(
            REPEAT_INTERVAL_HOURS, TimeUnit.HOURS
        ).build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.KEEP, // Не перезаписывать, если уже запущен
            request
        )
    }

    fun cancelPeriodicReminders() {
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }
}