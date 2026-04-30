package com.example.memorizy.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.memorizy.R

val GoogleSansRegular = FontFamily(
    Font(R.font.google_sans_regular)
)

val GoogleSansBold = FontFamily(
    Font(R.font.google_sans_bold)
)

val GoogleSansItalic = FontFamily(
    Font(R.font.google_sans_italic)
)

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = GoogleSansBold,
        fontSize = 28.sp
    ),
    displayMedium = TextStyle(
        fontFamily = GoogleSansBold,
        fontSize = 24.sp
    ),
    displaySmall = TextStyle(
        fontFamily = GoogleSansBold,
        fontSize = 18.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = GoogleSansRegular,
        fontSize = 24.sp
    ),
    bodySmall = TextStyle(
        fontFamily = GoogleSansRegular,
        fontSize = 18.sp
    ),
    labelLarge = TextStyle(
        fontFamily = GoogleSansItalic,
        fontSize = 28.sp
    ),
    labelMedium = TextStyle(
        fontFamily = GoogleSansItalic,
        fontSize = 24.sp
    ),
    labelSmall = TextStyle(
        fontFamily = GoogleSansItalic,
        fontSize = 18.sp
    )
)