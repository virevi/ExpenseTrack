package com.example.expensetrack.util

import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.*

object DateUtils {

    private val displayFormatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    private val shortDateFormatter = SimpleDateFormat("MMM dd", Locale.getDefault())

    fun formatDisplayDateTime(dateTime: LocalDateTime): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            dateTime.format(DateTimeFormatter.ofPattern("dd/MM/yyyy"))
        } else {
            // Fallback for older devices
            displayFormatter.format(Date())
        }
    }

    fun formatDisplayDateTime(date: Date): String {
        return displayFormatter.format(date)
    }

    fun formatShortDate(dateTime: LocalDateTime): String {
        return if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            dateTime.format(DateTimeFormatter.ofPattern("MMM dd"))
        } else {
            shortDateFormatter.format(Date())
        }
    }

    fun formatShortDate(date: Date): String {
        return shortDateFormatter.format(date)
    }
}
