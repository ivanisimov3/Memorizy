@file:Suppress("NonAsciiCharacters")

package com.example.memorizy.data.repository

import android.util.Log
import com.example.memorizy.data.source.local.room.dao.CardDao
import com.example.memorizy.data.source.local.room.dao.StudySetDao
import com.example.memorizy.data.source.local.room.entity.Card
import com.example.memorizy.data.source.local.room.entity.StudySet
import com.example.memorizy.data.source.network.MemorizyApiService
import com.example.memorizy.data.source.network.dto.CardDto
import com.example.memorizy.data.sync.SyncAuthException
import com.example.memorizy.data.sync.SyncRetryException
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.After
import org.junit.Assert.fail
import org.junit.Before
import org.junit.Test
import retrofit2.HttpException
import retrofit2.Response
import java.io.IOException

class SyncRepositoryTest {

    private val studySetDao = mockk<StudySetDao>()
    private val cardDao = mockk<CardDao>()
    private val api = mockk<MemorizyApiService>()
    private val settingsRepository = mockk<SettingsRepository>()

    private lateinit var studySetRepository: StudySetRepositoryImpl
    private lateinit var cardRepository: CardRepositoryImpl

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.w(any(), any(), any<Throwable>()) } returns 0
        every { Log.e(any(), any(), any<Throwable>()) } returns 0

        studySetRepository = StudySetRepositoryImpl(studySetDao, api, settingsRepository)
        cardRepository = CardRepositoryImpl(cardDao, studySetDao, api, settingsRepository)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `StudySetRepositoryImpl syncLocalChanges не падает если токена нет`() = runTest {
        every { settingsRepository.token } returns flowOf(null)

        studySetRepository.syncLocalChanges()

        coVerify(exactly = 0) { studySetDao.getUnsyncedSets() }
        coVerify(exactly = 0) { api.createSet(any(), any()) }
    }

    @Test
    fun `StudySetRepositoryImpl syncLocalChanges throws SyncAuthException on 401`() = runTest {
        val localSet = studySet(id = 1, remoteId = null)

        every { settingsRepository.token } returns flowOf("expired-token")
        coEvery { studySetDao.getUnsyncedSets() } returns listOf(localSet)
        coEvery { studySetDao.getEditedSets() } returns emptyList()
        coEvery { studySetDao.getSetsToDelete() } returns emptyList()
        coEvery { api.createSet("Bearer expired-token", any()) } throws httpException(401)

        try {
            studySetRepository.syncLocalChanges()
            fail("Ожидалось исключение SyncAuthException")
        } catch (_: SyncAuthException) {
            Unit
        }

        coVerify(exactly = 0) { studySetDao.updateSet(any()) }
    }

    @Test
    fun `CardRepositoryImpl syncLocalChanges throws SyncRetryException on network failure`() = runTest {
        val parentSet = studySet(id = 1, remoteId = 100)
        val localCard = card(id = 1, setId = 1, remoteId = null)

        every { settingsRepository.token } returns flowOf("token")
        coEvery { cardDao.getUnsyncedCards() } returns listOf(localCard)
        coEvery { cardDao.getEditedCards() } returns emptyList()
        coEvery { cardDao.getCardsToDelete() } returns emptyList()
        coEvery { studySetDao.getSetByIdSimple(1) } returns parentSet
        coEvery { api.createCard("Bearer token", any()) } throws IOException("network error")

        try {
            cardRepository.syncLocalChanges()
            fail("Ожидалось исключение SyncRetryException")
        } catch (_: SyncRetryException) {
            Unit
        }

        coVerify(exactly = 0) { cardDao.deleteCard(any()) }
    }

    @Test
    fun `CardRepositoryImpl при частичном успехе не удаляет локальные данные и помечает sync на retry`() = runTest {
        val parentSet = studySet(id = 1, remoteId = 100)
        val firstCard = card(id = 1, setId = 1, remoteId = null, term = "Первая")
        val secondCard = card(id = 2, setId = 1, remoteId = null, term = "Вторая")

        every { settingsRepository.token } returns flowOf("token")
        coEvery { cardDao.getUnsyncedCards() } returns listOf(firstCard, secondCard)
        coEvery { cardDao.getEditedCards() } returns emptyList()
        coEvery { cardDao.getCardsToDelete() } returns emptyList()
        coEvery { studySetDao.getSetByIdSimple(1) } returns parentSet
        coEvery {
            api.createCard(
                "Bearer token",
                match {
                    it.term == "Первая" &&
                        it.reviewCount == firstCard.reviewCount &&
                        it.mistakeCount == firstCard.mistakeCount &&
                        it.recentAnswerHistory == firstCard.recentAnswerHistory
                }
            )
        } returns CardDto(
            id = 501,
            term = "Первая",
            definition = firstCard.definition,
            definitionVariants = firstCard.definitionVariants,
            studySetId = 100,
            createdAt = firstCard.createdAt,
            level = firstCard.level,
            nextReviewDate = firstCard.nextReviewDate,
            reviewCount = firstCard.reviewCount,
            mistakeCount = firstCard.mistakeCount,
            recentAnswerHistory = firstCard.recentAnswerHistory
        )
        coEvery {
            api.createCard("Bearer token", match { it.term == "Вторая" })
        } throws IOException("network error")

        try {
            cardRepository.syncLocalChanges()
            fail("Ожидалось исключение SyncRetryException")
        } catch (_: SyncRetryException) {
            Unit
        }

        coVerify {
            cardDao.updateCard(match { it.id == firstCard.id && it.remoteId == 501L })
        }
        coVerify(exactly = 0) { cardDao.deleteCard(any()) }
    }

    private fun httpException(code: Int): HttpException {
        val response = Response.error<Any>(
            code,
            "error".toResponseBody("text/plain".toMediaType())
        )
        return HttpException(response)
    }

    private fun studySet(
        id: Long,
        remoteId: Long? = null
    ) = StudySet(
        id = id,
        name = "Набор $id",
        description = "Описание",
        iconId = 1,
        remoteId = remoteId
    )

    private fun card(
        id: Long,
        setId: Long,
        remoteId: Long? = null,
        term: String = "Термин $id"
    ) = Card(
        id = id,
        setId = setId,
        term = term,
        definition = "Определение $id",
        remoteId = remoteId,
        level = 1,
        nextReviewDate = 1_000_000L,
        reviewCount = 4,
        mistakeCount = 1,
        recentAnswerHistory = "1101"
    )
}