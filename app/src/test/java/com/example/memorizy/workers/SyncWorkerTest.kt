@file:Suppress("NonAsciiCharacters")

package com.example.memorizy.workers

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.memorizy.data.repository.CardRepository
import com.example.memorizy.data.repository.StudySetRepository
import com.example.memorizy.data.sync.SyncAuthException
import com.example.memorizy.data.sync.SyncRetryException
import io.mockk.coEvery
import io.mockk.coVerifyOrder
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.unmockkStatic
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

class SyncWorkerTest {

    private val context = mockk<Context>(relaxed = true)
    private val params = mockk<WorkerParameters>(relaxed = true)
    private val studySetRepository = mockk<StudySetRepository>()
    private val cardRepository = mockk<CardRepository>()

    private lateinit var worker: SyncWorker

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.w(any(), any(), any<Throwable>()) } returns 0
        every { Log.e(any(), any(), any<Throwable>()) } returns 0

        worker = SyncWorker(context, params, studySetRepository, cardRepository)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `SyncWorker returns success when all sync steps complete`() = runTest {
        coEvery { studySetRepository.syncLocalChanges() } returns Unit
        coEvery { studySetRepository.fetchRemoteChanges() } returns Unit
        coEvery { cardRepository.syncLocalChanges() } returns Unit
        coEvery { cardRepository.fetchRemoteChanges() } returns Unit

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success()::class, result::class)
        coVerifyOrder {
            studySetRepository.syncLocalChanges()
            studySetRepository.fetchRemoteChanges()
            cardRepository.syncLocalChanges()
            cardRepository.fetchRemoteChanges()
        }
    }

    @Test
    fun `SyncWorker returns failure on SyncAuthException`() = runTest {
        coEvery { studySetRepository.syncLocalChanges() } throws SyncAuthException("auth")

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.failure()::class, result::class)
    }

    @Test
    fun `SyncWorker returns retry on SyncRetryException`() = runTest {
        coEvery { studySetRepository.syncLocalChanges() } throws SyncRetryException("retry")

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry()::class, result::class)
    }

    @Test
    fun `SyncWorker returns retry on unexpected exception`() = runTest {
        coEvery { studySetRepository.syncLocalChanges() } throws IllegalStateException("boom")

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry()::class, result::class)
    }
}