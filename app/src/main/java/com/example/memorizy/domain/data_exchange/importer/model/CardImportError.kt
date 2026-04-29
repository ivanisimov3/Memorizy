package com.example.memorizy.domain.data_exchange.importer.model

data class CardImportError(
    val lineNumber: Int,
    val reason: String
)