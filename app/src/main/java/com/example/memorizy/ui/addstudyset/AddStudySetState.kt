package com.example.memorizy.ui.addstudyset

data class AddStudySetState(
    val name: String = "",
    val description: String = "",
    val selectedIconId: Int = 1,
    val isNameEmptyError: Boolean = false,
    val isSetCreated: Boolean = false
)
