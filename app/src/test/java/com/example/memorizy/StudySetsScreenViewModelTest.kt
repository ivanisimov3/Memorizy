package com.example.memorizy

import com.example.memorizy.data.studyset.StudySet
import com.example.memorizy.data.StudySetWithCardNumber
import com.example.memorizy.domain.studyset.StudySetRepository
import com.example.memorizy.ui.studysets.StudySetsState
import com.example.memorizy.ui.studysets.StudySetsViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertFalse
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Rule
import org.junit.Test

class StudySetsScreenViewModelTest {

    // 1. Правило для "подмены" Dispatchers.Main
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    // 2. Правило для создания моков
    @get:Rule
    val mockkRule = MockKRule(this)

    // 3. Создаем "фальшивый" (Mock) репозиторий
    @MockK
    private lateinit var studySetRepository: StudySetRepository

    // 4. Наш тестируемый ViewModel
    private lateinit var viewModel: StudySetsViewModel

    // 5. Готовим "фальшивые" данные
    private val fakeSet1 = StudySet(id = 1, name = "Английский", description = "A1", iconId = 1)
    private val fakeSet2 = StudySet(id = 2, name = "Испанский", description = "A2", iconId = 2)

    private val fakeData = listOf(
        StudySetWithCardNumber(fakeSet1, 10),
        StudySetWithCardNumber(fakeSet2, 20)
    )

    @Before
    fun setUp() {
        every { studySetRepository.getAllSetsWithCardNumber() } returns flowOf(fakeData)
        // Добавили это из первого исправления:
        coEvery { studySetRepository.deleteSet(any()) } returns Unit

        viewModel = StudySetsViewModel(studySetRepository)
    }

    // --- ИСПРАВЛЕННЫЙ ТЕСТ 1 ---
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `state is loaded correctly after init`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        // Создаем список, куда будут "складываться" все состояния
        val states = mutableListOf<StudySetsState>()

        // Act
        // Запускаем "фальшивого зрителя", который собирает ВСЕ состояния в список
        val job = launch {
            viewModel.uiState.toList(states)
        }

        // "Прокручиваем" время, чтобы `stateIn` запустился и `combine` отработал
        advanceUntilIdle()

        // Assert
        // Мы ожидаем ДВА состояния в списке:
        // 1. initialValue (isLoading = true)
        // 2. Новое состояние от `combine` (isLoading = false)
        assertEquals(2, states.size)

        // Берем ПОСЛЕДНЕЕ состояние
        val currentState = states.last()

        assertFalse(currentState.isLoading) // <-- Теперь должно быть false
        assertEquals(2, currentState.studySets.size)
        assertEquals("Английский", currentState.studySets[0].studySet.name)

        // Очищаем корутину-зрителя
        job.cancel()
    }

    // --- ИСПРАВЛЕННЫЙ ТЕСТ 2 ---
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `search query filters the list correctly`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        val states = mutableListOf<StudySetsState>()

        // Act
        // Запускаем "зрителя"
        val job = launch {
            viewModel.uiState.toList(states)
        }

        // "Прокручиваем" (чтобы получить 2 начальных состояния)
        advanceUntilIdle()

        // Теперь совершаем действие - поиск
        viewModel.onSearchQueryChanged("Испан")

        // "Прокручиваем" еще раз, чтобы `combine` отработал на новый `query`
        advanceUntilIdle()

        // Assert
        // Мы ожидаем ТРИ состояния в списке:
        // 1. initialValue (loading=true, sets=0)
        // 2. after init (loading=false, sets=2)
        // 3. after search (loading=false, sets=1)
        assertEquals(3, states.size)

        // Берем самое ПОСЛЕДНЕЕ
        val currentState = states.last()

        assertFalse(currentState.isLoading)
        assertEquals(1, currentState.studySets.size)
        assertEquals("Испанский", currentState.studySets[0].studySet.name)
        assertEquals("Испан", currentState.searchQuery)

        job.cancel()
    }

    // --- ТЕСТ 3 (он должен быть "зеленым", просто добавьте `advanceUntilIdle`) ---
    @OptIn(ExperimentalCoroutinesApi::class)
    @Test
    fun `deleteSet calls repository deleteSet`() = runTest(mainDispatcherRule.testDispatcher) {
        // Act
        viewModel.onDeleteSet(fakeSet1)

        // "Прокручиваем", чтобы `viewModelScope.launch` выполнился
        advanceUntilIdle()

        // Assert
        coVerify(exactly = 1) { studySetRepository.deleteSet(fakeSet1) }
    }
}