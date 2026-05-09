package com.example.memorizy.domain.text_comparison

data class TextComparisonResult(
    val category: TextComparisonCategory,
    val isCorrect: Boolean,
    val fuzzyScore: Float,
    val entailmentScore: Float,
    val contradictionScore: Float,
    val neutralScore: Float
)