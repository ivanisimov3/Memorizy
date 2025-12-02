package com.example.memorizy.ui.screens.addcard

data class AddCardState (
    val term: String = "",
    val definition: String = "",
    val isTermEmptyError: Boolean = false,
    val isDefinitionEmptyError: Boolean = false,
    val isCardCreated: Boolean = false
)