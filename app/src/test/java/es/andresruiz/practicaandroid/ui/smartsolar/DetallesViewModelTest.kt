package es.andresruiz.practicaandroid.ui.smartsolar

import es.andresruiz.domain.models.Detalles
import es.andresruiz.domain.usecases.GetDetallesUseCase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
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

    @Before
    fun setUp() {
        Dispatchers.setMain(testDispatcher)

        // Inicializar mocks
        getDetallesUseCase = mock()
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
        viewModel = DetallesViewModel(getDetallesUseCase)

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
        viewModel = DetallesViewModel(getDetallesUseCase)

        // Assert
        val currentState = viewModel.uiState.value
        assertTrue(currentState is DetallesUiState.Empty)
    }

    @Test
    fun fetchDetalles_Exception_ReturnsErrorState() = runTest {
        // Arrange
        val errorMessage = "Error de conexión"
        whenever(getDetallesUseCase()).thenThrow(RuntimeException(errorMessage))

        // Act
        viewModel = DetallesViewModel(getDetallesUseCase)

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
        viewModel = DetallesViewModel(getDetallesUseCase)

        // Verificamos que estamos en estado de error
        assertTrue(viewModel.uiState.value is DetallesUiState.Error)

        // Act
        viewModel.retry()

        // Assert
        val currentState = viewModel.uiState.value
        assertTrue(currentState is DetallesUiState.Success)
        assertEquals(mockDetalles, (currentState as DetallesUiState.Success).detalles)
    }
}