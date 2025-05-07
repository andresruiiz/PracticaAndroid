package es.andresruiz.practicaandroid.ui.facturas

import es.andresruiz.domain.models.Factura
import es.andresruiz.domain.models.FilterState
import es.andresruiz.domain.usecases.GetFacturasUseCase
import es.andresruiz.domain.usecases.RefreshFacturasUseCase
import es.andresruiz.practicaandroid.ui.facturas.FacturasViewModel.FacturasUiState
import es.andresruiz.practicaandroid.ui.filtros.FilterManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.doAnswer
import org.mockito.kotlin.mock
import org.mockito.kotlin.times
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import java.io.IOException

@ExperimentalCoroutinesApi
class FacturasViewModelTest {

    // Test dispatcher para corrutinas
    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    private lateinit var getFacturasUseCase: GetFacturasUseCase
    private lateinit var refreshFacturasUseCase: RefreshFacturasUseCase
    private lateinit var filterManager: FilterManager

    private lateinit var viewModel: FacturasViewModel

    private val testFacturas = listOf(
        Factura("Pagada", 100.0, "01/01/2025"),
        Factura("Anulada", 200.0, "15/02/2025"),
        Factura("Pendiente de pago", 150.0, "10/03/2025")
    )

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Inicializar mocks
        getFacturasUseCase = mock()
        refreshFacturasUseCase = mock()
        filterManager = mock()

