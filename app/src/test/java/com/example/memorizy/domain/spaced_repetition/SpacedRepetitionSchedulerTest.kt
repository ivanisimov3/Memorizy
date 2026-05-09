@file:Suppress("NonAsciiCharacters")

package com.example.memorizy.domain.spaced_repetition

import com.example.memorizy.data.source.local.room.entity.Card
import org.junit.Assert.*
import org.junit.Test

class SpacedRepetitionSchedulerTest {

    @Test
    fun `правильный ответ повышает уровень на 1`() {
        val original = card(level = 3)
        val result = SpacedRepetitionScheduler.processAnswer(
            card = original, isCorrect = true, now = NOW, targetDate = null
        )
        assertEquals(4, result.level)
    }

    @Test
    fun `неправильный ответ сбрасывает уровень до 0`() {
        val original = card(level = 3)
        val result = SpacedRepetitionScheduler.processAnswer(
            card = original, isCorrect = false, now = NOW, targetDate = null
        )
        assertEquals(0, result.level)
    }

    @Test
    fun `уровень не опускается ниже 0`() {
        val original = card(level = 0)
        val result = SpacedRepetitionScheduler.processAnswer(
            card = original, isCorrect = false, now = NOW, targetDate = null
        )
        assertEquals(0, result.level)
    }

    @Test
    fun `уровень не поднимается выше MAX_LEVEL`() {
        val original = card(level = SpacedRepetitionScheduler.MAX_LEVEL)
        val result = SpacedRepetitionScheduler.processAnswer(
            card = original, isCorrect = true, now = NOW, targetDate = null
        )
        assertEquals(SpacedRepetitionScheduler.MAX_LEVEL, result.level)
    }

    @Test
    fun `правильный ответ устанавливает nextReviewDate в будущее`() {
        val original = card(level = 2)
        val result = SpacedRepetitionScheduler.processAnswer(
            card = original, isCorrect = true, now = NOW, targetDate = null
        )
        assertTrue(
            "nextReviewDate должен быть в будущем",
            result.nextReviewDate > NOW
        )
    }

    @Test
    fun `неправильный ответ устанавливает nextReviewDate в будущее`() {
        val original = card(level = 2)
        val result = SpacedRepetitionScheduler.processAnswer(
            card = original, isCorrect = false, now = NOW, targetDate = null
        )
        assertTrue(
            "nextReviewDate должен быть в будущем",
            result.nextReviewDate > NOW
        )
    }

    @Test
    fun `неправильный ответ назначает минимальный интервал`() {
        val original = card(level = 4)
        val result = SpacedRepetitionScheduler.processAnswer(
            card = original, isCorrect = false, now = NOW, targetDate = null
        )

        val actualInterval = result.nextReviewDate - NOW

        assertEquals(MIN_INTERVAL, actualInterval)
    }

    @Test
    fun `ответ обновляет счетчики и историю последних ответов`() {
        val original = card(
            level = 2,
            reviewCount = 8,
            mistakeCount = 2,
            recentAnswerHistory = "1010"
        )

        val result = SpacedRepetitionScheduler.processAnswer(
            card = original, isCorrect = true, now = NOW, targetDate = null
        )

        assertEquals(9, result.reviewCount)
        assertEquals(2, result.mistakeCount)
        assertEquals("10101", result.recentAnswerHistory)
    }

    @Test
    fun `история последних ответов хранит только пять последних значений`() {
        val original = card(
            level = 2,
            recentAnswerHistory = "01010"
        )

        val result = SpacedRepetitionScheduler.processAnswer(
            card = original, isCorrect = true, now = NOW, targetDate = null
        )

        assertEquals("10101", result.recentAnswerHistory)
    }

    @Test
    fun `интервал уровня 0 без дедлайна равен MIN_INTERVAL`() { // BASE_INTERVALS[0] = 0, но coerceAtLeast(MIN_INTERVAL) = 30 минут
        val interval = SpacedRepetitionScheduler.calculateInterval(
            level = 0, now = NOW, targetDate = null
        )
        assertEquals(MIN_INTERVAL, interval)
    }

    @Test
    fun `интервал уровня 1 без дедлайна равен 1 дню`() {
        val interval = SpacedRepetitionScheduler.calculateInterval(
            level = 1, now = NOW, targetDate = null
        )
        assertEquals(1 * DAY_MS, interval)
    }

    @Test
    fun `интервал уровня 4 без дедлайна равен 14 дней`() {
        val interval = SpacedRepetitionScheduler.calculateInterval(
            level = 4, now = NOW, targetDate = null
        )
        assertEquals(14 * DAY_MS, interval)
    }

    @Test
    fun `интервал уровня 7 без дедлайна равен 180 дням`() {
        val interval = SpacedRepetitionScheduler.calculateInterval(
            level = 7, now = NOW, targetDate = null
        )
        assertEquals(180 * DAY_MS, interval)
    }

