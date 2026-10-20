package com.example.ui

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

object DateUtils {
    private val DATE_FORMAT = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val DISPLAY_FORMAT = SimpleDateFormat("EEE, dd MMM yyyy", Locale.getDefault())

    fun getTodayString(): String {
        return DATE_FORMAT.format(Date())
    }

    fun getYesterdayString(): String {
        val cal = Calendar.getInstance()
        cal.add(Calendar.DAY_OF_YEAR, -1)
        return DATE_FORMAT.format(cal.time)
    }

    fun formatDisplayDate(dateStr: String): String {
        return try {
            val date = DATE_FORMAT.parse(dateStr)
            if (date != null) {
                val today = getTodayString()
                val yesterday = getYesterdayString()
                when (dateStr) {
                    today -> "Today (${DISPLAY_FORMAT.format(date)})"
                    yesterday -> "Yesterday (${DISPLAY_FORMAT.format(date)})"
                    else -> DISPLAY_FORMAT.format(date)
                }
            } else {
                dateStr
            }
        } catch (_: Exception) {
            dateStr
        }
    }

    fun isWithinRange(dateStr: String, filter: DateFilter, customStart: String, customEnd: String): Boolean {
        if (filter == DateFilter.ALL_TIME) return true
        val today = getTodayString()
        val yesterday = getYesterdayString()

        when (filter) {
            DateFilter.TODAY -> return dateStr == today
            DateFilter.YESTERDAY -> return dateStr == yesterday
            DateFilter.THIS_WEEK -> {
                val cal = Calendar.getInstance()
                cal.firstDayOfWeek = Calendar.MONDAY
                cal.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
                cal.set(Calendar.HOUR_OF_DAY, 0)
                cal.set(Calendar.MINUTE, 0)
                cal.set(Calendar.SECOND, 0)
                val startOfWeek = DATE_FORMAT.format(cal.time)
                return dateStr >= startOfWeek && dateStr <= today
            }
            DateFilter.THIS_MONTH -> {
                val cal = Calendar.getInstance()
                cal.set(Calendar.DAY_OF_MONTH, 1)
                val startOfMonth = DATE_FORMAT.format(cal.time)
                return dateStr >= startOfMonth && dateStr <= today
            }
            DateFilter.CUSTOM_RANGE -> {
                if (customStart.isNotBlank() && dateStr < customStart) return false
                if (customEnd.isNotBlank() && dateStr > customEnd) return false
                return true
            }
            DateFilter.ALL_TIME -> return true
        }
    }
}

enum class DateFilter(val label: String) {
    TODAY("Today"),
    YESTERDAY("Yesterday"),
    THIS_WEEK("This Week"),
    THIS_MONTH("This Month"),
    ALL_TIME("All Time"),
    CUSTOM_RANGE("Custom Range")
}
