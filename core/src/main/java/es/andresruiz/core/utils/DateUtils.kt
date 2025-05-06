package es.andresruiz.core.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

fun formatDateToDisplay(date: String): String {
    return try {
        val inputFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val outputFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
        val parsedDate = inputFormat.parse(date)
        parsedDate?.let {
            val formatted = outputFormat.format(it)
            // Primera letra del mes en mayúscula
            formatted.split(" ").joinToString(" ") { part ->
                if (part.length == 3) {
                    part.replaceFirstChar { char ->
                        char.titlecase(Locale.getDefault())
                    }
                } else {
                    part
                }
            }
        } ?: date
    } catch (e: Exception) {
        date // Devuelve la fecha original si hay error
    }
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

fun convertDateStringToMillis(dateString: String): Long? {
    return try {
        val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        formatter.parse(dateString)?.time
    } catch (e: Exception) {
        null
    }
}

fun getCurrentDateInMillis(): Long {
    return Calendar.getInstance().timeInMillis
}

fun isDateAfter(date1: String, date2: String): Boolean {
    val millis1 = convertDateStringToMillis(date1)
    val millis2 = convertDateStringToMillis(date2)

    return if (millis1 != null && millis2 != null) {
        millis1 > millis2
    } else {
        false
    }
}