    @Test
    fun `интервал сжимается при наличии дедлайна`() {
        val farDeadline = NOW + 365 * DAY_MS
        val closeDeadline = NOW + 14 * DAY_MS

        val intervalFar = SpacedRepetitionScheduler.calculateInterval(
            level = 3, now = NOW, targetDate = farDeadline
        )
        val intervalClose = SpacedRepetitionScheduler.calculateInterval(
            level = 3, now = NOW, targetDate = closeDeadline
        )

        assertTrue(
            "Интервал при близком дедлайне ($intervalClose) " +
                "должен быть меньше, чем при далёком ($intervalFar)",
            intervalClose < intervalFar
        )
    }

    @Test
    fun `дальний дедлайн не увеличивает интервал выше базового`() {
        val farDeadline = NOW + 365 * DAY_MS
        val interval = SpacedRepetitionScheduler.calculateInterval(
            level = 3, now = NOW, targetDate = farDeadline
        )

        assertEquals(7 * DAY_MS, interval)
    }

    @Test
    fun `дедлайн учитывает буфер в 1 день`() {
        val deadline = NOW + 1 * DAY_MS
        val interval = SpacedRepetitionScheduler.calculateInterval(
            level = 3, now = NOW, targetDate = deadline
        )

        assertEquals(MIN_INTERVAL, interval)
    }

    @Test
    fun `просроченный дедлайн возвращает MIN_INTERVAL`() {
        val pastDeadline = NOW - 2 * DAY_MS
        val interval = SpacedRepetitionScheduler.calculateInterval(
            level = 3, now = NOW, targetDate = pastDeadline
        )
        assertEquals(MIN_INTERVAL, interval)
    }

    @Test
    fun `интервал с дедлайном не меньше MIN_INTERVAL`() {
        val veryCloseDeadline = NOW + 1 * MINUTE_MS
        val interval = SpacedRepetitionScheduler.calculateInterval(
            level = 5, now = NOW, targetDate = veryCloseDeadline
        )
        assertTrue(
            "Интервал ($interval) не должен быть меньше MIN_INTERVAL ($MIN_INTERVAL)",
            interval >= MIN_INTERVAL
        )
    }

    @Test
    fun `getCardsForReview возвращает только просроченные карточки`() {
        val cards = listOf(
            card(nextReviewDate = NOW - 1000),
            card(nextReviewDate = NOW),
            card(nextReviewDate = NOW + 1000)
        )

        val reviewCards = SpacedRepetitionScheduler.getCardsForReview(cards, NOW)
        assertEquals(2, reviewCards.size)
    }

    @Test
    fun `getCardsForReview сортирует по уровню потом по дате`() {
        val cardLevel3 = Card(id = 1, setId = 1, term = "a", definition = "a",
            level = 3, nextReviewDate = NOW - 1000)
        val cardLevel1Early = Card(id = 2, setId = 1, term = "b", definition = "b",
            level = 1, nextReviewDate = NOW - 5000)
        val cardLevel1Late = Card(id = 3, setId = 1, term = "c", definition = "c",
            level = 1, nextReviewDate = NOW - 1000)

        val result = SpacedRepetitionScheduler.getCardsForReview(
            listOf(cardLevel3, cardLevel1Late, cardLevel1Early), NOW
        )

        assertEquals("Первая - level 1, ранняя дата", 2L, result[0].id)
        assertEquals("Вторая - level 1, поздняя дата", 3L, result[1].id)
        assertEquals("Третья - level 3", 1L, result[2].id)
    }

    @Test
    fun `getCardsForReview возвращает пустой список если нет просроченных`() {
        val cards = listOf(
            card(nextReviewDate = NOW + DAY_MS),
            card(nextReviewDate = NOW + 2 * DAY_MS)
        )
        val result = SpacedRepetitionScheduler.getCardsForReview(cards, NOW)
        assertTrue(result.isEmpty())
    }

    @Test
    fun `getTimeUntilNextReview возвращает время до ближайшей карточки`() {
        val cards = listOf(
            card(nextReviewDate = NOW + 30 * MINUTE_MS),
            card(nextReviewDate = NOW + 27 * MINUTE_MS),
            card(nextReviewDate = NOW + 1 * DAY_MS)
        )
        val time = SpacedRepetitionScheduler.getTimeUntilNextReview(cards, NOW)
        assertEquals(27 * MINUTE_MS, time)
    }

    @Test
    fun `getTimeUntilNextReview возвращает 0 если есть просроченные`() {
        val cards = listOf(
            card(nextReviewDate = NOW - 1000),
            card(nextReviewDate = NOW + DAY_MS)
        )
        val time = SpacedRepetitionScheduler.getTimeUntilNextReview(cards, NOW)
        assertEquals(0L, time)
    }

    @Test
    fun `getTimeUntilNextReview возвращает null для пустого списка`() {
        val time = SpacedRepetitionScheduler.getTimeUntilNextReview(emptyList(), NOW)
        assertNull(time)
    }

