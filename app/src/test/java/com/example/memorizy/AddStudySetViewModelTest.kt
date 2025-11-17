package com.example.memorizy

import com.example.memorizy.domain.studyset.StudySetRepository
import com.example.memorizy.ui.addset.AddStudySetViewModel
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.junit4.MockKRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Rule
import org.junit.Test

@ExperimentalCoroutinesApi
class AddSetViewModelTest {

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
    private lateinit var viewModel: AddStudySetViewModel

    @Before
    fun setUp() {
        // "Учим" наш мок-репозиторий: "Когда вызовут insertSet, просто ничего не делай (Unit)"
        // Это ОБЯЗАТЕЛЬНО для suspend-функций, иначе тест упадет (как у нас было)
        coEvery { studySetRepository.insertSet(any()) } returns Unit

        // Создаем ViewModel
        viewModel = AddStudySetViewModel(studySetRepository)
    }

    // ТЕСТ 1: Пытаемся создать набор с ПУСТЫМ именем
    @Test
    fun `onCreateClicked with blank name sets error`() = runTest {
        // Arrange
        // (Имя уже пустое по умолчанию)

        // Act
        // Нажимаем "Создать"
        viewModel.onCreateButtonClicked()

        // Assert
        val state = viewModel.uiState.value
        assertTrue(state.isNameEmptyError) // 1. Ошибка должна появиться
        assertFalse(state.isSetCreated)    // 2. Набор НЕ создан

        // 3. Убеждаемся, что репозиторий НЕ БЫЛ вызван
        coVerify(exactly = 0) { studySetRepository.insertSet(any()) }
    }

    // ТЕСТ 2: Успешное создание набора
    @Test
    fun `onCreateClicked with valid name inserts set and sets created flag`() = runTest(mainDispatcherRule.testDispatcher) {
        // Arrange
        // Вводим валидное имя
        viewModel.onNameChanged("Английский")

        // Act
        // Нажимаем "Создать"
        viewModel.onCreateButtonClicked()

        // В `onCreateClicked` есть `viewModelScope.launch`,
        // поэтому мы "прокручиваем" время, чтобы он выполнился
        advanceUntilIdle()

        // Assert
        val state = viewModel.uiState.value
        assertFalse(state.isNameEmptyError) // 1. Ошибки нет
        assertTrue(state.isSetCreated)     // 2. Флаг создания "взведен"

        // 3. Убеждаемся, что репозиторий БЫЛ вызван 1 раз
        coVerify(exactly = 1) { studySetRepository.insertSet(any()) }
    }

    // ТЕСТ 3: Ошибка "сбрасывается", когда пользователь начинает печатать
    @Test
    fun `onNameChanged resets name error`() = runTest {
        // Arrange
        // Сначала "провоцируем" ошибку
        viewModel.onCreateButtonClicked()
        assertTrue(viewModel.uiState.value.isNameEmptyError) // Убедились, что ошибка есть

        // Act
        // Пользователь "начинает печатать"
        viewModel.onNameChanged("A")

        // Assert
        // Ошибка должна немедленно исчезнуть
        assertFalse(viewModel.uiState.value.isNameEmptyError)
    }
}