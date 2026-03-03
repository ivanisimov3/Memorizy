package com.example.memorizy.ui.navigation

import kotlinx.serialization.Serializable

// Карта всех экранов в приложении

@Serializable
object Routes {

    @Serializable
    object Settings

    @Serializable
    object Auth

    @Serializable
    object StudySets

    @Serializable
    object AddStudySet

    @Serializable
    data class SetDetails(val setId: Long)

    @Serializable
    data class LearningMode(val setId: Long)

    @Serializable
    data class TestingMode(val setId: Long)

    @Serializable
    data class AddCard(val setId: Long)
}