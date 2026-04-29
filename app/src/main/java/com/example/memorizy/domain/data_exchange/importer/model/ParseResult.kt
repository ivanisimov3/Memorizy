package com.example.memorizy.domain.data_exchange.importer.model

data class ParseResult(
    val successfulCards: List<ParsedCard>,
    val errors: List<CardImportError>
)