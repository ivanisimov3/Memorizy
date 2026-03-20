package com.example.memorizy.ui.screens.addcard

import com.example.memorizy.domain.importer.model.ParseResult

data class AddCardState (
    val term: String = "",
    val definition: String = "",
    val definitionVariants: List<String> = emptyList(),
    val isTermEmptyError: Boolean = false,
    val isDefinitionEmptyError: Boolean = false,
    val isCardCreated: Boolean = false,
    val isSaving: Boolean = false,
    val isImporting: Boolean = false,
    val importSummary: ParseResult? = null,
    val showImportSummaryDialog: Boolean = false
)