package com.example.viewmodel

import android.app.Application
import android.content.Context
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.TaskItem
import com.example.data.TaskRepository
import com.example.model.DayStat
import com.example.model.MonthlyReportData
import com.example.util.AiEncouragementSpeaker
import com.example.util.DateUtils
import com.example.util.ReportCalculator
import com.example.util.WeekRangeInfo
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

enum class TaskFilter(val labelAr: String) {
    ALL("الكل"),
    PENDING("المتبقية"),
    COMPLETED("المنجزة")
}

data class CelebrationEvent(
    val id: Long = System.currentTimeMillis(),
    val phrase: String
)

class TaskViewModel(application: Application) : AndroidViewModel(application) {

    private val repository: TaskRepository
    private val prefs = application.getSharedPreferences("app_prefs", Context.MODE_PRIVATE)

    // Dark mode state: default to false (الوضع النهاري)
    private val _isDarkMode = MutableStateFlow(prefs.getBoolean("is_dark_mode", false))
    val isDarkMode: StateFlow<Boolean> = _isDarkMode.asStateFlow()

    // Sound effects & AI encouragement voice setting
    private val _isCelebrationSoundEnabled = MutableStateFlow(prefs.getBoolean("is_sound_enabled", true))
    val isCelebrationSoundEnabled: StateFlow<Boolean> = _isCelebrationSoundEnabled.asStateFlow()

    // Celebration event for confetti and encouraging speech
    private val _celebrationEvent = MutableStateFlow<CelebrationEvent?>(null)
    val celebrationEvent: StateFlow<CelebrationEvent?> = _celebrationEvent.asStateFlow()

    private val _selectedDate = MutableStateFlow(DateUtils.getTodayDateString())
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _selectedMonthPrefix = MutableStateFlow(DateUtils.getCurrentMonthPrefix())
    val selectedMonthPrefix: StateFlow<String> = _selectedMonthPrefix.asStateFlow()

    private val _taskFilter = MutableStateFlow(TaskFilter.ALL)
    val taskFilter: StateFlow<TaskFilter> = _taskFilter.asStateFlow()

