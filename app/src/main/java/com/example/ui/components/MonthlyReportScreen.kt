package com.example.ui.components

import android.content.Context
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.PendingActions
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.TrendingDown
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.DayCompletionLevel
import com.example.model.DayStat
import com.example.model.MonthlyReportData
import com.example.ui.theme.CompletionHigh
import com.example.ui.theme.CompletionHighBg
import com.example.ui.theme.CompletionLow
import com.example.ui.theme.CompletionLowBg
import com.example.ui.theme.CompletionMedium
import com.example.ui.theme.CompletionMediumBg
import com.example.ui.theme.PriorityHigh
import com.example.ui.theme.PriorityLow
import com.example.ui.theme.PriorityMedium
import com.example.ui.theme.TealPrimary
import com.example.util.ReportExporter
import java.util.Locale

@Composable
fun MonthlyReportScreen(
    report: MonthlyReportData,
    onMonthChange: (offset: Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
            .testTag("monthly_report_screen"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Month Header Navigator
        MonthNavigatorCard(
            monthNameAr = report.monthNameAr,
            onPrev = { onMonthChange(-1) },
            onNext = { onMonthChange(1) }
        )

        // Action Buttons Row (PDF Export & Share)
        ReportActionsRow(
            context = context,
            report = report
        )

        // Overall Performance KPI Card
        OverallKpiCard(report = report)

        // Daily Progress Chart (رسم بياني لتطور نسبة الإنجاز يومياً)
        DailyProgressChartCard(dailyStats = report.dailyStats)

        // Best & Worst Week Comparison
        WeeksComparisonCard(
            bestWeek = report.bestWeek,
            worstWeek = report.worstWeek
        )

        // Most Postponed / Uncompleted Tasks
        if (report.mostPostponedTasks.isNotEmpty()) {
            MostPostponedTasksCard(tasks = report.mostPostponedTasks)
        }

        // Breakdown by Priority & Category
        BreakdownCards(
            priorityStats = report.priorityStats,
            categoryStats = report.categoryStats
        )

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
private fun MonthNavigatorCard(
    monthNameAr: String,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onPrev) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                    contentDescription = "الشهر السابق",
                    tint = MaterialTheme.colorScheme.primary
                )
            }

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "تقرير شهر $monthNameAr",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "تحليل الأداء ونسب الإنجاز",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            IconButton(onClick = onNext) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "الشهر التالي",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
    }
}

@Composable
private fun ReportActionsRow(
    context: Context,
    report: MonthlyReportData
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        Button(
            onClick = { ReportExporter.printOrSavePdf(context, report) },
            modifier = Modifier
                .weight(1f)
                .testTag("export_pdf_button"),
            colors = ButtonDefaults.buttonColors(containerColor = TealPrimary),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.PictureAsPdf,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("تصدير كـ PDF", fontWeight = FontWeight.SemiBold)
        }

        OutlinedButton(
            onClick = { ReportExporter.shareReportAsText(context, report) },
            modifier = Modifier
                .weight(1f)
                .testTag("share_report_button"),
            shape = RoundedCornerShape(12.dp)
        ) {
            Icon(
                imageVector = Icons.Default.Share,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text("مشاركة التقرير", fontWeight = FontWeight.SemiBold)
        }
    }
}

@Composable
private fun OverallKpiCard(report: MonthlyReportData) {
    val animatedRate by animateFloatAsState(
        targetValue = report.overallCompletionRate,
        animationSpec = tween(durationMillis = 800),
        label = "overallRate"
    )

    val rateColor = when {
        report.overallCompletionRate >= 80f -> CompletionHigh
        report.overallCompletionRate >= 40f -> CompletionMedium
        else -> CompletionLow
    }

    val ratingTitle = when {
        report.totalTasks == 0 -> "لا توجد مهام مسجلة"
        report.overallCompletionRate >= 85f -> "أداء استثنائي! 🌟"
        report.overallCompletionRate >= 70f -> "إنجاز ممتاز 👍"
        report.overallCompletionRate >= 50f -> "أداء متوسط، استمر بالتحسن 💪"
        else -> "بحاجة لتعزيز الالتزام بالمهام 🎯"
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "نسبة الإنجاز الإجمالية للشهر",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Circular Progress Gauge
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val strokeWidth = 14.dp.toPx()
                    val diameter = size.minDimension - strokeWidth
                    val topLeft = Offset(strokeWidth / 2f, strokeWidth / 2f)
                    val arcSize = Size(diameter, diameter)

                    // Track background arc
                    drawArc(
                        color = Color.LightGray.copy(alpha = 0.25f),
                        startAngle = 140f,
                        sweepAngle = 260f,
                        useCenter = false,
                        style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                        topLeft = topLeft,
                        size = arcSize
                    )

                    // Progress active arc
                    val sweepProgress = (animatedRate / 100f).coerceIn(0f, 1f) * 260f
                    if (sweepProgress > 0f) {
                        drawArc(
                            color = rateColor,
                            startAngle = 140f,
                            sweepAngle = sweepProgress,
                            useCenter = false,
                            style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                            topLeft = topLeft,
                            size = arcSize
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = "${String.format(Locale.US, "%.0f", animatedRate)}%",
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Black,
                        color = rateColor
                    )
                    Text(
                        text = "معدل الإنجاز",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = ratingTitle,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.primary
            )

            Spacer(modifier = Modifier.height(20.dp))

            // 3 KPI Columns: Completed, Pending, Total
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(
                        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(14.dp)
                    )
                    .padding(vertical = 12.dp, horizontal = 10.dp),
                horizontalArrangement = Arrangement.SpaceAround
            ) {
                MetricColumn(
                    value = "${report.completedTasks}",
                    label = "المنجزة",
                    color = CompletionHigh,
                    icon = Icons.Default.CheckCircle
                )
                MetricColumn(
                    value = "${report.uncompletedTasks}",
                    label = "المتبقية",
                    color = CompletionLow,
                    icon = Icons.Default.PendingActions
                )
                MetricColumn(
                    value = "${report.totalTasks}",
                    label = "الإجمالي",
                    color = MaterialTheme.colorScheme.primary,
                    icon = Icons.Default.EmojiEvents
                )
            }
        }
    }
}

