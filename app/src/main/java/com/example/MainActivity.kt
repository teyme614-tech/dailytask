package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.Checklist
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material.icons.filled.LightMode
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material.icons.filled.ViewWeek
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.components.ActivityReminderCard
import com.example.ui.components.AddTaskDialog
import com.example.ui.components.CalendarView
import com.example.ui.components.ConfettiCelebration
import com.example.ui.components.DailyTaskSection
import com.example.ui.components.MonthlyReportScreen
import com.example.ui.components.MotivationalMessageCard
import com.example.ui.components.ResetTasksDialog
import com.example.ui.components.WeeklyPlanScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.TealPrimary
import com.example.util.AiEncouragementSpeaker
import com.example.util.ClappingSoundSynthesizer
import com.example.viewmodel.TaskViewModel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: TaskViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val isDarkMode by viewModel.isDarkMode.collectAsStateWithLifecycle()
            MyApplicationTheme(darkTheme = isDarkMode) {
                CompositionLocalProvider(LocalLayoutDirection provides LayoutDirection.Rtl) {
                    DailyTasksApp(
                        viewModel = viewModel,
                        isDarkMode = isDarkMode
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DailyTasksApp(
    viewModel: TaskViewModel,
    isDarkMode: Boolean
) {
    var currentTab by remember { mutableIntStateOf(0) }
    var showAddTaskDialog by remember { mutableStateOf(false) }
    var showResetDialog by remember { mutableStateOf(false) }
    var addTaskPreselectedDate by remember { mutableStateOf<String?>(null) }

    val selectedDate by viewModel.selectedDate.collectAsStateWithLifecycle()
    val selectedMonthPrefix by viewModel.selectedMonthPrefix.collectAsStateWithLifecycle()
    val tasksForDate by viewModel.tasksForSelectedDate.collectAsStateWithLifecycle()
    val tasksForToday by viewModel.tasksForToday.collectAsStateWithLifecycle()
    val dayStatsMap by viewModel.monthlyDayStats.collectAsStateWithLifecycle()
    val taskFilter by viewModel.taskFilter.collectAsStateWithLifecycle()
    val monthlyReport by viewModel.monthlyReport.collectAsStateWithLifecycle()
    val weekRangeInfo by viewModel.weekRangeInfo.collectAsStateWithLifecycle()
    val tasksForWeek by viewModel.tasksForWeek.collectAsStateWithLifecycle()

    val celebrationEvent by viewModel.celebrationEvent.collectAsStateWithLifecycle()
    val isSoundEnabled by viewModel.isCelebrationSoundEnabled.collectAsStateWithLifecycle()

    val context = LocalContext.current
    val aiSpeaker = remember { AiEncouragementSpeaker(context) }
    DisposableEffect(Unit) {
        onDispose {
            aiSpeaker.shutdown()
        }
    }

    // Speak first with Gulf male voice, then trigger enthusiastic clapping right after speech
    val coroutineScope = rememberCoroutineScope()
    LaunchedEffect(celebrationEvent) {
        val event = celebrationEvent ?: return@LaunchedEffect
        if (isSoundEnabled) {
            aiSpeaker.speak(event.phrase) {
                // Trigger pure clapping sound immediately after speech ends
                coroutineScope.launch {
                    ClappingSoundSynthesizer.playApplauseSound()
                }
            }
        }
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .padding(4.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Checklist,
                                contentDescription = null,
                                tint = TealPrimary,
                                modifier = Modifier.size(28.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "مهامي اليومية",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = { viewModel.toggleCelebrationSound() },
                        modifier = Modifier.testTag("celebration_sound_toggle")
                    ) {
                        Icon(
                            imageVector = if (isSoundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                            contentDescription = if (isSoundEnabled) "صوت التشجيع والتصفيق مفعل" else "صوت التشجيع والتصفيق مكتوم",
                            tint = if (isSoundEnabled) TealPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { showResetDialog = true },
                        modifier = Modifier.testTag("appbar_reset_tasks_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RestartAlt,
                            contentDescription = "تصفير المهام",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    IconButton(
                        onClick = { viewModel.toggleDarkMode() },
                        modifier = Modifier.testTag("theme_mode_toggle")
                    ) {
                        Icon(
                            imageVector = if (isDarkMode) Icons.Default.LightMode else Icons.Default.DarkMode,
                            contentDescription = if (isDarkMode) "تفعيل الوضع النهاري" else "تفعيل الوضع الليلي",
                            tint = if (isDarkMode) Color(0xFFFBBF24) else TealPrimary
                        )
                    }
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        },
        bottomBar = {
            NavigationBar(
                containerColor = MaterialTheme.colorScheme.surface,
                tonalElevation = 6.dp
            ) {
                NavigationBarItem(
                    selected = currentTab == 0,
                    onClick = { currentTab = 0 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.DateRange,
                            contentDescription = "المهام والتقويم"
                        )
                    },
                    label = { Text("المهام والتقويم", fontWeight = if (currentTab == 0) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealPrimary,
                        selectedTextColor = TealPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_tasks_tab")
                )

                NavigationBarItem(
                    selected = currentTab == 1,
                    onClick = { currentTab = 1 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.ViewWeek,
                            contentDescription = "الخطة الأسبوعية"
                        )
                    },
                    label = { Text("الخطة الأسبوعية", fontWeight = if (currentTab == 1) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealPrimary,
                        selectedTextColor = TealPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_weekly_tab")
                )

                NavigationBarItem(
                    selected = currentTab == 2,
                    onClick = { currentTab = 2 },
                    icon = {
                        Icon(
                            imageVector = Icons.Default.Assessment,
                            contentDescription = "التقرير الشهري"
                        )
                    },
                    label = { Text("التقرير الشهري", fontWeight = if (currentTab == 2) FontWeight.Bold else FontWeight.Normal) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = TealPrimary,
                        selectedTextColor = TealPrimary,
                        indicatorColor = MaterialTheme.colorScheme.primaryContainer
                    ),
                    modifier = Modifier.testTag("nav_report_tab")
                )
            }
        },
        floatingActionButton = {
            if (currentTab == 0 || currentTab == 1) {
                FloatingActionButton(
                    onClick = {
                        addTaskPreselectedDate = null
                        showAddTaskDialog = true
                    },
                    containerColor = TealPrimary,
                    contentColor = Color.White,
                    shape = CircleShape,
                    modifier = Modifier.testTag("fab_add_task")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "إضافة مهمة جديدة",
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Crossfade(
                targetState = currentTab,
                label = "tabCrossfade"
            ) { tabIndex ->
                when (tabIndex) {
                    0 -> {
                        // Tasks & Calendar Screen
                        val scrollState = rememberScrollState()
                        Column(
                            modifier = Modifier
                                .fillMaxSize()
                                .verticalScroll(scrollState)
                                .padding(horizontal = 16.dp, vertical = 12.dp),
                            verticalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            // Motivational Message Card during the day (رسالة تحفيزية تظهر على الشاشة)
                            MotivationalMessageCard()

                            // Activity & Health Reminder (رسالة تذكيرية بالنشاط)
                            ActivityReminderCard(tasksForToday = tasksForToday)

                            // Interactive Calendar
                            CalendarView(
                                selectedMonthPrefix = selectedMonthPrefix,
                                selectedDate = selectedDate,
                                dayStatsMap = dayStatsMap,
                                onMonthChange = { offset -> viewModel.changeMonth(offset) },
                                onDateSelect = { date -> viewModel.selectDate(date) }
                            )

                            // Daily Tasks Section
                            DailyTaskSection(
                                selectedDate = selectedDate,
                                tasks = tasksForDate,
                                currentFilter = taskFilter,
                                onFilterChange = { filter -> viewModel.setFilter(filter) },
                                onToggleTask = { id, status -> viewModel.toggleTask(id, status) },
                                onDeleteTask = { task -> viewModel.deleteTask(task) },
                                onAddNewTask = {
                                    addTaskPreselectedDate = selectedDate
                                    showAddTaskDialog = true
                                },
                                onResetClick = { showResetDialog = true }
                            )

                            Spacer(modifier = Modifier.height(60.dp))
                        }
                    }
                    1 -> {
                        // Weekly Plan Screen
                        WeeklyPlanScreen(
                            weekRangeInfo = weekRangeInfo,
                            tasksForWeek = tasksForWeek,
                            onWeekChange = { offsetDelta -> viewModel.changeWeek(offsetDelta) },
                            onResetToCurrentWeek = { viewModel.resetToCurrentWeek() },
                            onToggleTask = { id, status -> viewModel.toggleTask(id, status) },
                            onDeleteTask = { task -> viewModel.deleteTask(task) },
                            onAddTaskForDay = { dateStr ->
                                addTaskPreselectedDate = dateStr
                                showAddTaskDialog = true
                            }
                        )
                    }
                    2 -> {
                        // Monthly Report Screen
                        MonthlyReportScreen(
                            report = monthlyReport,
                            onMonthChange = { offset -> viewModel.changeMonth(offset) }
                        )
                    }
                }
            }

            // Confetti celebration & AI encouraging voice banner
            ConfettiCelebration(
                isActive = celebrationEvent != null,
                aiPhrase = celebrationEvent?.phrase,
                onCelebrationFinished = { viewModel.clearCelebration() },
                onReplayVoice = {
                    celebrationEvent?.phrase?.let { phrase ->
                        aiSpeaker.speak(phrase) {
                            coroutineScope.launch {
                                ClappingSoundSynthesizer.playApplauseSound()
                            }
                        }
                    }
                }
            )
        }

        // Add Task Dialog
        if (showAddTaskDialog) {
            AddTaskDialog(
                selectedDate = addTaskPreselectedDate ?: selectedDate,
                onDismiss = {
                    showAddTaskDialog = false
                    addTaskPreselectedDate = null
                },
                onConfirm = { title, category, priority, startDate, repeatDays ->
                    viewModel.addTask(
                        title = title,
                        category = category,
                        priority = priority,
                        startDate = startDate,
                        repeatDaysCount = repeatDays
                    )
                    showAddTaskDialog = false
                    addTaskPreselectedDate = null
                }
            )
        }

        // Reset Tasks Dialog
        if (showResetDialog) {
            ResetTasksDialog(
                selectedDate = selectedDate,
                onDismiss = { showResetDialog = false },
                onResetTodayCompletion = { viewModel.resetTasksCompletionForDate(selectedDate) },
                onDeleteTodayTasks = { viewModel.deleteTasksForDate(selectedDate) },
                onClearAllTasks = { viewModel.clearAllTasks() }
            )
        }
    }
}
