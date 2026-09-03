package com.example.util

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

object DateUtils {

    private val ARABIC_MONTH_NAMES = listOf(
        "يناير", "فبراير", "مارس", "أبريل", "مايو", "يونيو",
        "يوليو", "أغسطس", "سبتمبر", "أكتوبر", "نوفمبر", "ديسمبر"
    )

    private val ARABIC_DAY_NAMES = listOf(
        "الأحد", "الإثنين", "الثلاثاء", "الأربعاء", "الخميس", "الجمعة", "السبت"
    )

    fun getTodayDateString(): String {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        return sdf.format(cal.time)
    }

    fun getCurrentMonthPrefix(): String {
        val cal = Calendar.getInstance()
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        return sdf.format(cal.time)
    }

    fun parseYearMonth(monthPrefix: String): Pair<Int, Int> {
        val parts = monthPrefix.split("-")
        val year = parts.getOrNull(0)?.toIntOrNull() ?: Calendar.getInstance().get(Calendar.YEAR)
        val month = parts.getOrNull(1)?.toIntOrNull()?.minus(1) ?: Calendar.getInstance().get(Calendar.MONTH)
        return Pair(year, month)
    }

    fun getMonthNameAr(monthIndex: Int, year: Int): String {
        val m = (monthIndex % 12 + 12) % 12
        return "${ARABIC_MONTH_NAMES[m]} $year"
    }

    fun formatMonthPrefix(year: Int, monthIndex: Int): String {
        val m = monthIndex + 1
        return String.format(Locale.US, "%04d-%02d", year, m)
    }

    fun offsetMonth(monthPrefix: String, offset: Int): String {
        val (year, month) = parseYearMonth(monthPrefix)
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.add(Calendar.MONTH, offset)
        val sdf = SimpleDateFormat("yyyy-MM", Locale.US)
        return sdf.format(cal.time)
    }

    fun formatArabicFullDate(dateStr: String): String {
        return try {
            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
            val date = sdf.parse(dateStr) ?: return dateStr
            val cal = Calendar.getInstance()
            cal.time = date
            val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1 // 0=Sunday
            val dayOfMonth = cal.get(Calendar.DAY_OF_MONTH)
            val month = cal.get(Calendar.MONTH)
            val year = cal.get(Calendar.YEAR)
            val dayName = ARABIC_DAY_NAMES.getOrElse(dayOfWeek) { "" }
            val monthName = ARABIC_MONTH_NAMES.getOrElse(month) { "" }
            "$dayName، $dayOfMonth $monthName $year"
        } catch (e: Exception) {
            dateStr
        }
    }

    fun getDaysInMonth(year: Int, monthIndex: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthIndex)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        return cal.getActualMaximum(Calendar.DAY_OF_MONTH)
    }

    fun getFirstDayOfWeekInMonth(year: Int, monthIndex: Int): Int {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, monthIndex)
        cal.set(Calendar.DAY_OF_MONTH, 1)
        // Calendar.SUNDAY is 1, Calendar.MONDAY is 2 ... Calendar.SATURDAY is 7
        // Return 0 for Sunday, 1 for Monday, etc.
        return cal.get(Calendar.DAY_OF_WEEK) - 1
    }

    fun getWeekInfo(weekOffset: Int = 0): WeekRangeInfo {
        val cal = Calendar.getInstance()
        if (weekOffset != 0) {
            cal.add(Calendar.WEEK_OF_YEAR, weekOffset)
        }

        // Find Saturday of this week
        val dayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        val diffToSaturday = if (dayOfWeek == Calendar.SATURDAY) 0 else -((dayOfWeek - Calendar.SUNDAY + 1) % 7)
        cal.add(Calendar.DAY_OF_MONTH, diffToSaturday)

        val todayStr = getTodayDateString()
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US)
        val days = mutableListOf<WeekDayInfo>()

        for (i in 0 until 7) {
            val dateStr = sdf.format(cal.time)
            val dayOfWeekIndex = cal.get(Calendar.DAY_OF_WEEK) - 1
            val dayNumber = cal.get(Calendar.DAY_OF_MONTH)
            val month = cal.get(Calendar.MONTH)
            val dayNameAr = ARABIC_DAY_NAMES.getOrElse(dayOfWeekIndex) { "" }
            val monthNameAr = ARABIC_MONTH_NAMES.getOrElse(month) { "" }

            days.add(
                WeekDayInfo(
                    dateStr = dateStr,
                    dayNameAr = dayNameAr,
                    dayNumber = dayNumber,
                    monthNameAr = monthNameAr,
                    isToday = (dateStr == todayStr)
                )
            )
            if (i < 6) {
                cal.add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        val startDay = days.first()
        val endDay = days.last()
        val labelAr = "${startDay.dayNameAr} ${startDay.dayNumber} ${startDay.monthNameAr} - ${endDay.dayNameAr} ${endDay.dayNumber} ${endDay.monthNameAr}"

        return WeekRangeInfo(
            startDateStr = startDay.dateStr,
            endDateStr = endDay.dateStr,
            labelAr = labelAr,
            days = days,
            isCurrentWeek = (weekOffset == 0)
        )
    }
}

data class WeekDayInfo(
    val dateStr: String,
    val dayNameAr: String,
    val dayNumber: Int,
    val monthNameAr: String,
    val isToday: Boolean
)

data class WeekRangeInfo(
    val startDateStr: String,
    val endDateStr: String,
    val labelAr: String,
    val days: List<WeekDayInfo>,
    val isCurrentWeek: Boolean
)

