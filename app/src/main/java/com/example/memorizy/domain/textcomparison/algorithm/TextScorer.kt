package com.example.memorizy.domain.textcomparison.algorithm

interface TextScorer {

    fun score(expected: String, actual: String): Float
}