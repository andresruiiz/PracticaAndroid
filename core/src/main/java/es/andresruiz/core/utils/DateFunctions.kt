package es.andresruiz.core.utils

import java.text.SimpleDateFormat
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