package com.example.memorizy.domain.spacedrepetition

import com.example.memorizy.data.source.local.room.entity.Card

/**
 * Модифицированный алгоритм Лейтнера с поддержкой сжатия интервалов под дедлайн.
 *
 * Принцип работы:
 * - Каждая карточка имеет уровень (level 0..7).
 * - Правильный ответ → level + 1, интервал растёт.
 * - Неправильный ответ → level - 2, интервал сокращается.
 * - Если набор имеет дедлайн (targetDate), интервалы пропорционально сжимаются.
 */
object SpacedRepetitionScheduler {

    private const val MINUTE_MS = 60_000L
    private const val DAY_MS = 86_400_000L
    private const val MIN_INTERVAL = 30 * MINUTE_MS   // минимум 30 минут между показами
    const val MAX_LEVEL = 7

    // Базовые интервалы для каждого уровня (в миллисекундах)
    private val BASE_INTERVALS = longArrayOf(
        0,              // Level 0: показать сразу (новая карточка)
        1 * DAY_MS,     // Level 1: 1 день
        3 * DAY_MS,     // Level 2: 3 дня
        7 * DAY_MS,     // Level 3: 7 дней
        14 * DAY_MS,    // Level 4: 14 дней
        30 * DAY_MS,    // Level 5: 30 дней
        90 * DAY_MS,    // Level 6: 90 дней
        180 * DAY_MS    // Level 7: 180 дней (потолок, повторяется)
    )

    /**
     * Вычисляет интервал до следующего показа.
     *
     * @param level текущий уровень карточки
     * @param now текущее время в миллисекундах
     * @param targetDate дедлайн набора (null = стандартный режим)
     * @return интервал в миллисекундах
     */
    fun calculateInterval(level: Int, now: Long, targetDate: Long?): Long {
        val clampedLevel = level.coerceIn(0, MAX_LEVEL)
        val baseInterval = BASE_INTERVALS[clampedLevel]

        // Стандартный режим — базовые интервалы
        if (targetDate == null) return maxOf(baseInterval, MIN_INTERVAL)

        val remainingTime = targetDate - now
        // Дедлайн прошёл — минимальный интервал
        if (remainingTime <= 0) return MIN_INTERVAL

        // Пропорциональное сжатие: доля текущего интервала от суммы оставшихся
        val remainingSum = BASE_INTERVALS.drop(clampedLevel).sum().toDouble()
        if (remainingSum == 0.0) return MIN_INTERVAL

        val compressed = (remainingTime * (baseInterval.toDouble() / remainingSum)).toLong()
        return compressed.coerceAtLeast(MIN_INTERVAL)
    }

    /**
     * Обрабатывает ответ пользователя и возвращает обновлённую карточку.
     *
     * @param card текущая карточка
     * @param isCorrect правильный ли ответ
     * @param now текущее время в миллисекундах
     * @param targetDate дедлайн набора (null = стандартный режим)
     * @return копия карточки с обновлёнными level и nextReviewDate
     */
    fun processAnswer(card: Card, isCorrect: Boolean, now: Long, targetDate: Long?): Card {
        return if (isCorrect) {
            val newLevel = (card.level + 1).coerceAtMost(MAX_LEVEL)
            val interval = calculateInterval(newLevel, now, targetDate)
            card.copy(level = newLevel, nextReviewDate = now + interval)
        } else {
            // Откат на 2 уровня
            val newLevel = (card.level - 2).coerceAtLeast(0)
            val halfInterval = calculateInterval(newLevel, now, targetDate) / 2
            card.copy(
                level = newLevel,
                nextReviewDate = now + halfInterval.coerceAtLeast(MIN_INTERVAL)
            )
        }
    }

    /**
     * Возвращает карточки, которые пора повторять (nextReviewDate <= now).
     * Сортировка: сначала слабые (низкий level), затем ждавшие дольше.
     */
    fun getCardsForReview(allCards: List<Card>, now: Long): List<Card> {
        return allCards
            .filter { it.nextReviewDate <= now }
            .sortedWith(
                compareBy<Card> { it.level }
                    .thenBy { it.nextReviewDate }
            )
    }

    /**
     * Перемешивает карточки внутри одного уровня, сохраняя порядок уровней.
     * level 0 карточки перемешаны между собой, level 1 — между собой и т.д.
     */
    fun shuffleWithinLevels(cards: List<Card>): List<Card> {
        return cards
            .groupBy { it.level }
            .toSortedMap()
            .flatMap { (_, cardsInLevel) -> cardsInLevel.shuffled() }
    }

    /**
     * Вычисляет время до ближайшего повторения (для информирования пользователя).
     *
     * @return миллисекунды до ближайшего повторения, или null если карточек нет
     */
    fun getTimeUntilNextReview(allCards: List<Card>, now: Long): Long? {
        val nextReviewDate = allCards
            .map { it.nextReviewDate }
            .minOrNull() ?: return null

        return (nextReviewDate - now).coerceAtLeast(0)
    }
}
