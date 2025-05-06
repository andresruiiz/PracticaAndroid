package es.andresruiz.practicaandroid.ui.filtros

import es.andresruiz.domain.models.FilterState
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

@ExperimentalCoroutinesApi
class FilterManagerTest {

    private lateinit var filterManager: FilterManager

    @Before
    fun setUp() {
        filterManager = FilterManager()
    }

    @Test
    fun getCurrentFilter_InitialState_ReturnsDefaultFilterState() = runTest {
        // Arrange & Act
        val currentFilter = filterManager.getCurrentFilter()

        // Assert
        assertEquals("", currentFilter.fechaDesde)
        assertEquals("", currentFilter.fechaHasta)
        assertEquals(1, currentFilter.importeMin)
        assertEquals(300, currentFilter.importeMax)
        assertEquals(5, currentFilter.estados.size)
        assertFalse(currentFilter.estados["Pagada"]!!)
        assertFalse(currentFilter.estados["Anulada"]!!)
        assertFalse(currentFilter.estados["Cuota Fija"]!!)
        assertFalse(currentFilter.estados["Pendiente de pago"]!!)
        assertFalse(currentFilter.estados["Plan de pago"]!!)
    }

    @Test
    fun filterManager_saveFilter_UpdatesFilterState() = runTest {
        // Arrange
        val newFilterState = FilterState(
            fechaDesde = "01/01/2023",
            fechaHasta = "31/12/2023",
            importeMin = 50,
            importeMax = 150,
            estados = mapOf(
                "Pagada" to true,
                "Anulada" to false,
                "Cuota Fija" to true,
                "Pendiente de pago" to false,
                "Plan de pago" to false
            )
        )

        // Act
        filterManager.saveFilter(newFilterState)

        // Assert
        val currentFilter = filterManager.getCurrentFilter()
        assertEquals(newFilterState, currentFilter)
        assertTrue(filterManager.filtersApplied.first())
    }

    @Test
    fun filterManager_clearFilters_ResetsFilterStateToDefault() = runTest {
        // Arrange
        val customFilter = FilterState(
            fechaDesde = "01/01/2023",
            fechaHasta = "31/12/2023",
            importeMin = 50,
            importeMax = 150,
            estados = mapOf(
                "Pagada" to true,
                "Anulada" to false,
                "Cuota Fija" to true,
                "Pendiente de pago" to false,
                "Plan de pago" to false
            )
        )
        filterManager.saveFilter(customFilter)

        assertEquals(customFilter, filterManager.getCurrentFilter())
        assertTrue(filterManager.filtersApplied.first())

        // Act
        filterManager.clearFilters()

        // Assert
        val clearedFilter = filterManager.getCurrentFilter()
        assertEquals("", clearedFilter.fechaDesde)
        assertEquals("", clearedFilter.fechaHasta)
        assertEquals(1, clearedFilter.importeMin)
        assertEquals(300, clearedFilter.importeMax)

        for (estado in clearedFilter.estados.values) {
            assertFalse(estado)
        }

        assertFalse(filterManager.filtersApplied.first())
    }

    @Test
    fun filterManager_updateDataBounds_UpdatesMinAndMaxImporte() = runTest {
        // Arrange
        val newMin = 25
        val newMax = 200

        // Act
        filterManager.updateDataBounds(newMin, newMax)

        // Assert
        assertEquals(newMin, filterManager.dataMinImporte.first())
        assertEquals(newMax, filterManager.dataMaxImporte.first())
    }

    @Test
    fun updateDataBounds_WhenMinIsNegative_SetsToOne() = runTest {
        // Arrange
        val newMin = -10 // Valor inválido, debería convertirse a 1
        val newMax = 200

        // Act
        filterManager.updateDataBounds(newMin, newMax)

        // Assert
        assertEquals(1, filterManager.dataMinImporte.first()) // Mínimo debe ser al menos 1
        assertEquals(newMax, filterManager.dataMaxImporte.first())
    }

    @Test
    fun updateDataBounds_WhenMaxIsLessThanMin_SetsMaxToMin() = runTest {
        // Arrange
        val newMin = 100
        val newMax = 50 // Menor que el mínimo, debería ajustarse

        // Act
        filterManager.updateDataBounds(newMin, newMax)

        // Assert
        assertEquals(newMin, filterManager.dataMinImporte.first())
        assertEquals(newMin, filterManager.dataMaxImporte.first()) // Max debería ajustarse al min
    }

