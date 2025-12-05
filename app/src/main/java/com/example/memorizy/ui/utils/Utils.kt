package com.example.memorizy.ui.utils

import com.example.memorizy.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object AppIcons {
    val allIcons = mapOf(
        1 to R.drawable.ic_set1,
        2 to R.drawable.ic_set2,
        3 to R.drawable.ic_set3,
        4 to R.drawable.ic_set4,
        5 to R.drawable.ic_set5
    )

    fun getIconResById(iconId: Int): Int {
        return allIcons[iconId] ?: R.drawable.ic_set1
    }
}

fun formatDate(timestamp: Long): String {
    val formatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())
    return formatter.format(Date(timestamp))
}