    init {
        val database = AppDatabase.getDatabase(application)
        repository = TaskRepository(database.taskDao())
        viewModelScope.launch {
            repository.seedInitialDataIfEmpty(_selectedMonthPrefix.value)
        }
    }

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasksForSelectedDate: StateFlow<List<TaskItem>> = _selectedDate
        .flatMapLatest { date -> repository.getTasksForDate(date) }
        .combine(_taskFilter) { tasks, filter ->
            when (filter) {
                TaskFilter.ALL -> tasks
                TaskFilter.PENDING -> tasks.filter { !it.isCompleted }
                TaskFilter.COMPLETED -> tasks.filter { it.isCompleted }
            }
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasksForToday: StateFlow<List<TaskItem>> = repository.getTasksForDate(DateUtils.getTodayDateString())
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    @OptIn(ExperimentalCoroutinesApi::class)
    val allTasksForMonth: StateFlow<List<TaskItem>> = _selectedMonthPrefix
        .flatMapLatest { prefix -> repository.getTasksForMonth(prefix) }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val monthlyReport: StateFlow<MonthlyReportData> = combine(
        _selectedMonthPrefix,
        allTasksForMonth
    ) { prefix, tasks ->
        ReportCalculator.generateMonthlyReport(prefix, tasks)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ReportCalculator.generateMonthlyReport(_selectedMonthPrefix.value, emptyList())
    )

    // Map of date (yyyy-MM-dd) to DayStat for rapid lookup in calendar
    val monthlyDayStats: StateFlow<Map<String, DayStat>> = monthlyReport.map { report ->
        report.dailyStats.associateBy { it.date }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyMap()
    )

    // Weekly Plan State
    private val _weekOffset = MutableStateFlow(0)
    val weekOffset: StateFlow<Int> = _weekOffset.asStateFlow()

    val weekRangeInfo: StateFlow<WeekRangeInfo> = _weekOffset.map { offset ->
        DateUtils.getWeekInfo(offset)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = DateUtils.getWeekInfo(0)
    )

    @OptIn(ExperimentalCoroutinesApi::class)
    val tasksForWeek: StateFlow<List<TaskItem>> = weekRangeInfo.flatMapLatest { info ->
        repository.getTasksForDateRange(info.startDateStr, info.endDateStr)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    fun selectDate(date: String) {
        _selectedDate.value = date
        val monthPrefix = date.substring(0, 7)
        if (monthPrefix != _selectedMonthPrefix.value) {
            _selectedMonthPrefix.value = monthPrefix
        }
    }

    fun setFilter(filter: TaskFilter) {
        _taskFilter.value = filter
    }

    fun changeMonth(offset: Int) {
        val newPrefix = DateUtils.offsetMonth(_selectedMonthPrefix.value, offset)
        _selectedMonthPrefix.value = newPrefix
        // If the selected date is in the same month, keep it; otherwise set to 1st of month
        if (!_selectedDate.value.startsWith(newPrefix)) {
            _selectedDate.value = "$newPrefix-01"
        }
    }

    fun toggleTask(id: Long, currentCompleted: Boolean) {
        viewModelScope.launch {
            repository.toggleTaskCompletion(id, currentCompleted)
            // If the user just completed the task (was false, now true)
            if (!currentCompleted) {
                val phrase = AiEncouragementSpeaker.ENCOURAGEMENT_PHRASES.random()
                _celebrationEvent.value = CelebrationEvent(phrase = phrase)
            }
        }
    }

    fun deleteTask(task: TaskItem) {
        viewModelScope.launch {
            repository.deleteTask(task)
        }
    }

    fun addTask(
        title: String,
        category: String,
        priority: String,
        startDate: String,
        repeatDaysCount: Int = 1
    ) {
        viewModelScope.launch {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val parsedDate = sdf.parse(startDate) ?: Calendar.getInstance().time
            val cal = Calendar.getInstance().apply { time = parsedDate }

            val tasksToInsert = mutableListOf<TaskItem>()
            for (i in 0 until repeatDaysCount) {
                val dateString = sdf.format(cal.time)
                tasksToInsert.add(
                    TaskItem(
                        title = title.trim(),
                        category = category.trim().ifEmpty { "عام" },
                        priority = priority,
                        date = dateString,
                        isCompleted = false
                    )
                )
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }
            repository.insertTasks(tasksToInsert)
        }
    }

    fun updateTask(task: TaskItem) {
        viewModelScope.launch {
            repository.updateTask(task)
        }
    }

    fun changeWeek(delta: Int) {
        _weekOffset.value += delta
    }

    fun resetToCurrentWeek() {
        _weekOffset.value = 0
    }

    fun resetTasksCompletionForDate(date: String) {
        viewModelScope.launch {
            repository.resetTasksCompletionForDate(date)
        }
    }

    fun deleteTasksForDate(date: String) {
        viewModelScope.launch {
            repository.deleteTasksForDate(date)
        }
    }

    fun clearAllTasks() {
        viewModelScope.launch {
            repository.clearAllTasks()
        }
    }

    fun clearCelebration() {
        _celebrationEvent.value = null
    }

    fun triggerCelebration(customPhrase: String? = null) {
        val phrase = customPhrase ?: AiEncouragementSpeaker.ENCOURAGEMENT_PHRASES.random()
        _celebrationEvent.value = CelebrationEvent(phrase = phrase)
    }

    fun toggleCelebrationSound() {
        val nextState = !_isCelebrationSoundEnabled.value
        _isCelebrationSoundEnabled.value = nextState
        prefs.edit().putBoolean("is_sound_enabled", nextState).apply()
    }

    fun toggleDarkMode() {
        val nextMode = !_isDarkMode.value
        _isDarkMode.value = nextMode
        prefs.edit().putBoolean("is_dark_mode", nextMode).apply()
    }

    fun setDarkMode(enabled: Boolean) {
        _isDarkMode.value = enabled
        prefs.edit().putBoolean("is_dark_mode", enabled).apply()
    }
}
