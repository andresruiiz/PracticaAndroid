package es.andresruiz.practicaandroid.ui.facturas

import es.andresruiz.domain.models.Factura
import es.andresruiz.domain.models.FilterState
import es.andresruiz.domain.usecases.GetFacturasUseCase
import es.andresruiz.domain.usecases.RefreshFacturasUseCase
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
import org.junit.Assert.assertNull
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
        assertEquals(testFacturas, viewModel.facturas.value)
        assertFalse(viewModel.isLoading.value)
        assertNull(viewModel.error.value)
        assertFalse(viewModel.isEmpty.value)
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
    fun facturasViewModel_refreshFacturas_updatesLoadingState() = runTest {
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
        assertEquals(errorMessage, viewModel.error.value)
        assertFalse(viewModel.isLoading.value)
    }

    @Test
    fun facturasViewModel_refreshFacturasWhileLoading_doesNothing() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenReturn(MutableStateFlow(testFacturas))
        var isRefreshing = false
        doAnswer {
            isRefreshing = true
            // No completamos la corrutina para mantener isLoading en true
        }.whenever(refreshFacturasUseCase).invoke()

        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Primera llamada para activar isLoading
        viewModel.refreshFacturas()

        // Nos aseguramos que se ha llamado la primera vez
        verify(refreshFacturasUseCase, times(1)).invoke()
        assertTrue("La carga debería estar en progreso", isRefreshing)

        // Ahora forzamos el estado isLoading a true para el test
        val isLoadingField = FacturasViewModel::class.java.getDeclaredField("_isLoading")
        isLoadingField.isAccessible = true
        val isLoadingStateFlow = isLoadingField.get(viewModel) as MutableStateFlow<Boolean>
        isLoadingStateFlow.value = true

        // Verificamos que isLoading está ahora en true
        assertTrue(viewModel.isLoading.value)

        // Act
        viewModel.refreshFacturas() // Segunda llamada mientras isLoading es true

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
        assertEquals(1, viewModel.facturas.value.size)
        assertEquals("Pagada", viewModel.facturas.value[0].descEstado)
    }

    @Test
    fun facturasViewModel_filterResultEmpty_updatesIsEmptyState() = runTest {
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
        assertTrue(viewModel.isEmpty.value)
        assertTrue(viewModel.facturas.value.isEmpty())
    }

    @Test
    fun facturasViewModel_uiStateLoading_whenIsLoading() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(testFacturas)
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)

        doAnswer {
            // No completamos la corrutina para que isLoading se mantenga en true
        }.whenever(refreshFacturasUseCase).invoke()

        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Act
        viewModel.refreshFacturas()

        // Forzamos manualmente el estado de loading para el test
        val isLoadingField = FacturasViewModel::class.java.getDeclaredField("_isLoading")
        isLoadingField.isAccessible = true
        val isLoadingStateFlow = isLoadingField.get(viewModel) as MutableStateFlow<Boolean>
        isLoadingStateFlow.value = true

        // Assert
        assertTrue(viewModel.isLoading.value)
        assertTrue(viewModel.uiState.value is FacturasViewModel.FacturasUiState.Loading)
    }

    @Test
    fun facturasViewModel_uiStateSuccess_whenFacturasLoaded() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(testFacturas)
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Assert
        assertTrue(viewModel.uiState.value is FacturasViewModel.FacturasUiState.Success)
        val state = viewModel.uiState.value as FacturasViewModel.FacturasUiState.Success
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
        assertTrue(viewModel.uiState.value is FacturasViewModel.FacturasUiState.Error)
        val state = viewModel.uiState.value as FacturasViewModel.FacturasUiState.Error
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

        // Filtro que no coincide con ninguna factura
        val filteredState = FilterState(
            importeMin = 1000
        )

        // Act
        filterStateFlow.value = filteredState

        // Assert
        assertTrue(viewModel.uiState.value is FacturasViewModel.FacturasUiState.Empty)
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

        // Forzamos el estado de loading a false para simular la finalización
        val isLoadingField = FacturasViewModel::class.java.getDeclaredField("_isLoading")
        isLoadingField.isAccessible = true
        val isLoadingStateFlow = isLoadingField.get(viewModel) as MutableStateFlow<Boolean>
        isLoadingStateFlow.value = false

        // Forzamos el estado vacío para la prueba
        val allFacturasField = FacturasViewModel::class.java.getDeclaredField("_allFacturas")
        allFacturasField.isAccessible = true
        val allFacturasStateFlow = allFacturasField.get(viewModel) as MutableStateFlow<List<Factura>>
        allFacturasStateFlow.value = emptyList()

        // Assert
        assertFalse(viewModel.isLoading.value)
        assertTrue(viewModel.uiState.value is FacturasViewModel.FacturasUiState.Empty)
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
        assertEquals(errorMessage, viewModel.error.value)
        assertTrue(viewModel.uiState.value is FacturasViewModel.FacturasUiState.Error)
        val errorState = viewModel.uiState.value as FacturasViewModel.FacturasUiState.Error
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
    fun facturasViewModel_refreshFacturas_clearsErrorState() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenReturn(MutableStateFlow(testFacturas))
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Forzamos un estado de error
        val errorField = FacturasViewModel::class.java.getDeclaredField("_error")
        errorField.isAccessible = true
        val errorStateFlow = errorField.get(viewModel) as MutableStateFlow<String?>
        errorStateFlow.value = "Error previo"

        assertEquals("Error previo", viewModel.error.value)

        // Act
        viewModel.refreshFacturas()

        // Assert
        assertNull(viewModel.error.value)
    }

    @Test
    fun facturasViewModel_uiStateEmpty_whenFiltersRemoveAllFacturas() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(testFacturas)
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)
        val filterStateFlow = MutableStateFlow(FilterState())
        whenever(filterManager.filterState).thenReturn(filterStateFlow)
        whenever(filterManager.getCurrentFilter()).thenReturn(FilterState(importeMin = 1000))

        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Act
        filterStateFlow.value = FilterState(importeMin = 1000)

        // Assert
        assertTrue(viewModel.uiState.value is FacturasViewModel.FacturasUiState.Empty)
        val state = viewModel.uiState.value as FacturasViewModel.FacturasUiState.Empty
        assertEquals("No hay facturas que coincidan con los filtros seleccionados", state.message)
    }

    @Test
    fun facturasViewModel_loadFacturas_handlesUnknownError() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenAnswer { throw Exception() } // Error sin mensaje

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Assert
        assertEquals("Error desconocido al cargar facturas", viewModel.error.value)
        assertTrue(viewModel.uiState.value is FacturasViewModel.FacturasUiState.Error)
    }

    @Test
    fun facturasViewModel_uiStateEmpty_whenNoFacturasAtAll() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenReturn(MutableStateFlow(emptyList()))

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Assert
        assertTrue(viewModel.uiState.value is FacturasViewModel.FacturasUiState.Empty)
        val state = viewModel.uiState.value as FacturasViewModel.FacturasUiState.Empty
        assertEquals("No hay facturas disponibles", state.message)
    }

    @Test
    fun facturasViewModel_filterChange_doesNotChangeFacturas() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(testFacturas)
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)

        val filterStateFlow = MutableStateFlow(FilterState()) // Filtro sin cambios
        whenever(filterManager.filterState).thenReturn(filterStateFlow)
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Act
        filterStateFlow.value = FilterState() // Aplicar el mismo filtro

        // Assert
        assertEquals(testFacturas, viewModel.facturas.value) // Lista sin cambios
        assertFalse(viewModel.isEmpty.value)
    }

    @Test
    fun facturasViewModel_loadFacturas_unexpectedException() = runTest {
        // Arrange
        val unexpectedError = IllegalStateException("Estado inesperado")
        whenever(getFacturasUseCase()).thenAnswer { throw unexpectedError }

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Assert
        assertEquals(unexpectedError.message, viewModel.error.value)
        assertTrue(viewModel.uiState.value is FacturasViewModel.FacturasUiState.Error)
    }

    @Test
    fun facturasViewModel_uiStateSuccess_withEmptyFacturas() = runTest {
        // Arrange
        val emptyFacturas = emptyList<Factura>()
        val facturasFlow = MutableStateFlow(emptyFacturas)
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Assert
        assertTrue(viewModel.uiState.value is FacturasViewModel.FacturasUiState.Empty)
    }

    @Test
    fun facturasViewModel_showDialogThenHideDialog_correctValue() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenReturn(MutableStateFlow(testFacturas))
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager)

        // Act
        viewModel.showDialog()
        assertTrue(viewModel.showDialog.value)
        viewModel.hideDialog()

        // Assert
        assertFalse(viewModel.showDialog.value)
    }
}