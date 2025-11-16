package com.example.memorizy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

// Прикрепляем Hilt к приложению
@HiltAndroidApp
class MemorizyApplication : Application() {
}