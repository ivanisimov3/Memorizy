package com.example.memorizy.domain.text_comparison.nli

interface EntailmentTextClassifier {

    suspend fun classify(premise: String, hypothesis: String): EntailmentComparisonResult
}