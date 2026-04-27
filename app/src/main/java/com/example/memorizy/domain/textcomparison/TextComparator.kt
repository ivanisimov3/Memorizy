package com.example.memorizy.domain.textcomparison

// Интерфейс для сравнения двух текстов

interface TextComparator {

    suspend fun compareDetailed(expected: String, actual: String): TextComparisonResult

    suspend fun compare(expected: String, actual: String): Boolean {
        return compareDetailed(expected, actual).isCorrect
    }
}