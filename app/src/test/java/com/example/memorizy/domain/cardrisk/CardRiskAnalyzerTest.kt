@file:Suppress("NonAsciiCharacters")

package com.example.memorizy.domain.cardrisk

import com.example.memorizy.domain.cardrisk.CardRiskAnalyzer.CardRisk
import org.junit.Assert.assertEquals
import org.junit.Test

class CardRiskAnalyzerTest {

    @Test
    fun `границы диапазонов риска обрабатываются корректно`() {
        assertEquals(CardRisk.HIGH, CardRiskAnalyzer.calculateRisk(2))
        assertEquals(CardRisk.MEDIUM, CardRiskAnalyzer.calculateRisk(3))
        assertEquals(CardRisk.MEDIUM, CardRiskAnalyzer.calculateRisk(5))
        assertEquals(CardRisk.LOW, CardRiskAnalyzer.calculateRisk(6))
    }

    @Test
    fun `уровни 0 и 2 относятся к высокому риску`() {
        assertEquals(CardRisk.HIGH, CardRiskAnalyzer.calculateRisk(0))
        assertEquals(CardRisk.HIGH, CardRiskAnalyzer.calculateRisk(2))
    }

    @Test
    fun `уровни 3 и 5 относятся к среднему риску`() {
        assertEquals(CardRisk.MEDIUM, CardRiskAnalyzer.calculateRisk(3))
        assertEquals(CardRisk.MEDIUM, CardRiskAnalyzer.calculateRisk(5))
    }

    @Test
    fun `уровни 6 и 7 относятся к низкому риску`() {
        assertEquals(CardRisk.LOW, CardRiskAnalyzer.calculateRisk(6))
        assertEquals(CardRisk.LOW, CardRiskAnalyzer.calculateRisk(7))
    }

    @Test
    fun `все уровни от 0 до 7 попадают в ожидаемые категории`() {
        val expected = listOf(
            CardRisk.HIGH,
            CardRisk.HIGH,
            CardRisk.HIGH,
            CardRisk.MEDIUM,
            CardRisk.MEDIUM,
            CardRisk.MEDIUM,
            CardRisk.LOW,
            CardRisk.LOW
        )

        val actual = (0..7).map { level ->
            CardRiskAnalyzer.calculateRisk(level)
        }

        assertEquals(expected, actual)
    }

    @Test
    fun `некорректные уровни сейчас трактуются как низкий риск`() {
        assertEquals(CardRisk.LOW, CardRiskAnalyzer.calculateRisk(-1))
        assertEquals(CardRisk.LOW, CardRiskAnalyzer.calculateRisk(999))
    }
}