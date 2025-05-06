package es.andresruiz.domain.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.text.SimpleDateFormat
import java.util.*

/**
 * Pruebas unitarias de la data class FilterState
 * Uso el patrón AAA para facilitar la comprensión de las pruebas
 */
class FilterStateTest {

    @Test
    fun filterState_applyNoFilters_returnsAllFacturas() {
        // Arrange
        val filterState = FilterState()
        val facturas = listOf(
            Factura("Pagada", 100.0, "01/01/2025"),
            Factura("Anulada", 200.0, "15/02/2025"),
            Factura("Pendiente de pago", 50.0, "10/03/2025")
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(3, result.size)
        assertEquals(facturas, result)
    }

    @Test
    fun filterState_applyImporteFilter_returnsMatchingFacturas() {
        // Arrange
        val filterState = FilterState(
            importeMin = 100,
            importeMax = 200
        )
        val facturas = listOf(
            Factura("Pagada", 50.0, "01/01/2025"),
            Factura("Anulada", 150.0, "15/02/2025"),
            Factura("Pendiente de pago", 250.0, "10/03/2025")
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(1, result.size)
        assertEquals(150.0, result[0].importeOrdenacion, 0.01)
    }

    @Test
    fun filterState_applyEstadoFilter_returnsMatchingFacturas() {
        // Arrange
        val filterState = FilterState(
            estados = mapOf(
                "Pagada" to true,
                "Anulada" to false,
                "Cuota Fija" to false,
                "Pendiente de pago" to false,
                "Plan de pago" to false
            )
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "01/01/2025"),
            Factura("Anulada", 200.0, "15/02/2025"),
            Factura("Pendiente de pago", 150.0, "10/03/2025")
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(1, result.size)
        assertEquals("Pagada", result[0].descEstado)
    }

    @Test
    fun filterState_applyFechaDesdeFilter_returnsMatchingFacturas() {
        // Arrange
        val filterState = FilterState(
            fechaDesde = "15/02/2025"
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "01/01/2025"),
            Factura("Anulada", 200.0, "15/02/2025"),
            Factura("Pendiente de pago", 150.0, "10/03/2025")
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(2, result.size)
        assertTrue(result.any { it.fecha == "15/02/2025" })
        assertTrue(result.any { it.fecha == "10/03/2025" })
    }

    @Test
    fun filterState_applyFechaHastaFilter_returnsMatchingFacturas() {
        // Arrange
        val filterState = FilterState(
            fechaHasta = "15/02/2025"
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "01/01/2025"),
            Factura("Anulada", 200.0, "15/02/2025"),
            Factura("Pendiente de pago", 150.0, "10/03/2025")
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(2, result.size)
        assertTrue(result.any { it.fecha == "01/01/2025" })
        assertTrue(result.any { it.fecha == "15/02/2025" })
    }

    @Test
    fun filterState_applyMultipleFilters_returnsMatchingFacturas() {
        // Arrange
        val filterState = FilterState(
            fechaDesde = "15/01/2025",
            fechaHasta = "20/02/2025",
            importeMin = 150,
            importeMax = 250,
            estados = mapOf(
                "Pagada" to false,
                "Anulada" to true,
                "Cuota Fija" to false,
                "Pendiente de pago" to false,
                "Plan de pago" to false
            )
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "01/01/2025"),
            Factura("Anulada", 200.0, "15/02/2025"),
            Factura("Pendiente de pago", 150.0, "10/03/2025"),
            Factura("Anulada", 100.0, "16/01/2025")
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(1, result.size)
        assertEquals("Anulada", result[0].descEstado)
        assertEquals(200.0, result[0].importeOrdenacion, 0.01)
        assertEquals("15/02/2025", result[0].fecha)
    }

    @Test
    fun filterState_applyInvalidDates_handlesErrorCorrectly() {
        // Arrange
        val filterState = FilterState(
            fechaDesde = "invalid-date",
            fechaHasta = "also-invalid"
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "01/01/2025"),
            Factura("Anulada", 200.0, "15/02/2025")
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(2, result.size) // No se aplican filtros por fechas inválidas
    }

    @Test
    fun filterState_fechaDesdeWithNullFacturaDate_facturaIsNotFiltered() {
        // Arrange
        val filterState = FilterState(
            fechaDesde = "01/01/2025"
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "malformato-fecha") // Fecha con formato incorrecto
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(1, result.size) // La factura no se filtra a pesar del formato incorrecto
    }

    @Test
    fun filterState_fechaDesdeWithNullDesdeDate_facturaIsNotFiltered() {
        // Arrange
        val filterState = FilterState(
            fechaDesde = "formato-incorrecto" // Fecha de filtro con formato incorrecto
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "01/01/2025")
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(1, result.size) // No se aplica filtro porque la fecha de filtro es inválida
    }

    @Test
    fun filterState_fechaHastaWithNullFacturaDate_facturaIsNotFiltered() {
        // Arrange
        val filterState = FilterState(
            fechaHasta = "31/12/2025"
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "malformato-fecha") // Fecha con formato incorrecto
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(1, result.size) // La factura no se filtra a pesar del formato incorrecto
    }

    @Test
    fun filterState_fechaHastaWithNullHastaDate_facturaIsNotFiltered() {
        // Arrange
        val filterState = FilterState(
            fechaHasta = "formato-incorrecto" // Fecha de filtro con formato incorrecto
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "01/01/2025")
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(1, result.size) // No se aplica filtro porque la fecha de filtro es inválida
    }

    @Test
    fun filterState_facturaDateBeforeDesdeDate_facturaIsFiltered() {
        // Arrange
        val filterState = FilterState(
            fechaDesde = "15/01/2025"
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "01/01/2025") // Fecha anterior a fechaDesde
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(0, result.size) // La factura se filtra porque es anterior a la fecha desde
    }

    @Test
    fun filterState_facturaDateAfterHastaDate_facturaIsFiltered() {
        // Arrange
        val filterState = FilterState(
            fechaHasta = "15/01/2025"
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "01/02/2025") // Fecha posterior a fechaHasta
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(0, result.size) // La factura se filtra porque es posterior a la fecha hasta
    }

    @Test
    fun filterState_facturaDateEqualsDesdeDate_facturaIsIncluded() {
        // Arrange
        val filterState = FilterState(
            fechaDesde = "01/01/2025"
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "01/01/2025") // Fecha igual a fechaDesde
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(1, result.size) // La factura se incluye porque es igual a la fecha desde
    }

    @Test
    fun filterState_facturaDateEqualsHastaDate_facturaIsIncluded() {
        // Arrange
        val filterState = FilterState(
            fechaHasta = "01/01/2025"
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "01/01/2025") // Fecha igual a fechaHasta
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(1, result.size) // La factura se incluye porque es igual a la fecha hasta
    }

    @Test
    fun filterState_fechaFacturaDateNullFechaDesdeFilter_returnsAllFacturas() {
        // Arrange
        val filterState = FilterState(
            fechaDesde = "01/01/2025"
        )
        val facturas = listOf(
            // Formato que el SimpleDateFormat no puede parsear
            Factura("Pagada", 100.0, "2025.01.01")
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(1, result.size) // No se filtra porque fechaFacturaDate es null
    }

    @Test
    fun filterState_fechaDesdeDateNullFechaDesdeFilter_returnsAllFacturas() {
        // Arrange
        val filterState = FilterState(
            // Formato que el SimpleDateFormat no puede parsear
            fechaDesde = "2025.01.01"
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "01/01/2025")
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(1, result.size) // No se filtra porque fechaDesdeDate es null
    }

    @Test
    fun filterState_fechaFacturaDateLessThanFechaDesdeDate_facturaIsFiltered() {
        // Arrange
        val filterState = FilterState(
            fechaDesde = "15/01/2025"
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "14/01/2025") // Un día antes
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(0, result.size) // Se filtra porque la fecha es anterior a fechaDesde
    }

    @Test
    fun filterState_fechaFacturaDateEqualToFechaDesdeDate_facturaIsNotFiltered() {
        // Arrange
        val filterState = FilterState(
            fechaDesde = "15/01/2025"
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "15/01/2025") // Misma fecha
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(1, result.size) // No se filtra porque la fecha es igual
    }

    @Test
    fun filterState_fechaFacturaDateGreaterThanFechaDesdeDate_facturaIsNotFiltered() {
        // Arrange
        val filterState = FilterState(
            fechaDesde = "15/01/2025"
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "16/01/2025") // Un día después
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(1, result.size) // No se filtra porque la fecha es posterior
    }

    @Test
    fun filterState_fechaFacturaDateNullFechaHastaFilter_returnsAllFacturas() {
        // Arrange
        val filterState = FilterState(
            fechaHasta = "31/01/2025"
        )
        val facturas = listOf(
            // Formato que el SimpleDateFormat no puede parsear
            Factura("Pagada", 100.0, "2025.01.31")
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(1, result.size) // No se filtra porque fechaFacturaDate es null
    }

    @Test
    fun filterState_fechaHastaDateNullFechaHastaFilter_returnsAllFacturas() {
        // Arrange
        val filterState = FilterState(
            // Formato que el SimpleDateFormat no puede parsear
            fechaHasta = "2025.01.31"
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "31/01/2025")
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(1, result.size) // No se filtra porque fechaHastaDate es null
    }

    @Test
    fun filterState_fechaFacturaDateGreaterThanFechaHastaDate_facturaIsFiltered() {
        // Arrange
        val filterState = FilterState(
            fechaHasta = "15/01/2025"
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "16/01/2025") // Un día después
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(0, result.size) // Se filtra porque la fecha es posterior a fechaHasta
    }

    @Test
    fun filterState_fechaFacturaDateEqualToFechaHastaDate_facturaIsNotFiltered() {
        // Arrange
        val filterState = FilterState(
            fechaHasta = "15/01/2025"
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "15/01/2025") // Misma fecha
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(1, result.size) // No se filtra porque la fecha es igual
    }

    @Test
    fun filterState_fechaFacturaDateLessThanFechaHastaDate_facturaIsNotFiltered() {
        // Arrange
        val filterState = FilterState(
            fechaHasta = "15/01/2025"
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "14/01/2025") // Un día antes
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(1, result.size) // No se filtra porque la fecha es anterior
    }

    @Test
    fun filterState_combinedDateFiltersWithValidDates_returnsFilteredFacturas() {
        // Arrange
        val filterState = FilterState(
            fechaDesde = "15/01/2025",
            fechaHasta = "15/02/2025"
        )
        val facturas = listOf(
            Factura("Pagada", 100.0, "01/01/2025"), // Antes de fechaDesde
            Factura("Anulada", 200.0, "20/01/2025"), // Entre ambas fechas
            Factura("Pendiente de pago", 150.0, "20/02/2025") // Después de fechaHasta
        )

        // Act
        val result = filterState.aplicarFiltros(facturas)

        // Assert
        assertEquals(1, result.size)
        assertEquals("20/01/2025", result[0].fecha)
    }
}