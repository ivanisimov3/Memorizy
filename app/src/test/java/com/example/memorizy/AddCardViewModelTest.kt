package com.example.memorizy

import androidx.lifecycle.SavedStateHandle
import androidx.navigation.toRoute
import com.example.memorizy.data.repository.cardrepository.CardRepository
import com.example.memorizy.ui.screens.addcard.AddCardViewModel
import com.example.memorizy.ui.navigation.Routes
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import io.mockk.mockkStatic
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AddCardViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @get:Rule
    val mockkRule = MockKRule(this)

    @MockK
    private lateinit var repository: CardRepository

    // Mock для SavedStateHandle
    @MockK
    private lateinit var savedStateHandle: SavedStateHandle

    private lateinit var viewModel: AddCardViewModel

    @Before
    fun setUp() {
        // 1. МАГИЯ: Подделываем расширение .toRoute()
        // Нам нужно, чтобы savedStateHandle.toRoute<Routes.AddCard>() возвращал наш объект
        mockkStatic("androidx.navigation.SavedStateHandleKt")

        val mockRoute = Routes.AddCard(setId = 100) // Допустим, мы в наборе ID=100
        every { savedStateHandle.toRoute<Routes.AddCard>() } returns mockRoute

        // Учим репозиторий ничего не делать при вставке
        coEvery { repository.insertCard(any()) } returns Unit

        viewModel = AddCardViewModel(savedStateHandle, repository)
    }

    @Test
    fun `save clicked with empty fields shows errors`() = runTest {
        // Act
        viewModel.onCreateButtonClicked()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.isTermEmptyError)       // Ошибка термина
        assertTrue(state.isDefinitionEmptyError) // Ошибка определения
        assertFalse(state.isCardCreated)         // Не создан

        // Проверяем, что в репозиторий ничего не ушло
        coVerify(exactly = 0) { repository.insertCard(any()) }
    }

    @Test
    fun `save clicked with valid fields saves card`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        viewModel.onTermChanged("Apple")
        viewModel.onDefinitionChanged("Яблоко")

        // Act
        viewModel.onCreateButtonClicked()
        advanceUntilIdle() // Ждем корутину

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isTermEmptyError)
        assertTrue(state.isCardCreated) // Успех!

        // Проверяем, что сохранили с правильным ID набора (100)
        coVerify {
            repository.insertCard(match {
                it.term == "Apple" && it.setId == 100
            })
        }
    }
}