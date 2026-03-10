package com.example.memorizy.domain.importer.model

data class CardImportError(
    val lineNumber: Int,
    val reason: String
)