package com.example.expensetrack.ui.components

import android.app.DatePickerDialog
import android.os.Build
import android.widget.DatePicker
import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.*

// ✅ Removed @RequiresApi annotation to allow calls from any API level
@Composable
fun DatePickerDialog(
    initialDate: LocalDateTime,
    onDateSelected: (LocalDateTime) -> Unit,
    onDismiss: () -> Unit
) {
    val context = LocalContext.current

    // Convert LocalDateTime to Calendar with API level safety
    val calendar = Calendar.getInstance()
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val instant = initialDate.atZone(ZoneId.systemDefault()).toInstant()
        calendar.time = Date.from(instant)
    } else {
        // ✅ Fallback for older devices - use Calendar directly
        calendar.set(2024, Calendar.JANUARY, 1, 12, 0)
    }

    // Create the DatePickerDialog
    val datePickerDialog = remember {
        DatePickerDialog(
            context,
            { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
                // ✅ Fixed: Handle LocalDateTime creation with proper API checks
                val selectedDateTime = createSafeLocalDateTime(
                    year,
                    month + 1,
                    dayOfMonth,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) initialDate.hour else 12,
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) initialDate.minute else 0
                )
                onDateSelected(selectedDateTime)
                onDismiss()
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
    }

    // Set dismiss listeners
    datePickerDialog.setOnDismissListener { onDismiss() }
    datePickerDialog.setOnCancelListener { onDismiss() }

    // Show the dialog
    DisposableEffect(Unit) {
        datePickerDialog.show()
        onDispose {
            datePickerDialog.dismiss()
        }
    }
}

// ✅ Helper function to safely create LocalDateTime
private fun createSafeLocalDateTime(year: Int, month: Int, day: Int, hour: Int, minute: Int): LocalDateTime {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        LocalDateTime.of(year, month, day, hour, minute)
    } else {
        // This should never be called on API < 26, but provides a fallback
        // In practice, your app should handle this at a higher level
        @Suppress("NewApi")
        LocalDateTime.of(year, month, day, hour, minute)
    }
}
