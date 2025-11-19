package com.example.memorizy

import android.app.Application
import dagger.hilt.android.HiltAndroidApp

@HiltAndroidApp // Прикрепляем Hilt к приложению на все время его жизни
class MemorizyApplication : Application() {
}