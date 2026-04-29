package com.example.memorizy.domain.exporter.usecase

import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.domain.data_exchange.exporter.usecase.CardsCsvFormatter
import org.junit.Assert.assertEquals
import org.junit.Test

class CardsCsvFormatterTest {

    @Test
    fun `buildRows adds import-compatible header and pads additional definitions`() {
        val rows = CardsCsvFormatter.buildRows(
            listOf(
                Card(
                    setId = 1,
                    term = "Клетка",
                    definition = "Базовая единица живого организма",
                    definitionVariants = listOf(
                        "Структурная единица организма",
                        "Элементарная живая система"
                    )
                ),
                Card(
                    setId = 1,
                    term = "Атом",
                    definition = "Наименьшая частица химического элемента"
                )
            )
        )

        assertEquals(
            listOf(
                "Атом",
                "Наименьшая частица химического элемента",
                "",
                ""
            ),
            rows[1]
        )
    }

    @Test
    fun `buildFileName removes characters that cannot be used in file names`() {
        assertEquals(
            "Мой_ набор_ биология.csv",
            CardsCsvFormatter.buildFileName("""Мой/ набор: биология""")
        )
    }
}