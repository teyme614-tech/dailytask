package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DayCompletionLevel
import com.example.model.DayStat
import com.example.ui.theme.CompletionHigh
import com.example.ui.theme.CompletionHighBg
import com.example.ui.theme.CompletionLow
import com.example.ui.theme.CompletionLowBg
import com.example.ui.theme.CompletionMedium
import com.example.ui.theme.CompletionMediumBg
import com.example.ui.theme.TealPrimary
import com.example.util.DateUtils
import java.util.Locale

@Composable
fun CalendarView(
    selectedMonthPrefix: String, // "yyyy-MM"
    selectedDate: String, // "yyyy-MM-dd"
    dayStatsMap: Map<String, DayStat>,
    onMonthChange: (offset: Int) -> Unit,
    onDateSelect: (date: String) -> Unit,
    modifier: Modifier = Modifier
) {
    val (year, monthIndex) = DateUtils.parseYearMonth(selectedMonthPrefix)
    val monthName = DateUtils.getMonthNameAr(monthIndex, year)
    val daysInMonth = DateUtils.getDaysInMonth(year, monthIndex)
    val firstDayOffset = DateUtils.getFirstDayOfWeekInMonth(year, monthIndex) // 0 = Sunday
    val todayDateStr = DateUtils.getTodayDateString()

    val weekDaysAr = listOf("أحد", "إثنين", "ثلاثاء", "أربعاء", "خميس", "جمعة", "سبت")

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("calendar_card"),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Month Navigation Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // In RTL layout: ArrowBack points to next/previous correctly with auto-mirrored
                IconButton(
                    onClick = { onMonthChange(-1) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                        contentDescription = "الشهر السابق",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = monthName,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "تقويم نسبة الإنجاز اليومي",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                IconButton(
                    onClick = { onMonthChange(1) },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "الشهر التالي",
                        tint = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Weekday Headers
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                weekDaysAr.forEach { dayName ->
                    Text(
                        text = dayName,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Days Grid
            val totalCells = firstDayOffset + daysInMonth
            val totalRows = (totalCells + 6) / 7

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                for (row in 0 until totalRows) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        for (col in 0 until 7) {
                            val cellIndex = row * 7 + col
                            val dayNumber = cellIndex - firstDayOffset + 1

                            if (dayNumber in 1..daysInMonth) {
                                val cellDateStr = String.format(Locale.US, "%s-%02d", selectedMonthPrefix, dayNumber)
                                val stat = dayStatsMap[cellDateStr]
                                val isSelected = cellDateStr == selectedDate
                                val isToday = cellDateStr == todayDateStr

                                DayCell(
                                    dayNumber = dayNumber,
                                    stat = stat,
                                    isSelected = isSelected,
                                    isToday = isToday,
                                    onClick = { onDateSelect(cellDateStr) },
                                    modifier = Modifier.weight(1f)
                                )
                            } else {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Calendar Color Legend
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(10.dp)
                    )
                    .padding(vertical = 8.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                LegendItem(color = CompletionHigh, label = "عالية (≥80%)")
                LegendItem(color = CompletionMedium, label = "متوسطة (40-79%)")
                LegendItem(color = CompletionLow, label = "منخفضة (<40%)")
            }
        }
    }
}

@Composable
private fun DayCell(
    dayNumber: Int,
    stat: DayStat?,
    isSelected: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val level = stat?.level ?: DayCompletionLevel.EMPTY

    // Background tint based on completion level
    val bgColor = when (level) {
        DayCompletionLevel.HIGH -> CompletionHighBg
        DayCompletionLevel.MEDIUM -> CompletionMediumBg
        DayCompletionLevel.LOW -> CompletionLowBg
        DayCompletionLevel.EMPTY -> Color.Transparent
    }

    // Indicator dot/bar color
    val indicatorColor = when (level) {
        DayCompletionLevel.HIGH -> CompletionHigh
        DayCompletionLevel.MEDIUM -> CompletionMedium
        DayCompletionLevel.LOW -> CompletionLow
        DayCompletionLevel.EMPTY -> Color.Transparent
    }

    val animatedBg by animateColorAsState(
        targetValue = if (isSelected) TealPrimary else bgColor,
        label = "cellBg"
    )

    val textColor = when {
        isSelected -> Color.White
        level == DayCompletionLevel.HIGH -> Color(0xFF065F46)
        level == DayCompletionLevel.MEDIUM -> Color(0xFF92400E)
        level == DayCompletionLevel.LOW -> Color(0xFF991B1B)
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(RoundedCornerShape(10.dp))
            .background(animatedBg)
            .border(
                width = if (isToday && !isSelected) 2.dp else if (isSelected) 2.dp else 0.5.dp,
                color = if (isSelected) TealPrimary else if (isToday) MaterialTheme.colorScheme.primary else Color.LightGray.copy(alpha = 0.2f),
                shape = RoundedCornerShape(10.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "$dayNumber",
                fontSize = 13.sp,
                fontWeight = if (isSelected || isToday) FontWeight.Bold else FontWeight.Medium,
                color = textColor
            )

            // Small indicator dot for completion level
            if (stat != null && stat.totalTasks > 0) {
                Box(
                    modifier = Modifier
                        .padding(top = 2.dp)
                        .size(5.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) Color.White else indicatorColor)
                )
            }
        }
    }
}

@Composable
private fun LegendItem(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(color)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(
            text = label,
            fontSize = 10.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
