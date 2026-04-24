package com.example.memorizy.data.ml

data class TokenizedText(
    val inputIds: LongArray,
    val attentionMask: LongArray,
    val tokenTypeIds: LongArray
)