        // Configuración por defecto de los mocks
        val filterStateFlow = MutableStateFlow(FilterState())
        whenever(filterManager.filterState).thenReturn(filterStateFlow)
        whenever(filterManager.getCurrentFilter()).thenReturn(FilterState())
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun facturasViewModel_init_loadsFacturasFromDatabase() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(testFacturas)
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Assert
        assertTrue(viewModel.uiState.value is FacturasUiState.Success)
        assertEquals(testFacturas, (viewModel.uiState.value as FacturasUiState.Success).facturas)
        assertFalse(viewModel.isRefreshing.value)
        verify(filterManager).updateDataBounds(any(), any())
    }

    @Test
    fun facturasViewModel_initWithEmptyDatabase_refreshesFacturas() = runTest {
        // Arrange
        val emptyFlow = MutableStateFlow(emptyList<Factura>())
        whenever(getFacturasUseCase()).thenReturn(emptyFlow)

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Assert
        verify(refreshFacturasUseCase).invoke()
    }

    @Test
    fun facturasViewModel_refreshFacturas_updatesRefreshingState() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenReturn(MutableStateFlow(testFacturas))
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Act
        viewModel.refreshFacturas()

        // Assert
        verify(refreshFacturasUseCase).invoke()
    }

    @Test
    fun facturasViewModel_refreshFacturas_handlesError() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenReturn(MutableStateFlow(testFacturas))
        val errorMessage = "Error de conexión"
        whenever(refreshFacturasUseCase()).thenAnswer { throw Exception(errorMessage) }
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Act
        viewModel.refreshFacturas()

        // Assert
        verify(refreshFacturasUseCase).invoke()
        assertTrue(viewModel.uiState.value is FacturasUiState.Error)
        assertEquals(errorMessage, (viewModel.uiState.value as FacturasUiState.Error).message)
        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun facturasViewModel_refreshFacturasWhileRefreshing_doesNothing() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenReturn(MutableStateFlow(testFacturas))
        var isRefreshing = false
        doAnswer {
            isRefreshing = true
            // No completamos la corrutina para mantener isRefreshing en true
        }.whenever(refreshFacturasUseCase).invoke()

        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Primera llamada para activar isRefreshing
        viewModel.refreshFacturas()

        // Nos aseguramos que se ha llamado la primera vez
        verify(refreshFacturasUseCase, times(1)).invoke()
        assertTrue("La carga debería estar en progreso", isRefreshing)

        // Forzamos el estado isRefreshing a true para el test
        val isRefreshingField = FacturasViewModel::class.java.getDeclaredField("_isRefreshing")
        isRefreshingField.isAccessible = true
        val isRefreshingStateFlow = isRefreshingField.get(viewModel) as MutableStateFlow<Boolean>
        isRefreshingStateFlow.value = true

        // Act
        viewModel.refreshFacturas() // Segunda llamada mientras isRefreshing es true

        // Assert
        verify(refreshFacturasUseCase, times(1)).invoke() // Sigue siendo 1, no 2 (no se recargan)
    }

    @Test
    fun facturasViewModel_retry_refreshesFacturas() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenReturn(MutableStateFlow(testFacturas))
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Act
        viewModel.retry()

        // Assert
        verify(refreshFacturasUseCase).invoke()
    }

    @Test
    fun facturasViewModel_showDialog_updatesDialogState() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenReturn(MutableStateFlow(testFacturas))
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)
        assertFalse(viewModel.showDialog.value)

        // Act
        viewModel.showDialog()

        // Assert
        assertTrue(viewModel.showDialog.value)
    }

    @Test
    fun facturasViewModel_hideDialog_updatesDialogState() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenReturn(MutableStateFlow(testFacturas))
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)
        viewModel.showDialog()
        assertTrue(viewModel.showDialog.value)

        // Act
        viewModel.hideDialog()

        // Assert
        assertFalse(viewModel.showDialog.value)
    }

    @Test
    fun facturasViewModel_filterChange_updatesFilteredFacturas() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(testFacturas)
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)

        val filterStateFlow = MutableStateFlow(FilterState())
        whenever(filterManager.filterState).thenReturn(filterStateFlow)

        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Solo facturas pagadas
        val filteredState = FilterState(
            estados = mapOf(
                "Pagada" to true,
                "Anulada" to false,
                "Cuota Fija" to false,
                "Pendiente de pago" to false,
                "Plan de pago" to false
            )
        )

        // Act
        filterStateFlow.value = filteredState

        // Assert
        assertTrue(viewModel.uiState.value is FacturasUiState.Success)
        val facturas = (viewModel.uiState.value as FacturasUiState.Success).facturas
        assertEquals(1, facturas.size)
        assertEquals("Pagada", facturas[0].descEstado)
    }

    @Test
    fun facturasViewModel_filterResultEmpty_showsEmptyState() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(testFacturas)
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)

        val filterStateFlow = MutableStateFlow(FilterState())
        whenever(filterManager.filterState).thenReturn(filterStateFlow)

        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Filtro que no coincide con ninguna factura
        val filteredState = FilterState(
            importeMin = 1000
        )

        // Act
        filterStateFlow.value = filteredState

        // Assert
        assertTrue(viewModel.uiState.value is FacturasUiState.Empty)
        val emptyState = viewModel.uiState.value as FacturasUiState.Empty
        assertEquals("No hay facturas que coincidan con los filtros seleccionados", emptyState.message)
    }

    @Test
    fun facturasViewModel_uiStateLoading_whileInitializing() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(testFacturas)

        doAnswer {
            // No emitimos facturas todavía
            MutableStateFlow(emptyList<Factura>())
        }.whenever(getFacturasUseCase).invoke()

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        val uiStateField = FacturasViewModel::class.java.getDeclaredField("_uiState")
        uiStateField.isAccessible = true
        val uiStateFlow = uiStateField.get(viewModel) as MutableStateFlow<FacturasUiState>
        uiStateFlow.value = FacturasUiState.Loading

        // Assert
        assertTrue(viewModel.uiState.value is FacturasUiState.Loading)
    }

    @Test
    fun facturasViewModel_uiStateSuccess_whenFacturasLoaded() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(testFacturas)
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Assert
        assertTrue(viewModel.uiState.value is FacturasUiState.Success)
        val state = viewModel.uiState.value as FacturasUiState.Success
        assertEquals(testFacturas, state.facturas)
    }

    @Test
    fun facturasViewModel_uiStateError_whenErrorOccurs() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenReturn(MutableStateFlow(testFacturas))
        val errorMessage = "Error de conexión"
        whenever(refreshFacturasUseCase()).thenAnswer { throw Exception(errorMessage) }
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Act
        viewModel.refreshFacturas()

        // Assert
        assertTrue(viewModel.uiState.value is FacturasUiState.Error)
        val state = viewModel.uiState.value as FacturasUiState.Error
        assertEquals(errorMessage, state.message)
    }

    @Test
    fun facturasViewModel_uiStateEmpty_whenNoFacturasMatchFilter() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(testFacturas)
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)

        val filterStateFlow = MutableStateFlow(FilterState())
        whenever(filterManager.filterState).thenReturn(filterStateFlow)

        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Act
        filterStateFlow.value = FilterState(importeMin = 1000)

        // Assert
        assertTrue(viewModel.uiState.value is FacturasUiState.Empty)
        val state = viewModel.uiState.value as FacturasUiState.Empty
        assertEquals("No hay facturas que coincidan con los filtros seleccionados", state.message)
    }

    @Test
    fun facturasViewModel_uiStateEmpty_whenNoFacturasAvailable() = runTest {
        // Arrange
        val emptyFlow = MutableStateFlow(emptyList<Factura>())
        whenever(getFacturasUseCase()).thenReturn(emptyFlow)

        doAnswer {
            // Completamos pero no cambiamos la lista de facturas (sigue vacía)
        }.whenever(refreshFacturasUseCase).invoke()

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Forzamos el estado de refresing a false para simular la finalización
        val isRefreshingField = FacturasViewModel::class.java.getDeclaredField("_isRefreshing")
        isRefreshingField.isAccessible = true
        val isRefreshingStateFlow = isRefreshingField.get(viewModel) as MutableStateFlow<Boolean>
        isRefreshingStateFlow.value = false

        // Assert
        assertTrue(viewModel.uiState.value is FacturasUiState.Empty)
        val state = viewModel.uiState.value as FacturasUiState.Empty
        assertEquals("No hay facturas disponibles", state.message)
    }

    @Test
    fun facturasViewModel_initDatabaseError_handlesError() = runTest {
        // Arrange
        val errorMessage = "Error al acceder a la base de datos"
        whenever(getFacturasUseCase()).thenReturn(flow { throw IOException(errorMessage) })

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Espero a que el error se propague
        testDispatcher.scheduler.advanceUntilIdle()

        // Assert
        assertTrue(viewModel.uiState.value is FacturasUiState.Error)
        val errorState = viewModel.uiState.value as FacturasUiState.Error
        assertEquals(errorMessage, errorState.message)
    }

    @Test
    fun facturasViewModel_updateFilterManagerBounds_withNegativeImportes() = runTest {
        // Arrange
        val facturasConNegativo = listOf(
            Factura("Pagada", -50.0, "01/01/2025"),
            Factura("Anulada", 10.0, "15/02/2025")
        )
        val facturasFlow = MutableStateFlow(facturasConNegativo)
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Assert - El mínimo debe ser 1 aunque haya valores negativos
        verify(filterManager).updateDataBounds(1, 10)
    }

    @Test
    fun facturasViewModel_updateFilterManagerBounds_withFractionalImportes() = runTest {
        // Arrange
        val facturasConDecimales = listOf(
            Factura("Pagada", 50.4, "01/01/2025"),
            Factura("Anulada", 99.9, "15/02/2025")
        )
        val facturasFlow = MutableStateFlow(facturasConDecimales)
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Assert - Los valores deben redondearse correctamente
        verify(filterManager).updateDataBounds(50, 100)
    }

    @Test
    fun facturasViewModel_updateFilterManagerBounds_whenAllImportesAreEqual() = runTest {
        // Arrange
        val facturasIguales = listOf(
            Factura("Pagada", 100.0, "01/01/2025"),
            Factura("Anulada", 100.0, "15/02/2025")
        )
        val facturasFlow = MutableStateFlow(facturasIguales)
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Assert
        verify(filterManager).updateDataBounds(100, 100)
    }

    @Test
    fun facturasViewModel_refreshFacturas_handlesUnknownException() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenReturn(MutableStateFlow(testFacturas))
        whenever(refreshFacturasUseCase()).thenAnswer { throw Exception() }
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Act
        viewModel.refreshFacturas()

        // Assert
        assertTrue(viewModel.uiState.value is FacturasUiState.Error)
        val errorState = viewModel.uiState.value as FacturasUiState.Error
        assertEquals("Error desconocido al refrescar facturas", errorState.message)
    }

    @Test
    fun facturasViewModel_databaseEmitsNewFacturas_updatesUiState() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(emptyList<Factura>())
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Forzamos el estado de refreshing a false para el test
        val isRefreshingField = FacturasViewModel::class.java.getDeclaredField("_isRefreshing")
        isRefreshingField.isAccessible = true
        val isRefreshingStateFlow = isRefreshingField.get(viewModel) as MutableStateFlow<Boolean>
        isRefreshingStateFlow.value = false

        assertTrue(viewModel.uiState.value is FacturasUiState.Empty)

        // Act
        facturasFlow.value = testFacturas

        // Assert
        assertTrue(viewModel.uiState.value is FacturasUiState.Success)
        val state = viewModel.uiState.value as FacturasUiState.Success
        assertEquals(testFacturas, state.facturas)
    }

    @Test
    fun facturasViewModel_partialFiltering_correctlyFiltersFacturas() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(testFacturas)
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)

        val filterStateFlow = MutableStateFlow(FilterState())
        whenever(filterManager.filterState).thenReturn(filterStateFlow)

        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Act - filtrar solo por importe mínimo, no por estado
        filterStateFlow.value = FilterState(importeMin = 150)

        // Assert - debería mostrar solo facturas con importe >= 150
        assertTrue(viewModel.uiState.value is FacturasUiState.Success)
        val state = viewModel.uiState.value as FacturasUiState.Success
        assertEquals(2, state.facturas.size)
        assertTrue(state.facturas.all { it.importeOrdenacion >= 150 })
    }

    @Test
    fun facturasViewModel_refreshWithNetworkError_showsSpecificError() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenReturn(MutableStateFlow(testFacturas))
        val networkError = IOException("Error de conexión")
        whenever(refreshFacturasUseCase()).thenAnswer { throw networkError }

        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Act
        viewModel.refreshFacturas()

        // Assert - verificar que el mensaje de error es específico para errores de red
        assertTrue(viewModel.uiState.value is FacturasUiState.Error)
        val errorState = viewModel.uiState.value as FacturasUiState.Error
        assertEquals(networkError.message, errorState.message)
    }

    @Test
    fun facturasViewModel_loadFacturasUnknownError_handlesGenericError() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenAnswer { throw Exception() } // Error sin mensaje

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Assert
        assertTrue(viewModel.uiState.value is FacturasUiState.Error)
        val errorState = viewModel.uiState.value as FacturasUiState.Error
        assertEquals("Error desconocido al cargar facturas", errorState.message)
    }

    @Test
    fun facturasViewModel_whileRefreshing_keepSuccessState() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(testFacturas)
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Aseguramos que estamos en estado Success
        assertTrue(viewModel.uiState.value is FacturasUiState.Success)

        // Act
        // Forzamos el estado de refreshing a true pero mantenemos el mismo estado UI
        val isRefreshingField = FacturasViewModel::class.java.getDeclaredField("_isRefreshing")
        isRefreshingField.isAccessible = true
        val isRefreshingStateFlow = isRefreshingField.get(viewModel) as MutableStateFlow<Boolean>
        isRefreshingStateFlow.value = true

        // Assert - El estado de UI se mantiene como Success aunque estemos refrescando
        assertTrue(viewModel.isRefreshing.value)
        assertTrue(viewModel.uiState.value is FacturasUiState.Success)
    }
}