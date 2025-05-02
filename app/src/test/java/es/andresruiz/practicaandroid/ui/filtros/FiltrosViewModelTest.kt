package es.andresruiz.practicaandroid.ui.filtros

import es.andresruiz.domain.models.FilterState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class FiltrosViewModelTest {

    private lateinit var viewModel: FiltrosViewModel
    private lateinit var filterManager: FilterManager

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    // Datos de prueba
    private val initialFilterState = FilterState(
        fechaDesde = "",
        fechaHasta = "",
        importeMin = 10,
        importeMax = 200,
        estados = mapOf(
            "Pagada" to false,
            "Anulada" to false,
            "Cuota Fija" to true,
            "Pendiente de pago" to false,
            "Plan de pago" to false
        )
    )

    private val minImporteFlow = MutableStateFlow(5)
    private val maxImporteFlow = MutableStateFlow(250)

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Inicializar mocks
        filterManager = mock()

        // Configuración por defecto de los mocks
        whenever(filterManager.getCurrentFilter()).thenReturn(initialFilterState)
        whenever(filterManager.dataMinImporte).thenReturn(minImporteFlow)
        whenever(filterManager.dataMaxImporte).thenReturn(maxImporteFlow)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun filtrosViewModel_init_LoadsCurrentFilter() = runTest {
        // Arrange & Act
        viewModel = FiltrosViewModel(filterManager)

        // Assert
        assertEquals("", viewModel.fechaDesde.value)
        assertEquals("", viewModel.fechaHasta.value)
        assertEquals(10, viewModel.importeMin.value)
        assertEquals(200, viewModel.importeMax.value)
        assertEquals(initialFilterState.estados, viewModel.estados.value)
    }

    @Test
    fun filtrosViewModel_setFechaDesde_UpdatesStateCorrectly() = runTest {
        // Arrange
        viewModel = FiltrosViewModel(filterManager)
        val newFecha = "01/05/2023"

        // Act
        viewModel.setFechaDesde(newFecha)

        // Assert
        assertEquals(newFecha, viewModel.fechaDesde.value)
    }

    @Test
    fun filtrosViewModel_setFechaHasta_UpdatesStateCorrectly() = runTest {
        // Arrange
        viewModel = FiltrosViewModel(filterManager)
        val newFecha = "31/05/2023"

        // Act
        viewModel.setFechaHasta(newFecha)

        // Assert
        assertEquals(newFecha, viewModel.fechaHasta.value)
    }

    @Test
    fun setImporteMin_WhenLessThanMax_UpdatesMinOnly() = runTest {
        // Arrange
        viewModel = FiltrosViewModel(filterManager)
        val newMin = 50 // Menor que el máximo actual (200)

        // Act
        viewModel.setImporteMin(newMin)

        // Assert
        assertEquals(newMin, viewModel.importeMin.value)
        assertEquals(200, viewModel.importeMax.value) // Max no cambia
    }

    @Test
    fun setImporteMin_WhenGreaterThanMax_UpdatesBoth() = runTest {
        // Arrange
        viewModel = FiltrosViewModel(filterManager)
        val newMin = 250 // Mayor que el máximo actual (200)

        // Act
        viewModel.setImporteMin(newMin)

        // Assert
        assertEquals(newMin, viewModel.importeMin.value)
        assertEquals(newMin, viewModel.importeMax.value) // Max se ajusta al nuevo min
    }

    @Test
    fun setImporteMax_WhenGreaterThanMin_UpdatesMaxOnly() = runTest {
        // Arrange
        viewModel = FiltrosViewModel(filterManager)
        val newMax = 150 // Mayor que el mínimo actual (10)

        // Act
        viewModel.setImporteMax(newMax)

        // Assert
        assertEquals(10, viewModel.importeMin.value) // Min no cambia
        assertEquals(newMax, viewModel.importeMax.value)
    }

    @Test
    fun setImporteMax_WhenLessThanMin_UpdatesBoth() = runTest {
        // Arrange
        viewModel = FiltrosViewModel(filterManager)
        val newMax = 5 // Menor que el mínimo actual (10)

        // Act
        viewModel.setImporteMax(newMax)

        // Assert
        assertEquals(newMax, viewModel.importeMin.value) // Min se ajusta al nuevo max
        assertEquals(newMax, viewModel.importeMax.value)
    }

    @Test
    fun toggleEstado_WhenFalse_SetsToTrue() = runTest {
        // Arrange
        viewModel = FiltrosViewModel(filterManager)
        val estadoToToggle = "Pagada" // Inicialmente es false

        // Act
        viewModel.toggleEstado(estadoToToggle)

        // Assert
        assertTrue(viewModel.estados.value[estadoToToggle]!!)
    }

    @Test
    fun toggleEstado_WhenTrue_SetsToFalse() = runTest {
        // Arrange
        viewModel = FiltrosViewModel(filterManager)
        val estadoToToggle = "Cuota Fija" // Inicialmente es true

        // Act
        viewModel.toggleEstado(estadoToToggle)

        // Assert
        assertFalse(viewModel.estados.value[estadoToToggle]!!)
    }

    @Test
    fun filtrosViewModel_limpiarFiltros_ResetsAllFilters() = runTest {
        // Arrange
        val clearedFilterState = FilterState(
            importeMin = 5,
            importeMax = 250
        )
        whenever(filterManager.getCurrentFilter()).thenReturn(clearedFilterState)
        viewModel = FiltrosViewModel(filterManager)

        // Act
        viewModel.limpiarFiltros()

        // Assert
        verify(filterManager).clearFilters()
        assertEquals("", viewModel.fechaDesde.value)
        assertEquals("", viewModel.fechaHasta.value)
        assertEquals(5, viewModel.importeMin.value)
        assertEquals(250, viewModel.importeMax.value)
        assertEquals(clearedFilterState.estados, viewModel.estados.value)
    }

    @Test
    fun filtrosViewModel_aplicarFiltros_SavesCurrentState() = runTest {
        // Arrange
        viewModel = FiltrosViewModel(filterManager)

        // Modificamos algunos valores antes de aplicar
        viewModel.setFechaDesde("01/06/2023")
        viewModel.setFechaHasta("30/06/2023")
        viewModel.setImporteMin(20)
        viewModel.setImporteMax(180)
        viewModel.toggleEstado("Pagada")

        val expectedFilterState = FilterState(
            fechaDesde = "01/06/2023",
            fechaHasta = "30/06/2023",
            importeMin = 20,
            importeMax = 180,
            estados = mapOf(
                "Pagada" to true,
                "Anulada" to false,
                "Cuota Fija" to true,
                "Pendiente de pago" to false,
                "Plan de pago" to false
            )
        )

        // Act
        viewModel.aplicarFiltros()

        // Assert - Verificamos que se guardó un FilterState con los valores correctos
        verify(filterManager).saveFilter(expectedFilterState)
    }
}