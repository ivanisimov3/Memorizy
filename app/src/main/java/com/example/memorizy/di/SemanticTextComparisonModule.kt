package com.example.memorizy.di

import com.example.memorizy.data.ml.OnnxTextEmbeddingModel
import com.example.memorizy.domain.textcomparison.SemanticTextComparator
import com.example.memorizy.domain.textcomparison.semantic.RubertTinyLiteTextComparator
import com.example.memorizy.domain.textcomparison.semantic.TextEmbeddingModel
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class SemanticTextComparisonModule {

    @Binds
    @Singleton
    abstract fun bindTextEmbeddingModel(
        impl: OnnxTextEmbeddingModel
    ): TextEmbeddingModel

    @Binds
    @Singleton
    abstract fun bindSemanticTextComparator(
        impl: RubertTinyLiteTextComparator
    ): SemanticTextComparator
}