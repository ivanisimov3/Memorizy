@file:Suppress("NonAsciiCharacters")

package com.example.memorizy.domain.cardrisk

import com.example.memorizy.domain.cardrisk.CardRiskAnalyzer.CardRisk
import org.junit.Assert.assertEquals
import org.junit.Test

class CardRiskAnalyzerTest {

    @Test
    fun `новая карточка без истории имеет низкое знание`() {
        assertEquals(CardRisk.HIGH, CardRiskAnalyzer.calculateRisk(level = 0, recentAnswerHistory = ""))
    }

    @Test
    fun `пять правильных ответов и максимальный уровень дают высокое знание`() {
        assertEquals(CardRisk.LOW, CardRiskAnalyzer.calculateRisk(level = 7, recentAnswerHistory = "11111"))
    }

    @Test
    fun `уровень 5 и пять правильных ответов дают среднее знание при пороге 90`() {
        assertEquals(CardRisk.MEDIUM, CardRiskAnalyzer.calculateRisk(level = 5, recentAnswerHistory = "11111"))
    }

    @Test
    fun `меньше пяти ответов искусственно занижают стабильность`() {
        val score = CardRiskAnalyzer.calculateKnowledgeScore(level = 5, recentAnswerHistory = "111")

        assertEquals(0.645f, score, 0.001f)
        assertEquals(CardRisk.MEDIUM, CardRiskAnalyzer.calculateRisk(level = 5, recentAnswerHistory = "111"))
    }

    @Test
    fun `последние пять ответов важнее старой истории`() {
        val score = CardRiskAnalyzer.calculateKnowledgeScore(level = 7, recentAnswerHistory = "0000011111")

        assertEquals(1.0f, score, 0.001f)
        assertEquals(CardRisk.LOW, CardRiskAnalyzer.calculateRisk(level = 7, recentAnswerHistory = "0000011111"))
    }

    @Test
    fun `ошибка после максимального уровня переводит знание в низкую категорию`() {
        assertEquals(CardRisk.HIGH, CardRiskAnalyzer.calculateRisk(level = 0, recentAnswerHistory = "11110"))
    }

    @Test
    fun `некорректный уровень ограничивается допустимым диапазоном`() {
        assertEquals(CardRisk.HIGH, CardRiskAnalyzer.calculateRisk(level = -1, recentAnswerHistory = ""))
        assertEquals(CardRisk.LOW, CardRiskAnalyzer.calculateRisk(level = 999, recentAnswerHistory = "11111"))
    }
}