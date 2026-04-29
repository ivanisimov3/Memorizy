package com.example.memorizy.domain.spacedrepetition

import com.example.memorizy.data.source.local.room.entity.Card

/**
 * Модифицированный алгоритм Лейтнера с поддержкой сжатия интервалов под дедлайн.
 *
 * Принцип работы:
 * - Каждая карточка имеет уровень (level 0..7)
 * - Правильный ответ → level + 1
 * - Неправильный ответ → сброс к level 0
 * - Если набор имеет дедлайн (targetDate), интервалы пропорционально сжимаются
 */

object SpacedRepetitionScheduler {

    private const val MINUTE_MS = 60_000L
    private const val DAY_MS = 86_400_000L
    private const val MIN_INTERVAL = 30 * MINUTE_MS
    private const val DEADLINE_BUFFER = DAY_MS
    const val MAX_LEVEL = 7

    // Базовые интервалы для каждого уровня
    private val BASE_INTERVALS = longArrayOf(
        0,
        1 * DAY_MS,
        3 * DAY_MS,
        7 * DAY_MS,
        14 * DAY_MS,
        30 * DAY_MS,
        90 * DAY_MS,
        180 * DAY_MS
    )

    // Вычисляем интервал до следующего показа
    fun calculateInterval(level: Int, now: Long, targetDate: Long?): Long {
        val clampedLevel = level.coerceIn(0, MAX_LEVEL)
        val baseInterval = BASE_INTERVALS[clampedLevel]

        // Стандартный режим
        if (targetDate == null) return maxOf(baseInterval, MIN_INTERVAL)

        val remainingTime = targetDate - now - DEADLINE_BUFFER
        if (remainingTime <= 0) return MIN_INTERVAL

        // Сумма всех дней от текущего уровня до максимального при стандартном режиме
        val remainingSum = BASE_INTERVALS.drop(clampedLevel).sum().toDouble()
        if (remainingSum == 0.0) return MIN_INTERVAL

        // Высчитываем срок для следующей сессии заучивания
        val compressed = (remainingTime * (baseInterval.toDouble() / remainingSum)).toLong()
        return compressed.coerceIn(MIN_INTERVAL, maxOf(baseInterval, MIN_INTERVAL))
    }

    // Обрабатываем ответ пользователя и возвращаем обновлённую карточку.
    fun processAnswer(card: Card, isCorrect: Boolean, now: Long, targetDate: Long?): Card {
        val reviewCount = card.reviewCount + 1
        val mistakeCount = card.mistakeCount + if (isCorrect) 0 else 1
        val recentAnswerHistory = appendRecentAnswer(card.recentAnswerHistory, isCorrect)

        return if (isCorrect) {
            val newLevel = (card.level + 1).coerceAtMost(MAX_LEVEL)
            val interval = calculateInterval(newLevel, now, targetDate)
            card.copy(
                level = newLevel,
                nextReviewDate = now + interval,
                reviewCount = reviewCount,
                mistakeCount = mistakeCount,
                recentAnswerHistory = recentAnswerHistory
            )
        } else {
            card.copy(
                level = 0,
                nextReviewDate = now + MIN_INTERVAL,
                reviewCount = reviewCount,
                mistakeCount = mistakeCount,
                recentAnswerHistory = recentAnswerHistory
            )
        }
    }

    private fun appendRecentAnswer(history: String, isCorrect: Boolean): String {
        val answer = if (isCorrect) "1" else "0"
        return (history.filter { it == '0' || it == '1' } + answer).takeLast(5)
    }

    // Возвращаем карточки, которые пора повторять
    fun getCardsForReview(allCards: List<Card>, now: Long): List<Card> {
        return allCards
            .filter { it.nextReviewDate <= now }
            .sortedWith(    // Сортируем карточки по уровню, потом по времени повторения внутри уровня
                compareBy<Card> { it.level }
                    .thenBy { it.nextReviewDate }
            )
    }

    // Перемешиваем карточки внутри одного уровня
    fun shuffleWithinLevels(cards: List<Card>): List<Card> {
        return cards
            .groupBy { it.level }
            .toSortedMap()
            .flatMap { (_, cardsInLevel) -> cardsInLevel.shuffled() }   // _ - игнорируем ключ сортировки (уровень)
    }

    // Вычисляем время до ближайшего повторения
    fun getTimeUntilNextReview(allCards: List<Card>, now: Long): Long? {
        val nextReviewDate = allCards
            .map { it.nextReviewDate }  // Вытаскиваем из каждой сущности Card параметр nextReviewDate
            .minOrNull() ?: return null

        return (nextReviewDate - now).coerceAtLeast(0)
    }
}