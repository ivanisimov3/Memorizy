package com.example.memorizy.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.memorizy.R

val MarmeladRegular = FontFamily(
    Font(R.font.marmelad_regular)
)

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = MarmeladRegular,
        fontSize = 28.sp
    ),
    displayMedium = TextStyle(
        fontFamily = MarmeladRegular,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = MarmeladRegular,
        fontSize = 24.sp
    ),
    bodySmall = TextStyle(
        fontFamily = MarmeladRegular,
        fontSize = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = MarmeladRegular,
        fontSize = 28.sp,
        fontStyle = FontStyle.Italic
    ),
    labelMedium = TextStyle(
        fontFamily = MarmeladRegular,
        fontSize = 24.sp,
        fontStyle = FontStyle.Italic
    ),
    labelSmall = TextStyle(
        fontFamily = MarmeladRegular,
        fontSize = 18.sp,
        fontStyle = FontStyle.Italic
    )
)