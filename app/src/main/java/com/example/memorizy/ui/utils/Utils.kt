package com.example.memorizy.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import androidx.compose.material3.Icon
import java.util.Calendar

const val SESSION_TYPE_LEARNING = "learning"
const val SESSION_TYPE_TESTING = "testing"

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

object DateUtils {
    private val shortDateFormatter = SimpleDateFormat("dd.MM.yyyy", Locale.getDefault())
    private val fullDateTimeFormatter = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale.getDefault())

    fun formatShortDate(timestamp: Long): String {
        return shortDateFormatter.format(Date(timestamp))
    }

    fun formatFullDateTime(timestamp: Long): String {
        return fullDateTimeFormatter.format(Date(timestamp))
    }

    fun formatTimeUntil(ms: Long): String {
        val minutes = ms / 60_000
        val hours = minutes / 60
        val days = hours / 24

        return when {
            days > 0 -> "$days д"
            hours > 0 -> "$hours ч"
            else -> "$minutes мин"
        }
    }

    fun dayKey(timestamp: Long): Long {
        val cal = Calendar.getInstance()
        cal.timeInMillis = timestamp
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        return cal.timeInMillis
    }
}

@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(18.dp),
    containerColor: Color = MaterialTheme.colorScheme.primary,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .background(
                color = containerColor.copy(alpha = 0.15f)
            )
            .border(
                width = 1.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        containerColor.copy(alpha = 0.6f),
                        containerColor.copy(alpha = 0.2f)
                    )
                ),
                shape = shape
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
fun AppIcon(
    painter: Painter,
    contentDescription: String?,
    modifier: Modifier = Modifier,
    tint: Color = MaterialTheme.colorScheme.secondary
) {
    Icon(
        painter = painter,
        contentDescription = contentDescription,
        modifier = modifier,
        tint = tint
    )
}