package com.example.memorizy

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performTextInput
import com.example.memorizy.ui.addcard.AddCardScreenBody
import com.example.memorizy.ui.screens.addcard.AddCardState
import org.junit.Rule
import org.junit.Test

class AddCardScreenTest {

    // Это правило позволяет управлять Compose в тестах
    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun emptyFields_showError_whenButtonClicked() {
        // 1. Запускаем НАШ "Глупый" Composable
        // Мы подсовываем ему state с ошибками, как будто ViewModel уже отработал
        composeTestRule.setContent {
            AddCardScreenBody(
                modifier = androidx.compose.ui.Modifier,
                uiState = AddCardState(
                    term = "",
                    definition = "",
                    isTermEmptyError = true, // <-- Имитируем ошибку
                    isDefinitionEmptyError = true
                ),
                onBackClick = {},
                onCreateButtonClicked = {},
                onTermChanged = {},
                onDefinitionChanged = {}
            )
        }

        // 2. Проверяем, видит ли пользователь ошибки
        // Ищем текст ошибки на экране и утверждаем, что он отображается
        composeTestRule.onNodeWithText("Термин не может быть пустым").assertIsDisplayed()
        composeTestRule.onNodeWithText("Определение не может быть пустым").assertIsDisplayed()
    }

    @Test
    fun textInput_works() {
        // Тестируем, что текст можно ввести (хотя в dumb component это зависит от state)
        // Этот тест проверяет скорее то, что поля вообще существуют и кликабельны

        composeTestRule.setContent {
            AddCardScreenBody(
                modifier = androidx.compose.ui.Modifier,
                uiState = AddCardState(term = "", definition = ""),
                onBackClick = {},
                onCreateButtonClicked = {},
                onTermChanged = {}, // В реальном тесте интеграции здесь нужно обновлять стейт
                onDefinitionChanged = {}
            )
        }

        // Находим поле "Термин" и вводим текст
        composeTestRule.onNodeWithText("Термин").performTextInput("Hello")

        // (Примечание: так как наш Composable "глупый", он не обновит текст сам по себе,
        // если мы не передадим ему обновленный uiState.
        // Поэтому "глупые" компоненты чаще тестируют на отображение статического состояния, как в тесте выше).
    }
}