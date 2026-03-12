package com.example.memorizy.di

import com.example.memorizy.data.repository.AuthRepository
import com.example.memorizy.data.repository.AuthRepositoryImpl
import com.example.memorizy.data.repository.CardRepositoryImpl
import com.example.memorizy.data.repository.SessionRepository
import com.example.memorizy.data.repository.SessionRepositoryImpl
import com.example.memorizy.data.repository.StudySetRepositoryImpl
import com.example.memorizy.data.repository.CardRepository
import com.example.memorizy.data.repository.StudySetRepository
import com.example.memorizy.data.repository.SettingsRepository
import com.example.memorizy.data.repository.SettingsRepositoryImpl
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Связывание интерфейсов репозиториев с их реализациями

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {   // abstract class используем когда связываем

    @Binds  // Эффективнее чем Provides при работе с интерфейсами
    @Singleton
    abstract fun bindStudySetRepository(
        impl: StudySetRepositoryImpl
    ) : StudySetRepository

    @Binds
    @Singleton
    abstract fun bindCardRepository(
        impl: CardRepositoryImpl
    ) : CardRepository

    @Binds
    @Singleton
    abstract fun bingAuthRepository(
        impl: AuthRepositoryImpl
    ) : AuthRepository

    @Binds
    @Singleton
    abstract fun bindSettingsRepository(
        impl: SettingsRepositoryImpl
    ) : SettingsRepository

    @Binds
    @Singleton
    abstract fun bindSessionRepository(
        impl: SessionRepositoryImpl
    ) : SessionRepository
}