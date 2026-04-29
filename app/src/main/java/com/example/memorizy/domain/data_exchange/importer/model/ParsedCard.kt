package com.example.memorizy.domain.data_exchange.importer.model

data class ParsedCard(
    val term: String,
    val definition: String,
    val definitionVariants: List<String> = emptyList()
)