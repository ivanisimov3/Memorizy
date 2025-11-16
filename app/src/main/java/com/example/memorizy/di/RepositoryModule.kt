package com.example.memorizy.di

import com.example.memorizy.data.repository.CardRepositoryImpl
import com.example.memorizy.data.repository.StudySetRepositoryImpl
import com.example.memorizy.domain.repository.CardRepository
import com.example.memorizy.domain.repository.StudySetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

// Модуль для связывания общих интерфейсов с конкретной реализацией
@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

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
}