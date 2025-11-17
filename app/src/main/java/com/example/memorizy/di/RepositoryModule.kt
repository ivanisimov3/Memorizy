package com.example.memorizy.di

import com.example.memorizy.data.card.CardRepositoryImpl
import com.example.memorizy.data.studyset.StudySetRepositoryImpl
import com.example.memorizy.domain.card.CardRepository
import com.example.memorizy.domain.studyset.StudySetRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

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
}