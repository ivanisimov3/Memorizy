package com.example.memorizy.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.memorizy.R

val InterBold = FontFamily(
    Font(R.font.inter_bold)
)

val InterItalic = FontFamily(
    Font(R.font.inter_italic)
)

val InterRegular = FontFamily(
    Font(R.font.inter_regular)
)

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = InterBold,
        fontSize = 28.sp
    ),
    displayMedium = TextStyle(
        fontFamily = InterBold,
        fontWeight = FontWeight.Bold,
        fontSize = 24.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = InterRegular,
        fontSize = 24.sp
    ),
    bodySmall = TextStyle(
        fontFamily = InterRegular,
        fontSize = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = InterItalic,
        fontSize = 28.sp
    ),
    labelMedium = TextStyle(
        fontFamily = InterItalic,
        fontSize = 24.sp
    ),
    labelSmall = TextStyle(
        fontFamily = InterItalic,
        fontSize = 18.sp
    )
)