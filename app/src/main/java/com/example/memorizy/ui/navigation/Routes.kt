package com.example.memorizy.ui.navigation

import kotlinx.serialization.Serializable

@Serializable
object Routes {
    @Serializable
    object UserSets

    @Serializable
    object AddSet

    @Serializable
    data class SetDetails(val setId: Int)

    // 4. Экран добавления карточки (нужен ID набора)
    @Serializable
    data class AddCard(val setId: Int)
}