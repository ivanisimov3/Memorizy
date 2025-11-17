package com.example.memorizy.ui.navigation

import kotlinx.serialization.Serializable

// Новый способ навигации вместо строк
@Serializable
object Routes {
    @Serializable
    object UserSets

    @Serializable
    object AddSet

    @Serializable
    data class SetDetails(val setId: Int)

    @Serializable
    data class AddCard(val setId: Int)
}