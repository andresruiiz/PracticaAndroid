package es.andresruiz.core.utils

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Locale

class DateUtilsTest {

    @Test
    fun formatDateToDisplay_validDate_formatsCorrectly() {
        // Arrange
        val inputDate = "12/04/2025"
        val originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag("es")) // Para que el mes sea "abr"

        // Act
        val result = formatDateToDisplay(inputDate)

        // Assert
        assertEquals("12 Abr 2025", result)

        // Clean up
        Locale.setDefault(originalLocale)
    }

    @Test
    fun formatDateToDisplay_invalidDate_returnsOriginalString() {
        // Arrange
        val invalidDate = "not-a-date"

        // Act
        val result = formatDateToDisplay(invalidDate)

        // Assert
        assertEquals(invalidDate, result)
    }

    @Test
    fun formatDateToDisplay_emptyString_returnsEmptyString() {
        // Arrange
        val emptyDate = ""

        // Act
        val result = formatDateToDisplay(emptyDate)

        // Assert
        assertEquals("", result)
    }

    @Test
    fun formatDateToDisplay_monthNeedsCapitalization_capitalizesCorrectly() {
        // Arrange
        val inputDate = "01/01/2025"
        val originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag("es")) // "ene" → "Ene"

        // Act
        val result = formatDateToDisplay(inputDate)

        // Assert
        assertEquals("01 Ene 2025", result)

        // Clean up
        Locale.setDefault(originalLocale)
    }

    @Test
    fun formatDateToDisplay_monthAlreadyCapitalized_remainsCorrect() {
        // Arrange
        val inputDate = "01/01/2025"
        val originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.US) // "Jan" ya capitalizado

        // Act
        val result = formatDateToDisplay(inputDate)

        // Assert
        assertEquals("01 Jan 2025", result)

        // Clean up
        Locale.setDefault(originalLocale)
    }

    @Test
    fun formatDateToDisplay_differentDayAndYear_formatsCorrectly() {
        // Arrange
        val inputDate = "31/12/2025"
        val originalLocale = Locale.getDefault()
        Locale.setDefault(Locale.forLanguageTag("es")) // "dic" → "Dic"

        // Act
        val result = formatDateToDisplay(inputDate)

        // Assert
        assertEquals("31 Dic 2025", result)

        // Clean up
        Locale.setDefault(originalLocale)
    }

    @Test
    fun convertMillisToDate_positiveMillis_formatsCorrectly() {
        // Arrange
        val millis = 1735689600000L // 01/01/2025

        // Act
        val result = convertMillisToDate(millis)

        // Assert
        assertEquals("01/01/2025", result)
    }

    @Test
    fun convertMillisToDate_zeroMillis_formatsEpochStart() {
        // Arrange
        val zeroMillis = 0L // 01/01/1970

        // Act
        val result = convertMillisToDate(zeroMillis)

        // Assert
        assertEquals("01/01/1970", result)
    }

    @Test
    fun convertMillisToDate_negativeMillis_formatsPreEpochDate() {
        // Arrange
        val negativeMillis = -86400000L // 31/12/1969

        // Act
        val result = convertMillisToDate(negativeMillis)

        // Assert
        assertEquals("31/12/1969", result)
    }
}