@Composable
private fun MetricColumn(
    value: String,
    label: String,
    color: Color,
    icon: androidx.compose.ui.graphics.vector.ImageVector
) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(20.dp)
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )
        Text(
            text = label,
            fontSize = 11.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun DailyProgressChartCard(dailyStats: List<DayStat>) {
    var selectedDayStat by remember { mutableStateOf<DayStat?>(null) }

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "رسم بياني لتطور الإنجاز اليومي",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "تطور نسبة إنجاز المهام يوماً بيوم خلال الشهر",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Icon(
                    imageVector = Icons.Default.TrendingUp,
                    contentDescription = null,
                    tint = TealPrimary,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Chart Selected Day Detail Tooltip
            selectedDayStat?.let { stat ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "يوم ${stat.dayNumber}: ${stat.completedTasks} من ${stat.totalTasks} مهام",
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                    Text(
                        text = "${String.format(Locale.US, "%.0f", stat.completionRate * 100f)}%",
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold,
                        color = when (stat.level) {
                            DayCompletionLevel.HIGH -> CompletionHigh
                            DayCompletionLevel.MEDIUM -> CompletionMedium
                            else -> CompletionLow
                        }
                    )
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            // Canvas Chart
            val daysCount = dailyStats.size
            val highColor = CompletionHigh
            val medColor = CompletionMedium
            val lowColor = CompletionLow
            val neutralBar = Color.LightGray.copy(alpha = 0.2f)

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
                    .pointerInput(dailyStats) {
                        detectTapGestures { tapOffset ->
                            if (daysCount > 0) {
                                val colWidth = size.width / daysCount
                                val tappedIndex = (tapOffset.x / colWidth).toInt().coerceIn(0, daysCount - 1)
                                selectedDayStat = dailyStats[tappedIndex]
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    val w = size.width
                    val h = size.height
                    val bottomMargin = 20.dp.toPx()
                    val chartHeight = h - bottomMargin

                    // Draw 100%, 50% dotted grid lines
                    val line100Y = 0f
                    val line50Y = chartHeight * 0.5f
                    val baselineY = chartHeight

                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.3f),
                        start = Offset(0f, line100Y),
                        end = Offset(w, line100Y),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.2f),
                        start = Offset(0f, line50Y),
                        end = Offset(w, line50Y),
                        strokeWidth = 1.dp.toPx()
                    )
                    drawLine(
                        color = Color.LightGray.copy(alpha = 0.4f),
                        start = Offset(0f, baselineY),
                        end = Offset(w, baselineY),
                        strokeWidth = 1.5.dp.toPx()
                    )

                    // Draw Daily Bars
                    val barSpacing = 2.dp.toPx()
                    val totalSlots = daysCount.coerceAtLeast(1)
                    val slotWidth = w / totalSlots
                    val barWidth = (slotWidth - barSpacing).coerceAtLeast(2.dp.toPx())

                    dailyStats.forEachIndexed { index, stat ->
                        val x = index * slotWidth + (barSpacing / 2f)
                        val barColor = when (stat.level) {
                            DayCompletionLevel.HIGH -> highColor
                            DayCompletionLevel.MEDIUM -> medColor
                            DayCompletionLevel.LOW -> lowColor
                            DayCompletionLevel.EMPTY -> neutralBar
                        }

                        val barHeight = if (stat.totalTasks > 0) {
                            (stat.completionRate * chartHeight).coerceAtLeast(4.dp.toPx())
                        } else {
                            3.dp.toPx()
                        }

                        val topY = baselineY - barHeight

                        drawRoundRect(
                            color = barColor,
                            topLeft = Offset(x, topY),
                            size = Size(barWidth, barHeight),
                            cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx())
                        )
                    }
                }
            }

            // Day scale indicators (e.g. 1, 5, 10, 15, 20, 25, 30)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                listOf(1, 5, 10, 15, 20, 25, dailyStats.size).distinct().forEach { day ->
                    Text(
                        text = "$day",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun WeeksComparisonCard(
    bestWeek: com.example.model.WeekStat?,
    worstWeek: com.example.model.WeekStat?
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "تقييم أداء الأسابيع",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                // Best Week Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CompletionHighBg)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.EmojiEvents,
                                contentDescription = null,
                                tint = Color(0xFF047857),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "أفضل أسبوع",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF047857)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        if (bestWeek != null) {
                            Text(
                                text = bestWeek.labelAr,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF064E3B)
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.0f", bestWeek.completionRate)}% إنجاز",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFF047857)
                            )
                            Text(
                                text = "${bestWeek.completedTasks} من ${bestWeek.totalTasks} مهام",
                                fontSize = 10.sp,
                                color = Color(0xFF065F46)
                            )
                        } else {
                            Text(
                                text = "لا توجد بيانات",
                                fontSize = 11.sp,
                                color = Color(0xFF065F46)
                            )
                        }
                    }
                }

                // Worst Week Card
                Card(
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = CompletionLowBg)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.TrendingDown,
                                contentDescription = null,
                                tint = Color(0xFFB91C1C),
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "أقل أسبوع",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFFB91C1C)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        if (worstWeek != null) {
                            Text(
                                text = worstWeek.labelAr,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold,
                                color = Color(0xFF7F1D1D)
                            )
                            Text(
                                text = "${String.format(Locale.US, "%.0f", worstWeek.completionRate)}% إنجاز",
                                fontSize = 16.sp,
                                fontWeight = FontWeight.Black,
                                color = Color(0xFFB91C1C)
                            )
                            Text(
                                text = "${worstWeek.completedTasks} من ${worstWeek.totalTasks} مهام",
                                fontSize = 10.sp,
                                color = Color(0xFF991B1B)
                            )
                        } else {
                            Text(
                                text = "أداء ممتاز ومتقارب",
                                fontSize = 11.sp,
                                color = Color(0xFF991B1B)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun MostPostponedTasksCard(tasks: List<com.example.model.TaskFrequency>) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = CompletionLow,
                    modifier = Modifier.size(22.dp)
                )
                Column {
                    Text(
                        text = "المهام الأكثر تكراراً في عدم الإنجاز",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "مهام تأجلت أو لم تكتمل عدة أيام هذا الشهر",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                tasks.forEachIndexed { index, taskFreq ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Text(
                                text = "${index + 1}.",
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary,
                                fontSize = 13.sp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Column {
                                Text(
                                    text = taskFreq.title,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = taskFreq.category,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(6.dp))
                                .background(CompletionLowBg)
                                .padding(horizontal = 8.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "${taskFreq.uncompletedCount} مرات",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = CompletionLow
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun BreakdownCards(
    priorityStats: List<com.example.model.PriorityStat>,
    categoryStats: List<com.example.model.CategoryStat>
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "الإنجاز حسب الأولوية والتصنيف",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Priority Progress Bars
            Text(
                text = "حسب الأولوية:",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))

            priorityStats.filter { it.total > 0 }.forEach { pStat ->
                val pColor = when (pStat.priorityKey) {
                    "HIGH" -> PriorityHigh
                    "LOW" -> PriorityLow
                    else -> PriorityMedium
                }
                Column(modifier = Modifier.padding(vertical = 4.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "أولوية ${pStat.labelAr}",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "${pStat.completed}/${pStat.total} (${String.format(Locale.US, "%.0f", pStat.rate)}%)",
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold,
                            color = pColor
                        )
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    LinearProgressIndicator(
                        progress = { (pStat.rate / 100f).coerceIn(0f, 1f) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(6.dp)
                            .clip(RoundedCornerShape(3.dp)),
                        color = pColor,
                        trackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                }
            }

            if (categoryStats.isNotEmpty()) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "حسب التصنيف:",
                    fontSize = 12.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))

                categoryStats.take(4).forEach { catStat ->
                    Column(modifier = Modifier.padding(vertical = 4.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Text(
                                text = catStat.category,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "${catStat.completed}/${catStat.total} (${String.format(Locale.US, "%.0f", catStat.rate)}%)",
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold,
                                color = TealPrimary
                            )
                        }
                        Spacer(modifier = Modifier.height(4.dp))
                        LinearProgressIndicator(
                            progress = { (catStat.rate / 100f).coerceIn(0f, 1f) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = TealPrimary,
                            trackColor = MaterialTheme.colorScheme.surfaceVariant
                        )
                    }
                }
            }
        }
    }
}
