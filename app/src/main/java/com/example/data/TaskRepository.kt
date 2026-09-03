package com.example.data

import kotlinx.coroutines.flow.Flow
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class TaskRepository(private val taskDao: TaskDao) {

    fun getTasksForDate(date: String): Flow<List<TaskItem>> =
        taskDao.getTasksForDate(date)

    fun getTasksForMonth(monthPrefix: String): Flow<List<TaskItem>> =
        taskDao.getTasksForMonth(monthPrefix)

    fun getAllTasks(): Flow<List<TaskItem>> =
        taskDao.getAllTasks()

    suspend fun insertTask(task: TaskItem): Long =
        taskDao.insertTask(task)

    suspend fun insertTasks(tasks: List<TaskItem>) =
        taskDao.insertTasks(tasks)

    suspend fun updateTask(task: TaskItem) =
        taskDao.updateTask(task)

    suspend fun deleteTask(task: TaskItem) =
        taskDao.deleteTask(task)

    suspend fun deleteTaskById(id: Long) =
        taskDao.deleteTaskById(id)

    suspend fun toggleTaskCompletion(id: Long, currentCompleted: Boolean) {
        val nextCompleted = !currentCompleted
        val completedAt = if (nextCompleted) System.currentTimeMillis() else null
        taskDao.setTaskCompletion(id, nextCompleted, completedAt)
    }

    suspend fun seedInitialDataIfEmpty(currentMonthPrefix: String) {
        if (taskDao.countAll() == 0) {
            val sampleTasks = mutableListOf<TaskItem>()
            val cal = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

            // Let's create realistic tasks for today and earlier days of this month
            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
            val currentYear = cal.get(Calendar.YEAR)
            val currentMonth = cal.get(Calendar.MONTH) // 0-indexed

            val baseCategories = listOf("عمل", "صحة", "دراسة", "شخصي", "عبادة")
            val baseTitles = listOf(
                "قراءة ورد القرآن اليومي" to "عبادة",
                "ممارسة رياضة المشي 30 دقيقة" to "صحة",
                "مراجعة تقرير العمل الأسبوعي" to "عمل",
                "قراءة فصل من كتاب تطوير الذات" to "دراسة",
                "تنظيم جدول المهام والمواعيد" to "شخصي",
                "شرب 2 لتر من الماء" to "صحة",
                "إنهاء المهمة البرمجية للمشروع" to "عمل",
                "تعلم كلمات إنجليزية جديدة" to "دراسة"
            )

            // Seed previous days in current month (up to 7 days back or dayOfMonth)
            val startDay = maxOf(1, dayOfMonth - 6)
            for (d in startDay..dayOfMonth) {
                val dayCal = Calendar.getInstance()
                dayCal.set(currentYear, currentMonth, d)
                val dateStr = dateFormat.format(dayCal.time)
                val isToday = (d == dayOfMonth)

                val dayTaskCount = 4
                for (i in 0 until dayTaskCount) {
                    val pair = baseTitles[(d + i) % baseTitles.size]
                    val isDone = if (isToday) {
                        i % 2 == 0 // partially done today
                    } else {
                        // High completion rate for past days
                        (d + i) % 3 != 0
                    }
                    val priority = when ((d + i) % 3) {
                        0 -> TaskPriority.HIGH.name
                        1 -> TaskPriority.MEDIUM.name
                        else -> TaskPriority.LOW.name
                    }

                    sampleTasks.add(
                        TaskItem(
                            title = pair.first,
                            category = pair.second,
                            priority = priority,
                            date = dateStr,
                            isCompleted = isDone,
                            completedAt = if (isDone) System.currentTimeMillis() else null
                        )
                    )
                }
            }

            taskDao.insertTasks(sampleTasks)
        }
    }
}
