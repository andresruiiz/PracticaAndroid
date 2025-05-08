package es.andresruiz.practicaandroid.ui.smartsolar

import es.andresruiz.domain.models.Detalles
import es.andresruiz.domain.usecases.GetDetallesUseCase
import es.andresruiz.practicaandroid.R
import es.andresruiz.practicaandroid.util.ResourceProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestDispatcher
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

@ExperimentalCoroutinesApi
class DetallesViewModelTest {

    private lateinit var viewModel: DetallesViewModel
    private lateinit var getDetallesUseCase: GetDetallesUseCase
    private lateinit var resourceProvider: ResourceProvider

    private val testDispatcher: TestDispatcher = UnconfinedTestDispatcher()

    private val mockDetalles = Detalles(
        cau = "ES002100000000199LJ1FA000",
        estadoSolicitud = "No hemos recibido ninguna solicitud de autoconsumo",
        tipoAutoconsumo = "Con excedentes y compensación individual - Consumo",
        compensacionExcendentes = "Precio PVPC",
        potenciaInstalacion = "5kWp"
    )

    private val emptyDetalles = Detalles(
        cau = "",
        estadoSolicitud = "",
        tipoAutoconsumo = "",
        compensacionExcendentes = "",
        potenciaInstalacion = ""
    )

    // Mensajes de error
    private val mockNoDetallesMessage = "No hay detalles disponibles para mostrar"
    private val mockErrorMessage = "Error al cargar los detalles"

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Inicializar mocks
        getDetallesUseCase = mock()
        resourceProvider = mock()

        whenever(resourceProvider.getString(R.string.no_detalles))
            .thenReturn(mockNoDetallesMessage)
        whenever(resourceProvider.getString(R.string.error_cargar_detalles))
            .thenReturn(mockErrorMessage)
    }

    @After
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun fetchDetalles_Success_ReturnsSuccessState() = runTest {
        // Arrange
        whenever(getDetallesUseCase()).thenReturn(mockDetalles)

        // Act
        viewModel = DetallesViewModel(getDetallesUseCase, resourceProvider)

        // Assert
        val currentState = viewModel.uiState.value
        assertTrue(currentState is DetallesUiState.Success)
        assertEquals(mockDetalles, (currentState as DetallesUiState.Success).detalles)
    }

    @Test
    fun fetchDetalles_EmptyResponse_ReturnsEmptyState() = runTest {
        // Arrange
        whenever(getDetallesUseCase()).thenReturn(emptyDetalles)

        // Act
        viewModel = DetallesViewModel(getDetallesUseCase, resourceProvider)

        // Assert
        val currentState = viewModel.uiState.value
        assertTrue(currentState is DetallesUiState.Empty)
        assertEquals(mockNoDetallesMessage, (currentState as DetallesUiState.Empty).message)
    }

    @Test
    fun fetchDetalles_Exception_ReturnsErrorState() = runTest {
        // Arrange
        val errorMessage = "Error de conexión"
        whenever(getDetallesUseCase()).thenThrow(RuntimeException(errorMessage))

        // Act
        viewModel = DetallesViewModel(getDetallesUseCase, resourceProvider)

        // Assert
        val currentState = viewModel.uiState.value
        assertTrue(currentState is DetallesUiState.Error)
        assertEquals(errorMessage, (currentState as DetallesUiState.Error).message)
    }

    @Test
    fun retry_AfterError_ShouldTryToFetchAgain() = runTest {
        // Arrange
        val errorMessage = "Error inicial"

        // Primero lanzamos una excepción, luego devolvemos los detalles correctos
        whenever(getDetallesUseCase())
            .thenThrow(RuntimeException(errorMessage))
            .thenReturn(mockDetalles)

        // Primer intento (falla)
        viewModel = DetallesViewModel(getDetallesUseCase, resourceProvider)

        // Verificamos que estamos en estado de error
        assertTrue(viewModel.uiState.value is DetallesUiState.Error)

        // Act
        viewModel.retry()

        // Assert
        val currentState = viewModel.uiState.value
        assertTrue(currentState is DetallesUiState.Success)
        assertEquals(mockDetalles, (currentState as DetallesUiState.Success).detalles)
    }

    @Test
    fun fetchDetalles_LoadingState_IsSetInitially() = runTest {
        // Arrange
        val testDispatcher = StandardTestDispatcher(testScheduler)
        Dispatchers.setMain(testDispatcher)
        whenever(getDetallesUseCase()).thenReturn(mockDetalles)

        // Act
        viewModel = DetallesViewModel(getDetallesUseCase, resourceProvider)

        // Assert
        assertTrue(viewModel.uiState.value is DetallesUiState.Loading)
        testDispatcher.scheduler.advanceUntilIdle()
        assertTrue(viewModel.uiState.value is DetallesUiState.Success)
    }

    @Test
    fun fetchDetalles_ExceptionWithoutMessage_ReturnsErrorState() = runTest {
        // Arrange
        whenever(getDetallesUseCase()).thenThrow(RuntimeException())

        // Act
        viewModel = DetallesViewModel(getDetallesUseCase, resourceProvider)

        // Assert
        val currentState = viewModel.uiState.value
        assertTrue(currentState is DetallesUiState.Error)
        assertEquals(mockErrorMessage, (currentState as DetallesUiState.Error).message)
    }

    @Test
    fun fetchDetalles_CauBlankEstadoNoBlank_ReturnsSuccessState() = runTest {
        // Arrange
        val detallesParciales = Detalles(
            cau = "",
            estadoSolicitud = "Activo",
            tipoAutoconsumo = "Tipo",
            compensacionExcendentes = "Compensación",
            potenciaInstalacion = "Potencia"
        )
        whenever(getDetallesUseCase()).thenReturn(detallesParciales)

        // Act
        viewModel = DetallesViewModel(getDetallesUseCase, resourceProvider)

        // Assert
        val currentState = viewModel.uiState.value
        assertTrue(currentState is DetallesUiState.Success)
        assertEquals(detallesParciales, (currentState as DetallesUiState.Success).detalles)
    }
}