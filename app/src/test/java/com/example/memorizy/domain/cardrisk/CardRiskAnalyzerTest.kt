@file:Suppress("NonAsciiCharacters")

package com.example.memorizy.domain.cardrisk

import com.example.memorizy.domain.cardrisk.CardRiskAnalyzer.CardRisk
import org.junit.Assert.assertEquals
import org.junit.Test

class CardRiskAnalyzerTest {

    @Test
    fun `уровни 0 и 2 относятся к высокому риску`() {
        assertEquals(CardRisk.HIGH, CardRiskAnalyzer.calculateRisk(0))
        assertEquals(CardRisk.HIGH, CardRiskAnalyzer.calculateRisk(2))
    }

    @Test
    fun `уровни 3 и 4 относятся к среднему риску`() {
        assertEquals(CardRisk.MEDIUM, CardRiskAnalyzer.calculateRisk(3))
        assertEquals(CardRisk.MEDIUM, CardRiskAnalyzer.calculateRisk(4))
    }

    @Test
    fun `уровни 5 и 7 относятся к низкому риску`() {
        assertEquals(CardRisk.LOW, CardRiskAnalyzer.calculateRisk(5))
        assertEquals(CardRisk.LOW, CardRiskAnalyzer.calculateRisk(7))
    }
}