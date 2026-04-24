package com.example.memorizy.domain.textcomparison.semantic

// Интерфейс для превращения текста в вектор

interface TextEmbeddingModel {

    suspend fun embed(text: String): FloatArray
}