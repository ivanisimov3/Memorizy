package com.example.memorizy.domain.importer.model

data class ParseResult(
    val successfulCards: List<ParsedCard>,
    val errors: List<CardImportError>
)