package com.example.memorizy.di

import android.content.Context
import androidx.room.Room
import com.example.memorizy.data.source.local.AppDatabase
import com.example.memorizy.data.source.local.card.CardDao
import com.example.memorizy.data.source.local.studyset.StudySetDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class) // Зависимости живут, пока живо приложение
object DataModule { // object используем когда нужно произвести объекты (фабрика)

    // Учим создавать AppDatabase
    @Provides
    @Singleton  // Будет создан ТОЛЬКО ОДИН экземпляр
    fun provideAppDatabase(@ApplicationContext context: Context): AppDatabase { // контекст всего приложения
        return Room.databaseBuilder(
            context = context,
            klass = AppDatabase::class.java,
            name = "memorizy_db"
        ).build()
    }

    // Учим создавать StudySetDao
    @Provides
    @Singleton
    fun provideStudySetDao(db: AppDatabase): StudySetDao {
        return db.studySetDao()
    }

    // Учим создавать CardDao
    @Provides
    @Singleton
    fun provideCardDao(db: AppDatabase): CardDao {
        return db.cardDao()
    }
}