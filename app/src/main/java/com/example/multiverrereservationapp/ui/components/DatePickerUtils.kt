package com.example.multiverrereservationapp.ui.components

import androidx.compose.material3.*
import androidx.compose.runtime.*
import java.time.*
import java.time.format.TextStyle
import java.util.Locale
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.rememberDatePickerState

fun LocalDate.formatReadable(): String {
    val dayOfWeek = this.dayOfWeek.getDisplayName(TextStyle.SHORT, Locale.getDefault())
    val month = this.month.getDisplayName(TextStyle.FULL, Locale.getDefault())
    return "$dayOfWeek ${this.dayOfMonth} $month ${this.year}"
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerWithLimit(
    selectedDate: LocalDate?,
    onDateSelected: (LocalDate) -> Unit,
    onDismiss: () -> Unit
) {
    val today = LocalDate.now()
    val twoWeeksLater = today.plusDays(14)

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = selectedDate?.toEpochDay()?.times(24 * 60 * 60 * 1000),
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = {
                val millis = datePickerState.selectedDateMillis
                if (millis != null) {
                    val date = Instant.ofEpochMilli(millis)
                        .atZone(ZoneId.systemDefault())
                        .toLocalDate()
                    if (date in today..twoWeeksLater) {
                        onDateSelected(date)
                        onDismiss()
                    }
                }
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Annuler")
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}
