package com.example.memorizy.domain.textcomparison.nli

interface EntailmentTextClassifier {

    suspend fun classify(premise: String, hypothesis: String): EntailmentComparisonResult
}