    @Test
    fun saveFilter_WhenNoFilterIsActive_FiltersAppliedIsFalse() = runTest {
        // Arrange
        val noActiveFilter = FilterState(
            fechaDesde = "",
            fechaHasta = "",
            importeMin = 1, // Valor por defecto
            importeMax = 300, // Valor por defecto
            estados = mapOf(
                "Pagada" to false,
                "Anulada" to false,
                "Cuota Fija" to false,
                "Pendiente de pago" to false,
                "Plan de pago" to false
            )
        )

        // Act
        filterManager.saveFilter(noActiveFilter)

        // Assert
        assertFalse(filterManager.filtersApplied.first())
    }

    @Test
    fun saveFilter_WhenFechaDesdeIsSet_FiltersAppliedIsTrue() = runTest {
        // Arrange
        val filterWithFechaDesde = FilterState(
            fechaDesde = "01/01/2023",
            fechaHasta = "",
            importeMin = 1,
            importeMax = 300,
            estados = mapOf(
                "Pagada" to false,
                "Anulada" to false,
                "Cuota Fija" to false,
                "Pendiente de pago" to false,
                "Plan de pago" to false
            )
        )

        // Act
        filterManager.saveFilter(filterWithFechaDesde)

        // Assert
        assertTrue(filterManager.filtersApplied.first())
    }

    @Test
    fun saveFilter_WhenFechaHastaIsSet_FiltersAppliedIsTrue() = runTest {
        // Arrange
        val filterWithFechaHasta = FilterState(
            fechaDesde = "",
            fechaHasta = "31/12/2023",
            importeMin = 1,
            importeMax = 300,
            estados = mapOf(
                "Pagada" to false,
                "Anulada" to false,
                "Cuota Fija" to false,
                "Pendiente de pago" to false,
                "Plan de pago" to false
            )
        )

        // Act
        filterManager.saveFilter(filterWithFechaHasta)

        // Assert
        assertTrue(filterManager.filtersApplied.first())
    }

    @Test
    fun saveFilter_WhenImporteMinIsSet_FiltersAppliedIsTrue() = runTest {
        // Arrange
        val filterWithCustomImporteMin = FilterState(
            fechaDesde = "",
            fechaHasta = "",
            importeMin = 50, // Valor distinto al valor por defecto (1)
            importeMax = 300,
            estados = mapOf(
                "Pagada" to false,
                "Anulada" to false,
                "Cuota Fija" to false,
                "Pendiente de pago" to false,
                "Plan de pago" to false
            )
        )

        // Act
        filterManager.saveFilter(filterWithCustomImporteMin)

        // Assert
        assertTrue(filterManager.filtersApplied.first())
    }

    @Test
    fun saveFilter_WhenImporteMaxIsSet_FiltersAppliedIsTrue() = runTest {
        // Arrange
        val filterWithCustomImporteMax = FilterState(
            fechaDesde = "",
            fechaHasta = "",
            importeMin = 1,
            importeMax = 200, // Valor distinto al valor por defecto (300)
            estados = mapOf(
                "Pagada" to false,
                "Anulada" to false,
                "Cuota Fija" to false,
                "Pendiente de pago" to false,
                "Plan de pago" to false
            )
        )

        // Act
        filterManager.saveFilter(filterWithCustomImporteMax)

        // Assert
        assertTrue(filterManager.filtersApplied.first())
    }

    @Test
    fun saveFilter_WhenEstadoIsActive_FiltersAppliedIsTrue() = runTest {
        // Arrange
        val filterWithActiveEstado = FilterState(
            fechaDesde = "",
            fechaHasta = "",
            importeMin = 1,
            importeMax = 300,
            estados = mapOf(
                "Pagada" to true, // Un estado activo
                "Anulada" to false,
                "Cuota Fija" to false,
                "Pendiente de pago" to false,
                "Plan de pago" to false
            )
        )

        // Act
        filterManager.saveFilter(filterWithActiveEstado)

        // Assert
        assertTrue(filterManager.filtersApplied.first())
    }
}