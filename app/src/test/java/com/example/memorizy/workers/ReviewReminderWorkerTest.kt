package com.example.memorizy.workers

import com.example.memorizy.data.source.local.room.entity.Card
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

@Suppress("NonAsciiCharacters")
class ReviewReminderWorkerTest {

    private val NOW = 1000000000000L    // Фиксированное текущее время
    private val COOLDOWN = ReviewReminderWorker.COOLDOWN_MS
    private val MAX_OVERDUE = ReviewReminderWorker.MAX_OVERDUE_MS

    private fun card(nextReviewDate: Long): Card {
        return Card(
            id = 0,
            remoteId = null,
            setId = 1,
            term = "term",
            definition = "def",
            level = 1,
            nextReviewDate = nextReviewDate,
            createdAt = 0,
            isDeleted = false,
            isEdited = false
        )
    }

    @Test
    fun `пустой список карточек - не показывать уведомление`() {
        val allCards = emptyList<Card>()
        val dueCards = emptyList<Card>()
        
        val result = ReviewReminderWorker.shouldShowNotification(
            allCards = allCards,
            dueCards = dueCards,
            lastNotificationTime = 0L,
            now = NOW
        )
        
        assertFalse(result)
    }

    @Test
    fun `нет карточек к повторению — не показывать уведомление`() {
        val allCards = listOf(card(NOW + 10000), card(NOW + 20000))
        val dueCards = emptyList<Card>()
        
        val result = ReviewReminderWorker.shouldShowNotification(
            allCards = allCards,
            dueCards = dueCards,
            lastNotificationTime = 0L,
            now = NOW
        )
        
        assertFalse(result)
    }

    @Test
    fun `cooldown не прошёл — не показывать уведомление`() {
        val allCards = listOf(card(NOW - 10000))
        val dueCards = allCards
        
        val result = ReviewReminderWorker.shouldShowNotification(
            allCards = allCards,
            dueCards = dueCards,
            lastNotificationTime = NOW - COOLDOWN + 1000,   // Меньше 6 часов назад
            now = NOW
        )
        
        assertFalse(result)
    }

    @Test
    fun `карточек к повторению меньше порога и нет overdue — не показывать`() {
        val allCards = listOf(
            card(NOW - 1000),
            card(NOW + 10000),
            card(NOW + 20000),
            card(NOW + 30000)
        )
        val dueCards = listOf(allCards[0])
        
        val result = ReviewReminderWorker.shouldShowNotification(
            allCards = allCards,
            dueCards = dueCards,
            lastNotificationTime = 0L,
            now = NOW
        )
        
        assertFalse(result)
    }

    @Test
    fun `карточек к повторению больше порога — показать уведомление`() {
        val allCards = listOf(
            card(NOW - 1000),
            card(NOW - 2000),
            card(NOW + 20000),
            card(NOW + 30000)
        )
        val dueCards = listOf(allCards[0], allCards[1])
        
        val result = ReviewReminderWorker.shouldShowNotification(
            allCards = allCards,
            dueCards = dueCards,
            lastNotificationTime = 0L,
            now = NOW
        )
        
        assertTrue(result)
    }

    @Test
    fun `карточек меньше порога но есть overdue больше 24ч — показать уведомление`() {
        val allCards = (1..10).map { card(NOW + 10000) }.toMutableList()
        val overdueCard = card(NOW - MAX_OVERDUE - 1000)
        allCards[0] = overdueCard
        
        val dueCards = listOf(overdueCard)
        
        val result = ReviewReminderWorker.shouldShowNotification(
            allCards = allCards,
            dueCards = dueCards,
            lastNotificationTime = 0L,
            now = NOW
        )
        
        assertTrue(result)
    }

    @Test
    fun `порог не превышает MAX_THRESHOLD при большом количестве карточек`() {
        val allCards = (1..100).map { card(NOW + 10000) }.toMutableList()
        val dueCards = mutableListOf<Card>()
        
        for (i in 0 until 10) {
            val dueCard = card(NOW - 1000)
            allCards[i] = dueCard
            dueCards.add(dueCard)
        }
        
        val result = ReviewReminderWorker.shouldShowNotification(
            allCards = allCards,
            dueCards = dueCards,
            lastNotificationTime = 0L,
            now = NOW
        )
        
        assertTrue(result)
    }
}
