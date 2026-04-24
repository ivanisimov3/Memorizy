package com.example.memorizy.domain.textcomparison

data class SemanticComparisonResult(
    val isSimilar: Boolean,
    val similarity: Float,
    val threshold: Float
)