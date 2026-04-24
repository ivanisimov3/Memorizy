package com.example.memorizy.domain.textcomparison

interface SemanticTextComparator {

    suspend fun compare(expected: String, actual: String): SemanticComparisonResult
}