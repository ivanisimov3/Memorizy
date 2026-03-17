package com.example.memorizy.ui.utils

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.dp
import com.example.memorizy.R
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
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
    private const val ONE_HOUR_MS = 60 * 60_000L
    private const val ONE_DAY_MS = 24 * ONE_HOUR_MS

    data class DeadlineCountdown(
        val remainingDays: Long,
        val remainingHours: Long
    )

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

    fun endOfDayMillis(timestamp: Long): Long {
        return Calendar.getInstance().apply {
            timeInMillis = timestamp
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }

    fun buildDeadlineCountdown(
        targetDate: Long,
        now: Long
    ): DeadlineCountdown {
        val deadlineMoment = endOfDayMillis(targetDate)
        val remainingMillis = (deadlineMoment - now).coerceAtLeast(0L)
        val remainingDays = remainingMillis / ONE_DAY_MS
        val remainingHours = (remainingMillis % ONE_DAY_MS) / ONE_HOUR_MS

        return DeadlineCountdown(
            remainingDays = remainingDays,
            remainingHours = remainingHours
        )
    }
}

@Composable
fun GlassContainer(
    modifier: Modifier = Modifier,
    shape: Shape = RoundedCornerShape(24.dp),
    containerColor: Color = MaterialTheme.colorScheme.primary,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    onLongClick: (() -> Unit)? = null,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = modifier
            .clip(shape)
            .then(
                if (onClick != null || onLongClick != null) {
                    Modifier.combinedClickable(
                        enabled = enabled,
                        onClick = onClick ?: {},    // combinedClickable обязательно требует onClick
                        onLongClick = onLongClick
                    )
                } else Modifier
            )
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        containerColor.copy(alpha = 0.35f),
                        containerColor.copy(alpha = 0.15f)
                    )
                )
            )
            .border(
                width = 2.dp,
                brush = Brush.verticalGradient(
                    colors = listOf(
                        containerColor.copy(alpha = 0.5f),
                        containerColor.copy(alpha = 0.25f)
                    )
                ),
                shape = shape
            ),
        contentAlignment = Alignment.Center,
        content = content
    )
}