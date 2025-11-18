package com.example.memorizy.ui.addcard

data class AddCardState (
    val term: String = "",
    val definition: String = "",
    val isTermEmptyError: Boolean = false,
    val isDefinitionEmptyError: Boolean = false,
    val isCardCreated: Boolean = false
)