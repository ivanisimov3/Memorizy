package com.example.memorizy.domain.text_comparison.algorithm

interface TextScorer {

    fun score(expected: String, actual: String): Float
}