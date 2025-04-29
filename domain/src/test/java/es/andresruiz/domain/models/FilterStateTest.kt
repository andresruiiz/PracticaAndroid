package es.andresruiz.domain.models

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

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
}