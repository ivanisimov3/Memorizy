package com.example.memorizy

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.os.Build
import androidx.hilt.work.HiltWorkerFactory
import androidx.work.Configuration
import dagger.hilt.android.HiltAndroidApp
import jakarta.inject.Inject

// Точка старта приложения

@HiltAndroidApp // Прикрепляем Hilt к приложению на все время его жизни
class MemorizyApplication : Application(), Configuration.Provider {

    @Inject
    lateinit var workerFactory: HiltWorkerFactory   // Учим WorkManager работать с Hilt

    override fun onCreate() {
        super.onCreate()
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "review_reminder",
                "Напоминания о повторении",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Уведомления о карточках, которые пора повторить"
            }
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }
    }

    override val workManagerConfiguration: Configuration
        get() = Configuration.Builder()
            .setWorkerFactory(workerFactory)
            .build()
}