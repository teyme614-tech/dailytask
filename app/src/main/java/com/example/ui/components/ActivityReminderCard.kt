package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalDrink
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.filled.WaterDrop
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.TaskItem
import com.example.ui.theme.AmberTertiary
import com.example.ui.theme.CompletionHigh
import com.example.ui.theme.TealPrimary

@Composable
fun ActivityReminderCard(
    tasksForToday: List<TaskItem>,
    modifier: Modifier = Modifier
) {
    var isVisible by remember { mutableStateOf(true) }
    var breakCounter by remember { mutableIntStateOf(0) }
    var waterCount by remember { mutableIntStateOf(0) }

    if (!isVisible) return

    val total = tasksForToday.size
    val completed = tasksForToday.count { it.isCompleted }
    val pending = total - completed

    val (title, description, accentColor) = when {
        total == 0 -> Triple(
            "تذكير بالنشاط والهمة 🎯",
            "ابدأ يومك بنشاط! سجّل أولى مهامك لليوم ونظّم وقتك لتحقيق أهدافك.",
            TealPrimary
        )
        pending == 0 && total > 0 -> Triple(
            "نشاطك اليوم في قمته! 🌟",
            "أحسنت! أنجزت جميع مهام اليوم بنجاح باهر. خذ وقتاً مستحقاً للراحة واستعادة الطاقة.",
            CompletionHigh
        )
        pending > 0 -> Triple(
            "تذكير بالنشاط والتركيز ⚡",
            "لديك $pending ${if (pending == 1) "مهمة متبقية" else "مهام متبقية"} اليوم. تذكّر شرب الماء وأخذ استراحة قصيرة لتحفيز طاقتك الذهنية والبدنية.",
            AmberTertiary
        )
        else -> Triple(
            "تذكير بالنشاط اليومي 🏃‍♂️",
            "حافظ على حركتك ونشاطك باستمرار خلال اليوم لتعزيز إنتاجيتك.",
            TealPrimary
        )
    }

    Card(
        modifier = modifier
            .fillMaxWidth()
            .testTag("activity_reminder_card"),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // Header Row with dismiss button
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.weight(1f)
                ) {
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(accentColor.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.DirectionsRun,
                            contentDescription = null,
                            tint = accentColor,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(
                    onClick = { isVisible = false },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "إخفاء التذكير",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                lineHeight = 20.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Interactive Activity Boosters: Water Drink & Quick Stretch
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Water Tracker booster
                OutlinedButton(
                    onClick = { waterCount++ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = TealPrimary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.WaterDrop,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (waterCount > 0) "ماء ($waterCount كوب)" else "شرب ماء 💧",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Quick Activity/Stretch booster
                OutlinedButton(
                    onClick = { breakCounter++ },
                    modifier = Modifier.weight(1f),
                    shape = RoundedCornerShape(10.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = AmberTertiary
                    )
                ) {
                    Icon(
                        imageVector = Icons.Default.FitnessCenter,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (breakCounter > 0) "استراحة ($breakCounter)" else "استراحة وتمدد ☕",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
