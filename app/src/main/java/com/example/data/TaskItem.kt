package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

enum class TaskPriority(val labelAr: String, val level: Int) {
    HIGH("عالية", 3),
    MEDIUM("متوسطة", 2),
    LOW("منخفضة", 1)
}

@Entity(tableName = "tasks")
data class TaskItem(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val category: String = "عام",
    val priority: String = TaskPriority.MEDIUM.name,
    val date: String, // Format: yyyy-MM-dd
    val isCompleted: Boolean = false,
    val completedAt: Long? = null,
    val notes: String = ""
)
