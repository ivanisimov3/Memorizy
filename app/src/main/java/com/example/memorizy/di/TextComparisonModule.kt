package com.example.memorizy.di

import com.example.memorizy.domain.textcomparison.FuzzyTokenComparator
import com.example.memorizy.domain.textcomparison.TextComparator
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

// Связывание интерфейса сравнения текста с основной реализацией

@Module
@InstallIn(SingletonComponent::class)
abstract class TextComparisonModule {

    @Binds
    @Singleton
    abstract fun bindTextComparator(
        impl: FuzzyTokenComparator
    ): TextComparator
}