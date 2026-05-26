package com.example.memorizy.di

import com.example.memorizy.data.ml.OnnxEntailmentTextClassifier
import com.example.memorizy.domain.text_comparison.HybridTextComparator
import com.example.memorizy.domain.text_comparison.TextComparator
import com.example.memorizy.domain.text_comparison.nli.EntailmentTextClassifier
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
        impl: HybridTextComparator
    ): TextComparator

    @Binds
    @Singleton
    abstract fun bindEntailmentTextClassifier(
        impl: OnnxEntailmentTextClassifier
    ): EntailmentTextClassifier
}