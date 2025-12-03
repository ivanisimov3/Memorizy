package com.example.memorizy.ui.navigation

import kotlinx.serialization.Serializable

// Built-in type safe APIs to provide compile-time type safety for your navigation graph
// Use the following rules to decide what type to use for your route:
// - Object: Use an object for routes without arguments.
// - Class: Use a class or data class for routes with arguments.
@Serializable
object Routes { // Почему имеено object Routes? а не class Routes и тд

    @Serializable
    object Auth

    @Serializable
    object StudySets

    @Serializable
    object AddStudySet

    @Serializable
    data class SetDetails(val setId: Long)

    @Serializable
    data class AddCard(val setId: Long)
}