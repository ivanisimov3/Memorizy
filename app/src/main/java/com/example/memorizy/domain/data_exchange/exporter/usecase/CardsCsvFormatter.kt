package com.example.memorizy.domain.data_exchange.exporter.usecase

import com.example.memorizy.data.source.local.room.entity.Card

object CardsCsvFormatter {

    fun buildFileName(setName: String): String {
        val safeName = setName
            .trim()
            .replace(Regex("""[\\/:*?"<>|]"""), "_")
            .replace(Regex("""\s+"""), " ")
            .take(MAX_FILE_NAME_LENGTH)
            .trim()
            .ifBlank { DEFAULT_FILE_NAME }

        return "$safeName.csv"
    }

    fun buildRows(cards: List<Card>): List<List<String>> {
        val maxVariantCount = cards.maxOfOrNull { it.definitionVariants.size } ?: 0

        val cardRows = cards.map { card ->
            buildList {
                add(card.term)
                add(card.definition)
                addAll(card.definitionVariants)
                repeat(maxVariantCount - card.definitionVariants.size) {
                    add("")
                }
            }
        }

        return cardRows
    }

    private const val MAX_FILE_NAME_LENGTH = 80
    private const val DEFAULT_FILE_NAME = "memorizy_export"
}