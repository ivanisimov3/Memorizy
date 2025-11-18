package com.example.memorizy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Прикрепляем Hilt к приложению на все время его жизни
@HiltAndroidApp
class MemorizyApplication : Application() {
}