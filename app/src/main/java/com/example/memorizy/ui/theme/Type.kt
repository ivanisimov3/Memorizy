package com.example.memorizy.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.memorizy.R

val Commissioner = FontFamily(
    Font(R.font.commissioner_regular)
)

val TikTokSans = FontFamily(
    Font(R.font.tiktoksans_regular),
    Font(R.font.tiktoksans_bold, FontWeight.Bold)
)

// Set of Material typography styles to start with
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = Commissioner,
        fontWeight = FontWeight.Bold,
        fontSize = 36.sp
    ),
    displayMedium = TextStyle(
        fontFamily = TikTokSans,
        fontWeight = FontWeight.Normal,
        fontSize = 20.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = TikTokSans,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp
    ),
    labelSmall = TextStyle(
        fontFamily = TikTokSans,
        fontWeight = FontWeight.Bold,
        fontSize = 14.sp
    )
)