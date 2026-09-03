package com.example.model

enum class DayCompletionLevel {
    HIGH,   // >= 80% (Green)
    MEDIUM, // 40% - 79% (Amber)
    LOW,    // < 40% (Red)
    EMPTY   // 0 tasks (Neutral)
}

data class DayStat(
    val date: String, // yyyy-MM-dd
    val dayNumber: Int,
    val totalTasks: Int,
    val completedTasks: Int,
    val completionRate: Float, // 0.0 to 1.0
    val level: DayCompletionLevel
)

data class WeekStat(
    val weekNumber: Int,
    val labelAr: String,
    val dateRangeAr: String,
    val totalTasks: Int,
    val completedTasks: Int,
    val completionRate: Float // 0.0 to 100.0
)

data class TaskFrequency(
    val title: String,
    val category: String,
    val uncompletedCount: Int
)

data class CategoryStat(
    val category: String,
    val total: Int,
    val completed: Int,
    val rate: Float
)

data class PriorityStat(
    val priorityKey: String,
    val labelAr: String,
    val total: Int,
    val completed: Int,
    val rate: Float
)

data class MonthlyReportData(
    val monthPrefix: String, // "yyyy-MM"
    val monthNameAr: String, // e.g. "سبتمبر 2026"
    val totalTasks: Int,
    val completedTasks: Int,
    val uncompletedTasks: Int,
    val overallCompletionRate: Float, // 0.0 to 100.0
    val dailyStats: List<DayStat>,
    val bestWeek: WeekStat?,
    val worstWeek: WeekStat?,
    val mostPostponedTasks: List<TaskFrequency>,
    val categoryStats: List<CategoryStat>,
    val priorityStats: List<PriorityStat>
)
