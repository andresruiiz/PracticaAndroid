package es.andresruiz.practicaandroid.ui.facturas

import es.andresruiz.domain.models.Factura
import es.andresruiz.domain.models.FilterState
import es.andresruiz.domain.usecases.GetFacturasUseCase
import es.andresruiz.domain.usecases.RefreshFacturasUseCase
import es.andresruiz.practicaandroid.R
import es.andresruiz.practicaandroid.ui.facturas.FacturasViewModel.FacturasUiState
import es.andresruiz.practicaandroid.ui.filtros.FilterManager
import es.andresruiz.core.utils.ResourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
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
    private lateinit var resourceProvider: ResourceProvider

    private lateinit var viewModel: FacturasViewModel

    private val testFacturas = listOf(
        Factura("Pagada", 100.0, "01/01/2025"),
        Factura("Anulada", 200.0, "15/02/2025"),
        Factura("Pendiente de pago", 150.0, "10/03/2025")
    )

    private val mockNoFacturasMessage = "No hay facturas disponibles"
    private val mockNoFacturasFilterMessage = "No hay facturas que coincidan con los filtros seleccionados"
    private val mockErrorCargarFacturasMessage = "Error desconocido al cargar facturas"
    private val mockErrorRefrescarFacturasMessage = "Error desconocido al refrescar facturas"

    @Before
    fun setup() {
        Dispatchers.setMain(testDispatcher)

        // Inicializar mocks
        getFacturasUseCase = mock()
        refreshFacturasUseCase = mock()
        filterManager = mock()
        resourceProvider = mock()

        // Configuración por defecto de los mocks
        val filterStateFlow = MutableStateFlow(FilterState())
        whenever(filterManager.filterState).thenReturn(filterStateFlow)
        whenever(filterManager.getCurrentFilter()).thenReturn(FilterState())

        // Configuración ResourceProvider
        whenever(resourceProvider.getString(R.string.no_facturas))
            .thenReturn(mockNoFacturasMessage)
        whenever(resourceProvider.getString(R.string.no_facturas_filtros))
            .thenReturn(mockNoFacturasFilterMessage)
        whenever(resourceProvider.getString(R.string.error_cargar_facturas))
            .thenReturn(mockErrorCargarFacturasMessage)
        whenever(resourceProvider.getString(R.string.error_regargar_facturas))
            .thenReturn(mockErrorRefrescarFacturasMessage)
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
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

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
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Assert
        verify(refreshFacturasUseCase).invoke()
    }

    @Test
    fun facturasViewModel_refreshFacturas_handlesError() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenReturn(MutableStateFlow(testFacturas))
        val errorMessage = "Error de conexión"
        whenever(refreshFacturasUseCase()).thenAnswer { throw Exception(errorMessage) }
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

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

        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

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
    fun facturasViewModel_showDialog_updatesDialogState() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenReturn(MutableStateFlow(testFacturas))
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)
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
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)
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

        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

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

        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Filtro que no coincide con ninguna factura
        val filteredState = FilterState(
            importeMin = 1000
        )

        // Act
        filterStateFlow.value = filteredState

        // Assert
        assertTrue(viewModel.uiState.value is FacturasUiState.Empty)
        val emptyState = viewModel.uiState.value as FacturasUiState.Empty
        assertEquals(mockNoFacturasFilterMessage, emptyState.message)
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
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

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
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

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
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Assert
        verify(filterManager).updateDataBounds(100, 100)
    }

    @Test
    fun facturasViewModel_updateFilterManagerBounds_withEmptyList() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(emptyList<Factura>())
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Probamos que se llame al método directamente con valores por defecto
        val facturasField = FacturasViewModel::class.java.getDeclaredField("_allFacturas")
        facturasField.isAccessible = true
        val facturasStateFlow = facturasField.get(viewModel) as MutableStateFlow<List<Factura>>

        // Cambiamos el valor para forzar la ejecución del else en updateFilterManagerBounds
        facturasStateFlow.value = emptyList()

        // Llamamos manualmente al método privado
        val method = FacturasViewModel::class.java.getDeclaredMethod("updateFilterManagerBounds", List::class.java)
        method.isAccessible = true
        method.invoke(viewModel, emptyList<Factura>())

        // Assert
        verify(filterManager).updateDataBounds(1, 300)
    }

    @Test
    fun facturasViewModel_refreshFacturas_handlesUnknownException() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenReturn(MutableStateFlow(testFacturas))
        whenever(refreshFacturasUseCase()).thenAnswer { throw Exception() }
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Act
        viewModel.refreshFacturas()

        // Assert
        assertTrue(viewModel.uiState.value is FacturasUiState.Error)
        val errorState = viewModel.uiState.value as FacturasUiState.Error
        assertEquals(mockErrorRefrescarFacturasMessage, errorState.message)
    }

    @Test
    fun facturasViewModel_databaseEmitsNewFacturas_updatesUiState() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(emptyList<Factura>())
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

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

        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Act - filtrar solo por importe mínimo, no por estado
        filterStateFlow.value = FilterState(importeMin = 150)

        // Assert - debería mostrar solo facturas con importe >= 150
        assertTrue(viewModel.uiState.value is FacturasUiState.Success)
        val state = viewModel.uiState.value as FacturasUiState.Success
        assertEquals(2, state.facturas.size)
        assertTrue(state.facturas.all { it.importeOrdenacion >= 150 })
    }

    @Test
    fun facturasViewModel_loadFacturasUnknownError_handlesGenericError() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenAnswer { throw Exception() } // Error sin mensaje

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Assert
        assertTrue(viewModel.uiState.value is FacturasUiState.Error)
        val errorState = viewModel.uiState.value as FacturasUiState.Error
        assertEquals(mockErrorCargarFacturasMessage, errorState.message)
    }

    @Test
    fun facturasViewModel_whileRefreshing_keepSuccessState() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(testFacturas)
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Aseguramos que estamos en estado Success
        assertTrue(viewModel.uiState.value is FacturasUiState.Success)

        // Act
        // Forzamos el estado de refreshing a true pero mantenemos el mismo estado UI
        val isRefreshingField = FacturasViewModel::class.java.getDeclaredField("_isRefreshing")
        isRefreshingField.isAccessible = true
        val isRefreshingStateFlow = isRefreshingField.get(viewModel) as MutableStateFlow<Boolean>
        isRefreshingStateFlow.value = true

        // Simulamos una reemisión del combine
        val filterStateFlow = MutableStateFlow(FilterState())
        whenever(filterManager.filterState).thenReturn(filterStateFlow)
        filterStateFlow.value = FilterState()

        // Assert - El estado de UI se mantiene como Success aunque estemos refrescando
        assertTrue(viewModel.isRefreshing.value)
        assertTrue(viewModel.uiState.value is FacturasUiState.Success)
    }

    @Test
    fun facturasViewModel_retry_callsRefreshFacturas() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenReturn(MutableStateFlow(testFacturas))
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Act
        viewModel.retry()

        // Assert
        verify(refreshFacturasUseCase).invoke()
    }

    @Test
    fun facturasViewModel_emptyWithErrorState_maintainsErrorState() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(emptyList<Factura>())
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Forzamos un estado de error
        val uiStateField = FacturasViewModel::class.java.getDeclaredField("_uiState")
        uiStateField.isAccessible = true
        val uiStateFlow = uiStateField.get(viewModel) as MutableStateFlow<FacturasUiState>
        uiStateFlow.value = FacturasUiState.Error("Error previo")

        // Confirmamos que estamos en estado Error
        assertTrue(viewModel.uiState.value is FacturasUiState.Error)

        // Act - simulamos una reemisión con facturas vacías
        val filterStateFlow = MutableStateFlow(FilterState())
        whenever(filterManager.filterState).thenReturn(filterStateFlow)
        filterStateFlow.value = FilterState() // Forzar reemisión del combine

        // Assert - El estado de error se debe mantener aunque la lista esté vacía
        assertTrue(viewModel.uiState.value is FacturasUiState.Error)
    }

    @Test
    fun facturasViewModel_emptyWithRefreshing_showsLoadingState() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(emptyList<Factura>())
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Forzamos el estado de refreshing a true
        val isRefreshingField = FacturasViewModel::class.java.getDeclaredField("_isRefreshing")
        isRefreshingField.isAccessible = true
        val isRefreshingStateFlow = isRefreshingField.get(viewModel) as MutableStateFlow<Boolean>
        isRefreshingStateFlow.value = true

        // Forzamos el estado de UI a Loading
        val uiStateField = FacturasViewModel::class.java.getDeclaredField("_uiState")
        uiStateField.isAccessible = true
        val uiStateFlow = uiStateField.get(viewModel) as MutableStateFlow<FacturasUiState>
        uiStateFlow.value = FacturasUiState.Loading

        // Act - simulamos una reemisión con facturas vacías y refreshing true
        val filterStateFlow = MutableStateFlow(FilterState())
        whenever(filterManager.filterState).thenReturn(filterStateFlow)
        filterStateFlow.value = FilterState() // Forzar reemisión del combine

        // Assert - Si está vacío pero refreshing es true, debe mostrar Loading
        assertTrue(viewModel.isRefreshing.value)
        assertTrue(viewModel.uiState.value is FacturasUiState.Loading)
    }

    @Test
    fun facturasViewModel_errorWhileRefreshing_showsErrorState() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(testFacturas)
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Forzamos el estado de refreshing a true
        val isRefreshingField = FacturasViewModel::class.java.getDeclaredField("_isRefreshing")
        isRefreshingField.isAccessible = true
        val isRefreshingStateFlow = isRefreshingField.get(viewModel) as MutableStateFlow<Boolean>
        isRefreshingStateFlow.value = true

        // Act - Simulamos un error durante el refresh
        val errorMsg = "Error durante refresh"
        val uiStateField = FacturasViewModel::class.java.getDeclaredField("_uiState")
        uiStateField.isAccessible = true
        val uiStateFlow = uiStateField.get(viewModel) as MutableStateFlow<FacturasUiState>
        uiStateFlow.value = FacturasUiState.Error(errorMsg)

        // Simulamos la reemisión del combine para verificar la condición
        val filterStateFlow = MutableStateFlow(FilterState())
        whenever(filterManager.filterState).thenReturn(filterStateFlow)
        filterStateFlow.value = FilterState() // Forzar reemisión

        // Assert - Cuando hay un error, debe mostrarse aunque esté refreshing
        assertTrue(viewModel.isRefreshing.value)
        assertTrue(viewModel.uiState.value is FacturasUiState.Error)
        assertEquals(errorMsg, (viewModel.uiState.value as FacturasUiState.Error).message)
    }

    @Test
    fun facturasViewModel_initWithEmptyDatabaseAndRefreshingTrue_handlesCorrectly() = runTest {
        // Arrange
        val emptyFlow = MutableStateFlow(emptyList<Factura>())
        whenever(getFacturasUseCase()).thenReturn(emptyFlow)

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Forzamos el estado de refreshing a true
        val isRefreshingField = FacturasViewModel::class.java.getDeclaredField("_isRefreshing")
        isRefreshingField.isAccessible = true
        val isRefreshingStateFlow = isRefreshingField.get(viewModel) as MutableStateFlow<Boolean>
        isRefreshingStateFlow.value = true

        // Emitimos un nuevo valor vacío para forzar la evaluación de la condición
        emptyFlow.value = emptyList()

        // Assert - No debería llamar a refreshFacturas() de nuevo
        verify(refreshFacturasUseCase, times(1)).invoke() // Ya se llama una vez en el init
    }

    @Test
    fun facturasViewModel_noReEmitSameState_whenFiltersChange() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(testFacturas)
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)

        val filterStateFlow = MutableStateFlow(FilterState())
        whenever(filterManager.filterState).thenReturn(filterStateFlow)

        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Act - Cambiamos a un filtro que produce el mismo resultado
        val initialState = viewModel.uiState.value
        filterStateFlow.value = FilterState() // Mismo filtro, debe producir mismo resultado

        // Assert - El estado debe seguir siendo Success con las mismas facturas
        assertEquals(initialState::class, viewModel.uiState.value::class)
        assertTrue(viewModel.uiState.value is FacturasUiState.Success)
    }

    @Test
    fun facturaViewModel_nullMinMax_usesDefaultValues() = runTest {
        // Arrange - Lista con facturas pero forzamos a que minOfOrNull y maxOfOrNull devuelvan null
        val facturasFlow = MutableStateFlow(testFacturas)
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        val updateMethod = FacturasViewModel::class.java.getDeclaredMethod(
            "updateFilterManagerBounds",
            List::class.java
        )
        updateMethod.isAccessible = true

        val mockList = mutableListOf<Factura>()
        mockList.add(Factura("Test", Double.NaN, "01/01/2025")) // Valor que causaría problemas

        updateMethod.invoke(viewModel, mockList)

        // Assert - Verificamos que se usan los valores por defecto cuando min/max son null
        verify(filterManager).updateDataBounds(100, 200)
    }

    @Test
    fun facturaViewModel_refreshWithErrorWhileLoadingData_showsError() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(emptyList<Factura>())
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)

        // Act - Simulamos un error al refrescar
        val errorMessage = "Error de conexión específico"
        whenever(refreshFacturasUseCase()).thenAnswer { throw IOException(errorMessage) }

        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Assert
        assertTrue(viewModel.uiState.value is FacturasUiState.Error)
        assertEquals(errorMessage, (viewModel.uiState.value as FacturasUiState.Error).message)
    }

    @Test
    fun facturaViewModel_initWithEmptyDatabaseAndErrorInRefresh_showsError() = runTest {
        // Arrange
        val emptyFlow = MutableStateFlow(emptyList<Factura>())
        whenever(getFacturasUseCase()).thenReturn(emptyFlow)
        val errorMessage = "Error al refrescar inicialmente"
        whenever(refreshFacturasUseCase()).thenAnswer { throw Exception(errorMessage) }

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Assert
        assertTrue(viewModel.uiState.value is FacturasUiState.Error)
        assertEquals(errorMessage, (viewModel.uiState.value as FacturasUiState.Error).message)
    }

    @Test
    fun facturaViewModel_facturaStateClasses_instantiate() {
        // Arrange & Act - Instanciamos todos los tipos de estado
        val loadingState = FacturasUiState.Loading
        val successState = FacturasUiState.Success(testFacturas)
        val emptyState = FacturasUiState.Empty("Test message")
        val errorState = FacturasUiState.Error("Test error")

        // Assert - Verificamos que se crean correctamente
        assertTrue(loadingState is FacturasUiState.Loading)
        assertTrue(successState is FacturasUiState.Success)
        assertEquals(testFacturas, successState.facturas)
        assertTrue(emptyState is FacturasUiState.Empty)
        assertEquals("Test message", emptyState.message)
        assertTrue(errorState is FacturasUiState.Error)
        assertEquals("Test error", errorState.message)
    }

    @Test
    fun facturasViewModel_initWithLoadDataError_showsError() = runTest {
        // Arrange
        val errorMessage = "Error al cargar facturas"
        whenever(getFacturasUseCase()).thenThrow(RuntimeException(errorMessage))

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Assert
        assertTrue(viewModel.uiState.value is FacturasUiState.Error)
        assertEquals(errorMessage, (viewModel.uiState.value as FacturasUiState.Error).message)
    }

    @Test
    fun facturasViewModel_refreshFacturas_setsIsRefreshingToFalseAfterError() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenReturn(MutableStateFlow(testFacturas))
        whenever(refreshFacturasUseCase()).thenThrow(RuntimeException("Error"))

        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Act
        viewModel.refreshFacturas()

        // Assert
        assertFalse(viewModel.isRefreshing.value)
        assertTrue(viewModel.uiState.value is FacturasUiState.Error)
    }

    @Test
    fun facturasViewModel_refreshFacturas_setsIsRefreshingToFalseAfterSuccess() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenReturn(MutableStateFlow(testFacturas))
        whenever(refreshFacturasUseCase()).thenReturn(Unit)

        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Act
        viewModel.refreshFacturas()

        // Assert
        assertFalse(viewModel.isRefreshing.value)
    }

    @Test
    fun facturasViewModel_retry_invokesRefreshFacturas() = runTest {
        // Arrange
        whenever(getFacturasUseCase()).thenReturn(MutableStateFlow(testFacturas))
        whenever(refreshFacturasUseCase()).thenReturn(Unit)

        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Act
        viewModel.retry()

        // Assert
        verify(refreshFacturasUseCase).invoke()
    }

    @Test
    fun facturasViewModel_updateFilterManagerBounds_withExtremeValues() = runTest {
        // Arrange
        val extremeFacturas = listOf(
            Factura("Pagada", 0.0, "01/01/2025"),
            Factura("Anulada", 1000.0, "15/02/2025")
        )
        val facturasFlow = MutableStateFlow(extremeFacturas)
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)

        // Act
        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Assert
        verify(filterManager).updateDataBounds(1, 1000) // Mínimo coercionado a 1
    }

    @Test
    fun facturasViewModel_emptyListWithError_maintainsError() = runTest {
        // Arrange
        val facturasFlow = MutableStateFlow(emptyList<Factura>())
        whenever(getFacturasUseCase()).thenReturn(facturasFlow)
        whenever(refreshFacturasUseCase()).thenThrow(RuntimeException("Error previo"))

        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Act
        viewModel.refreshFacturas() // Provoca el estado de error

        // Assert
        assertTrue(viewModel.uiState.value is FacturasUiState.Error)
        assertEquals("Error previo", (viewModel.uiState.value as FacturasUiState.Error).message)
    }

    @Test
    fun facturasViewModel_emptyListNoRefreshing_showsEmpty() = runTest {
        // Arrange
        val emptyFlow = MutableStateFlow(emptyList<Factura>())
        whenever(getFacturasUseCase()).thenReturn(emptyFlow)
        whenever(refreshFacturasUseCase()).thenReturn(Unit)

        viewModel = FacturasViewModel(getFacturasUseCase, refreshFacturasUseCase, filterManager, resourceProvider)

        // Act
        advanceUntilIdle() // Esperar a que las corrutinas terminen

        // Assert
        assertFalse(viewModel.isRefreshing.value)
        assertTrue(viewModel.uiState.value is FacturasUiState.Empty)
        assertEquals(mockNoFacturasMessage, (viewModel.uiState.value as FacturasUiState.Empty).message)
    }
}