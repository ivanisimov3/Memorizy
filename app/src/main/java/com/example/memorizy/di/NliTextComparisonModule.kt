package com.example.memorizy.di

import com.example.memorizy.data.ml.OnnxEntailmentTextClassifier
import com.example.memorizy.domain.text_comparison.nli.EntailmentTextClassifier
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import jakarta.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class NliTextComparisonModule {

    @Binds
    @Singleton
    abstract fun bindEntailmentTextClassifier(
        impl: OnnxEntailmentTextClassifier
    ): EntailmentTextClassifier
}