package com.example.memorizy.domain.text_comparison

// Интерфейс для сравнения двух текстов

interface TextComparator {

    suspend fun compareDetailed(expected: String, actual: String): TextComparisonResult

    suspend fun compare(expected: String, actual: String): Boolean {
        return compareDetailed(expected, actual).isCorrect
    }
}