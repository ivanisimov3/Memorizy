package com.example.memorizy.domain.textcomparison

interface TextScorer {

    fun score(expected: String, actual: String): Float
}