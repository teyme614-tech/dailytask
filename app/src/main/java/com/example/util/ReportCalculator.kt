package com.example.util

import com.example.data.TaskItem
import com.example.data.TaskPriority
import com.example.model.CategoryStat
import com.example.model.DayCompletionLevel
import com.example.model.DayStat
import com.example.model.MonthlyReportData
import com.example.model.PriorityStat
import com.example.model.TaskFrequency
import com.example.model.WeekStat
import java.util.Locale

object ReportCalculator {

    fun generateMonthlyReport(monthPrefix: String, tasks: List<TaskItem>): MonthlyReportData {
        val (year, monthIndex) = DateUtils.parseYearMonth(monthPrefix)
        val monthNameAr = DateUtils.getMonthNameAr(monthIndex, year)
        val daysInMonth = DateUtils.getDaysInMonth(year, monthIndex)

        val totalTasks = tasks.size
        val completedTasks = tasks.count { it.isCompleted }
        val uncompletedTasks = totalTasks - completedTasks
        val overallRate = if (totalTasks > 0) {
            (completedTasks.toFloat() / totalTasks.toFloat()) * 100f
        } else {
            0f
        }

        // 1. Daily stats for every day in month
        val tasksByDay = tasks.groupBy {
            val dayPart = it.date.split("-").getOrNull(2)?.toIntOrNull() ?: 1
            dayPart
        }

        val dailyStats = (1..daysInMonth).map { dayNum ->
            val dayDateStr = String.format(Locale.US, "%s-%02d", monthPrefix, dayNum)
            val dayTasks = tasksByDay[dayNum] ?: emptyList()
            val dayTotal = dayTasks.size
            val dayCompleted = dayTasks.count { it.isCompleted }
            val dayRate = if (dayTotal > 0) (dayCompleted.toFloat() / dayTotal.toFloat()) else 0f

            val level = when {
                dayTotal == 0 -> DayCompletionLevel.EMPTY
                dayRate >= 0.80f -> DayCompletionLevel.HIGH
                dayRate >= 0.40f -> DayCompletionLevel.MEDIUM
                else -> DayCompletionLevel.LOW
            }

            DayStat(
                date = dayDateStr,
                dayNumber = dayNum,
                totalTasks = dayTotal,
                completedTasks = dayCompleted,
                completionRate = dayRate,
                level = level
            )
        }

        // 2. Week calculations (Week 1: 1-7, Week 2: 8-14, Week 3: 15-21, Week 4: 22-28, Week 5: 29-end)
        val weekRanges = listOf(
            Triple(1, 1, 7),
            Triple(2, 8, 14),
            Triple(3, 15, 21),
            Triple(4, 22, 28),
            Triple(5, 29, daysInMonth)
        ).filter { it.second <= daysInMonth }

        val weekStats = weekRanges.map { (wNum, startD, endD) ->
            val actualEnd = minOf(endD, daysInMonth)
            val weekTasks = tasks.filter { task ->
                val day = task.date.split("-").getOrNull(2)?.toIntOrNull() ?: 0
                day in startD..actualEnd
            }
            val wTotal = weekTasks.size
            val wCompleted = weekTasks.count { it.isCompleted }
            val wRate = if (wTotal > 0) (wCompleted.toFloat() / wTotal.toFloat()) * 100f else 0f
            WeekStat(
                weekNumber = wNum,
                labelAr = "الأسبوع $wNum",
                dateRangeAr = "$startD - $actualEnd $monthNameAr",
                totalTasks = wTotal,
                completedTasks = wCompleted,
                completionRate = wRate
            )
        }

        val activeWeeks = weekStats.filter { it.totalTasks > 0 }
        val bestWeek = activeWeeks.maxByOrNull { it.completionRate }
        val worstWeek = if (activeWeeks.size > 1) {
            activeWeeks.minByOrNull { it.completionRate }
        } else if (activeWeeks.size == 1 && activeWeeks[0].completionRate < 100f) {
            activeWeeks[0]
        } else null

        // 3. Most postponed / uncompleted tasks
        val uncompletedList = tasks.filter { !it.isCompleted }
        val mostPostponedTasks = uncompletedList
            .groupBy { it.title.trim() }
            .map { (title, items) ->
                val category = items.firstOrNull()?.category ?: "عام"
                TaskFrequency(
                    title = title,
                    category = category,
                    uncompletedCount = items.size
                )
            }
            .sortedByDescending { it.uncompletedCount }
            .take(5)

        // 4. Category breakdown
        val categoryStats = tasks
            .groupBy { it.category }
            .map { (cat, items) ->
                val cTotal = items.size
                val cComp = items.count { it.isCompleted }
                val cRate = if (cTotal > 0) (cComp.toFloat() / cTotal.toFloat()) * 100f else 0f
                CategoryStat(
                    category = cat,
                    total = cTotal,
                    completed = cComp,
                    rate = cRate
                )
            }
            .sortedByDescending { it.total }

        // 5. Priority breakdown
        val priorityStats = listOf(TaskPriority.HIGH, TaskPriority.MEDIUM, TaskPriority.LOW).map { p ->
            val pTasks = tasks.filter { it.priority == p.name }
            val pTotal = pTasks.size
            val pComp = pTasks.count { it.isCompleted }
            val pRate = if (pTotal > 0) (pComp.toFloat() / pTotal.toFloat()) * 100f else 0f
            PriorityStat(
                priorityKey = p.name,
                labelAr = p.labelAr,
                total = pTotal,
                completed = pComp,
                rate = pRate
            )
        }

        return MonthlyReportData(
            monthPrefix = monthPrefix,
            monthNameAr = monthNameAr,
            totalTasks = totalTasks,
            completedTasks = completedTasks,
            uncompletedTasks = uncompletedTasks,
            overallCompletionRate = overallRate,
            dailyStats = dailyStats,
            bestWeek = bestWeek,
            worstWeek = worstWeek,
            mostPostponedTasks = mostPostponedTasks,
            categoryStats = categoryStats,
            priorityStats = priorityStats
        )
    }
}
