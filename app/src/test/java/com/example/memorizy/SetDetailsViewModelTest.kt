package com.example.memorizy

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.example.memorizy.data.repository.cardrepository.CardRepository
import com.example.memorizy.data.source.local.card.Card
import com.example.memorizy.data.source.local.studyset.StudySet
import com.example.memorizy.data.repository.studysetrepository.StudySetRepository
import com.example.memorizy.ui.navigation.Routes
import com.example.memorizy.ui.setdetails.SetDetailsViewModel
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class SetDetailsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var studySetRepository: StudySetRepository
    @MockK
    private lateinit var cardRepository: CardRepository
    @MockK
    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: SetDetailsViewModel

    @Before
    fun setUp() {
        mockkStatic("androidx.navigation.SavedStateHandleKt")
        val mockRoute = Routes.SetDetails(setId = 55)
        every { savedStateHandle.toRoute<Routes.SetDetails>() } returns mockRoute
    }

    @Test
    fun `uiState loads set and cards successfully`() = runTest { // Убрал UnconfinedTestDispatcher
        // Arrange
        val fakeSet = StudySet(id = 55, name = "Test Set", description = "Desc", iconId = 1)
        val fakeCards = listOf(Card(id = 1, setId = 55, term = "A", definition = "B"))

        // Учим репозитории возвращать Flow с данными
        every { studySetRepository.getSet(55) } returns flowOf(fakeSet)
        every { cardRepository.getAllCardsForSet(55) } returns flowOf(fakeCards)

        viewModel = SetDetailsViewModel(savedStateHandle, studySetRepository, cardRepository)

        // Act (Собираем состояние)
        val states = mutableListOf<com.example.memorizy.ui.setdetails.SetDetailsState>()

        // Запускаем сборщика в фоновой корутине
        val job = launch {
            viewModel.uiState.toList(states)
        }

        // ⚡️ ВАЖНО: "Прокручиваем" время, чтобы combine успел отработать
        advanceUntilIdle()

        // Assert
        // Теперь в списке states должно быть 2 состояния:
        // 1. Начальное (isLoading = true)
        // 2. Загруженное (isLoading = false, данные есть)

        val lastState = states.last()

        assertFalse(lastState.isLoading) // <-- Теперь здесь будет false, и тест пройдет
        assertEquals("Test Set", lastState.studySet?.name)
        assertEquals(1, lastState.cards.size)
        assertEquals("A", lastState.cards[0].term)

        job.cancel()
    }
}