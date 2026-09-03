package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.DeleteSweep
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.CompletionLow
import com.example.ui.theme.TealPrimary
import com.example.util.DateUtils

@Composable
fun ResetTasksDialog(
    selectedDate: String,
    onDismiss: () -> Unit,
    onResetTodayCompletion: () -> Unit,
    onDeleteTodayTasks: () -> Unit,
    onClearAllTasks: () -> Unit
) {
    var showAllTasksConfirm by remember { mutableStateOf(false) }

    if (showAllTasksConfirm) {
        AlertDialog(
            onDismissRequest = { showAllTasksConfirm = false },
            icon = {
                Icon(
                    imageVector = Icons.Default.WarningAmber,
                    contentDescription = null,
                    tint = CompletionLow,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "تأكيد التصفير الشامل",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
            },
            text = {
                Text(
                    text = "سيتم حذف جميع المهام المسجلة في التطبيق نهائياً لكافة الأيام. هل أنت متأكد من رغبتك في المتابعة؟",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        showAllTasksConfirm = false
                        onClearAllTasks()
                        onDismiss()
                    },
                    modifier = Modifier.testTag("confirm_clear_all_button")
                ) {
                    Text(
                        text = "نعم، احذف الكل",
                        fontWeight = FontWeight.Bold,
                        color = CompletionLow
                    )
                }
            },
            dismissButton = {
                TextButton(onClick = { showAllTasksConfirm = false }) {
                    Text("تراجع")
                }
            },
            shape = RoundedCornerShape(20.dp)
        )
        return
    }

    val formattedDate = DateUtils.formatArabicFullDate(selectedDate)

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(TealPrimary.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.RestartAlt,
                        contentDescription = null,
                        tint = TealPrimary,
                        modifier = Modifier.size(22.dp)
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "خيارات تصفير المهام",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = formattedDate,
                        style = MaterialTheme.typography.bodySmall,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Text(
                    text = "اختر نوع التصفير المطلوب تنفيذه:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                // Option 1: Reset completion for today
                ResetOptionCard(
                    title = "إلغاء تحديد إنجاز مهام اليوم",
                    subtitle = "إعادة جميع مهام هذا اليوم إلى حالة غير منجزة (0%)",
                    icon = Icons.Default.RestartAlt,
                    iconTint = TealPrimary,
                    onClick = {
                        onResetTodayCompletion()
                        onDismiss()
                    },
                    testTag = "reset_today_completion_option"
                )

                // Option 2: Delete tasks for today
                ResetOptionCard(
                    title = "حذف مهام هذا اليوم",
                    subtitle = "مسح كافة المهام المسجلة في هذا اليوم فقط",
                    icon = Icons.Default.DeleteSweep,
                    iconTint = Color(0xFFD97706),
                    onClick = {
                        onDeleteTodayTasks()
                        onDismiss()
                    },
                    testTag = "delete_today_tasks_option"
                )

                // Option 3: Reset/Clear all tasks in app
                ResetOptionCard(
                    title = "تصفير شامل لكافة المهام",
                    subtitle = "مسح جميع المهام من كافة الأيام للبدء من الصفر",
                    icon = Icons.Default.DeleteForever,
                    iconTint = CompletionLow,
                    onClick = {
                        showAllTasksConfirm = true
                    },
                    testTag = "clear_all_tasks_option"
                )
            }
        },
        confirmButton = {},
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                modifier = Modifier.testTag("dismiss_reset_dialog")
            ) {
                Text("إغلاق", fontWeight = FontWeight.SemiBold)
            }
        },
        shape = RoundedCornerShape(20.dp)
    )
}

@Composable
private fun ResetOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    testTag: String
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .testTag(testTag),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(iconTint.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = iconTint,
                    modifier = Modifier.size(18.dp)
                )
            }

            Spacer(modifier = Modifier.width(10.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}