    @Test
    fun `shuffleWithinLevels сохраняет порядок по уровням`() {
        val cardsLevel0 = (1..5).map { Card(id = it.toLong(), setId = 1,
            term = "t$it", definition = "d$it", level = 0, nextReviewDate = NOW) }
        val cardsLevel3 = (6..10).map { Card(id = it.toLong(), setId = 1,
            term = "t$it", definition = "d$it", level = 3, nextReviewDate = NOW) }

        val mixed = (cardsLevel3 + cardsLevel0) // Уровень 3 первый, потом 0

        val result = SpacedRepetitionScheduler.shuffleWithinLevels(mixed)

        val levels = result.map { it.level }
        val firstLevel3Index = levels.indexOf(3)
        val lastLevel0Index = levels.lastIndexOf(0)

        assertTrue(
            "Все карточки уровня 0 должны стоять перед уровнем 3",
            lastLevel0Index < firstLevel3Index
        )
    }

    @Test
    fun `shuffleWithinLevels сохраняет количество карточек`() {
        val cards = (1..20).map { Card(id = it.toLong(), setId = 1,
            term = "t$it", definition = "d$it", level = it % 4, nextReviewDate = NOW) }

        val result = SpacedRepetitionScheduler.shuffleWithinLevels(cards)
        assertEquals(cards.size, result.size)
    }

    @Test
    fun `повторные правильные ответы подряд последовательно повышают уровень и увеличивают интервал`() {
        val firstStep = SpacedRepetitionScheduler.processAnswer(
            card = card(level = 2),
            isCorrect = true,
            now = NOW,
            targetDate = null
        )
        val secondStep = SpacedRepetitionScheduler.processAnswer(
            card = firstStep,
            isCorrect = true,
            now = firstStep.nextReviewDate,
            targetDate = null
        )

        val firstInterval = firstStep.nextReviewDate - NOW
        val secondInterval = secondStep.nextReviewDate - firstStep.nextReviewDate

        assertEquals(3, firstStep.level)
        assertEquals(4, secondStep.level)
        assertTrue(secondInterval > firstInterval)
    }

    @Test
    fun `повторные неправильные ответы подряд оставляют карточку на минимальном уровне`() {
        val firstStep = SpacedRepetitionScheduler.processAnswer(
            card = card(level = 4),
            isCorrect = false,
            now = NOW,
            targetDate = null
        )
        val secondStep = SpacedRepetitionScheduler.processAnswer(
            card = firstStep,
            isCorrect = false,
            now = firstStep.nextReviewDate,
            targetDate = null
        )

        assertEquals(0, firstStep.level)
        assertEquals(0, secondStep.level)
        assertEquals(MIN_INTERVAL, firstStep.nextReviewDate - NOW)
        assertEquals(MIN_INTERVAL, secondStep.nextReviewDate - firstStep.nextReviewDate)
    }

    @Test
    fun `при приближении дедлайна интервалы не увеличиваются нелогично`() {
        val farDeadline = NOW + 60 * DAY_MS
        val mediumDeadline = NOW + 30 * DAY_MS
        val closeDeadline = NOW + 7 * DAY_MS

        val intervalFar = SpacedRepetitionScheduler.calculateInterval(4, NOW, farDeadline)
        val intervalMedium = SpacedRepetitionScheduler.calculateInterval(4, NOW, mediumDeadline)
        val intervalClose = SpacedRepetitionScheduler.calculateInterval(4, NOW, closeDeadline)

        assertTrue(intervalFar >= intervalMedium)
        assertTrue(intervalMedium >= intervalClose)
    }

    @Test
    fun `карточка максимального уровня при ошибке сбрасывается и получает минимальный интервал`() {
        val result = SpacedRepetitionScheduler.processAnswer(
            card = card(level = SpacedRepetitionScheduler.MAX_LEVEL),
            isCorrect = false,
            now = NOW,
            targetDate = null
        )

        assertEquals(0, result.level)
        assertEquals(MIN_INTERVAL, result.nextReviewDate - NOW)
    }

    @Test
    fun `карточка минимального уровня при серии ошибок не уходит ниже нуля`() {
        val firstStep = SpacedRepetitionScheduler.processAnswer(
            card = card(level = 0),
            isCorrect = false,
            now = NOW,
            targetDate = null
        )
        val secondStep = SpacedRepetitionScheduler.processAnswer(
            card = firstStep,
            isCorrect = false,
            now = firstStep.nextReviewDate,
            targetDate = null
        )

        assertEquals(0, firstStep.level)
        assertEquals(0, secondStep.level)
        assertEquals(MIN_INTERVAL, firstStep.nextReviewDate - NOW)
        assertEquals(MIN_INTERVAL, secondStep.nextReviewDate - firstStep.nextReviewDate)
    }

    private fun card(
        level: Int = 0,
        nextReviewDate: Long = NOW,
        reviewCount: Int = 0,
        mistakeCount: Int = 0,
        recentAnswerHistory: String = ""
    ) = Card(
        id = 1, setId = 1,
        term = "test", definition = "тест",
        level = level,
        nextReviewDate = nextReviewDate,
        reviewCount = reviewCount,
        mistakeCount = mistakeCount,
        recentAnswerHistory = recentAnswerHistory
    )

    private val DAY_MS = 86_400_000L
    private val MINUTE_MS = 60_000L
    private val MIN_INTERVAL = 30 * MINUTE_MS
    private val NOW = 1_000_000_000_000L    // Фиксированный момент времени
}