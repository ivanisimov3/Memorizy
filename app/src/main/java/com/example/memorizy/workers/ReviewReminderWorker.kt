package com.example.memorizy.workers

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.memorizy.MainActivity
import com.example.memorizy.R
import com.example.memorizy.data.repository.CardRepository
import com.example.memorizy.data.repository.SettingsRepository
import com.example.memorizy.data.repository.StudySetRepository
import com.example.memorizy.data.source.local.room.entity.Card
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlin.math.round

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
class ReviewReminderWorker @AssistedInject constructor(
    @Assisted private val ctx: Context,
    @Assisted params: WorkerParameters,
    private val cardRepository: CardRepository,
    private val studySetRepository: StudySetRepository,
    private val settingsRepository: SettingsRepository
) : CoroutineWorker(ctx, params) {

    companion object {
        const val CHANNEL_ID = "review_reminder"
        const val NOTIFICATION_ID = 1
        const val COOLDOWN_MS = 6 * 60 * 60 * 1000L // 6 часов
        const val MAX_OVERDUE_MS = 24 * 60 * 60 * 1000L // 24 часа
        const val MAX_THRESHOLD = 10

        fun shouldShowNotification(
            allCards: List<Card>,
            dueCards: List<Card>,
            lastNotificationTime: Long,
            now: Long
        ): Boolean {
            if (now - lastNotificationTime < COOLDOWN_MS) return false

            if (dueCards.isEmpty()) return false

            val threshold = minOf(
                round(allCards.size / 2.0).toInt(),
                MAX_THRESHOLD
            ).coerceAtLeast(1)

            if (dueCards.size >= threshold) return true

            // Если какая-то карточка слишком долго ждёт
            if (dueCards.any { now - it.nextReviewDate > MAX_OVERDUE_MS }) return true

            return false
        }
    }

    override suspend fun doWork(): Result {
        return try {
            val now = System.currentTimeMillis()
            val lastNotificationTime = settingsRepository.getLastNotificationTime()

            val allCards = cardRepository.getAllNonDeletedCardsSuspend()
            val dueCards = allCards.filter { it.nextReviewDate <= now }

            if (!shouldShowNotification(allCards, dueCards, lastNotificationTime, now)) {
                return Result.success()
            }

            val setIds = dueCards.map { it.setId }.distinct()
            val notificationText = if (setIds.size == 1) {
                val setName = studySetRepository.getSetName(setIds.first()) ?: "набора"
                "У вас ${dueCards.size} карточек в наборе \"$setName\""
            } else {
                "У вас ${dueCards.size} карточек в ${setIds.size} наборах"
            }

            showNotification(title = "Пора повторять!", text = notificationText)
            settingsRepository.setLastNotificationTime(now)

            Result.success()
        } catch (e: Exception) {
            e.printStackTrace()
            Result.retry()
        }
    }

    private fun showNotification(title: String, text: String) {
        val intent = Intent(ctx, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK    // Закрыть все предыдущие экраны, если приложение уже открыто
        }
        val pendingIntent = PendingIntent.getActivity(
            ctx,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(ctx, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(text)
            .setStyle(NotificationCompat.BigTextStyle().bigText(text))
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val notificationManager =
            ctx.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(NOTIFICATION_ID, notification)
    }
}