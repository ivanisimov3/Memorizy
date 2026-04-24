@file:Suppress("NonAsciiCharacters")

package com.example.memorizy.workers

import android.content.Context
import android.util.Log
import androidx.work.ListenableWorker
import androidx.work.WorkerParameters
import com.example.memorizy.data.sync.SyncAuthException
import com.example.memorizy.data.sync.SyncCoordinator
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
    private val syncCoordinator = mockk<SyncCoordinator>()

    private lateinit var worker: SyncWorker

    @Before
    fun setUp() {
        mockkStatic(Log::class)
        every { Log.w(any(), any(), any<Throwable>()) } returns 0
        every { Log.e(any(), any(), any<Throwable>()) } returns 0

        worker = SyncWorker(context, params, syncCoordinator)
    }

    @After
    fun tearDown() {
        unmockkStatic(Log::class)
    }

    @Test
    fun `SyncWorker returns success when all sync steps complete`() = runTest {
        coEvery { syncCoordinator.syncAll() } returns Unit

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.success()::class, result::class)
        coVerifyOrder {
            syncCoordinator.syncAll()
        }
    }

    @Test
    fun `SyncWorker returns failure on SyncAuthException`() = runTest {
        coEvery { syncCoordinator.syncAll() } throws SyncAuthException("auth")

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.failure()::class, result::class)
    }

    @Test
    fun `SyncWorker returns retry on SyncRetryException`() = runTest {
        coEvery { syncCoordinator.syncAll() } throws SyncRetryException("retry")

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry()::class, result::class)
    }

    @Test
    fun `SyncWorker returns retry on unexpected exception`() = runTest {
        coEvery { syncCoordinator.syncAll() } throws IllegalStateException("boom")

        val result = worker.doWork()

        assertEquals(ListenableWorker.Result.retry()::class, result::class)
    }
}