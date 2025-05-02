package es.andresruiz.data_retrofit.usecases

import es.andresruiz.data_retrofit.repository.DetallesRepository
import es.andresruiz.domain.models.Detalles
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetDetallesUseCaseImplTest {

    private lateinit var detallesRepository: DetallesRepository
    private lateinit var getDetallesUseCase: GetDetallesUseCaseImpl

    @Before
    fun setup() {
        detallesRepository = mock()
        getDetallesUseCase = GetDetallesUseCaseImpl(detallesRepository)
    }

    @Test
    fun getDetallesUseCase_invoke_returnsDetallesFromRepository() = runTest {
        // Arrange
        val expectedDetalles = Detalles(
            cau = "ES002100000000199LJ1FA000",
            estadoSolicitud = "No hemos recibido ninguna solicitud de autoconsumo",
            tipoAutoconsumo = "Con excedentes y compensación individual - Consumo",
            compensacionExcendentes = "Precio PVPC",
            potenciaInstalacion = "5kWp"
        )
        whenever(detallesRepository.getDetalles()).thenReturn(expectedDetalles)

        // Act
        val result = getDetallesUseCase()

        // Assert
        assertEquals(expectedDetalles, result)
        assertEquals("ES002100000000199LJ1FA000", result.cau)
        assertEquals("No hemos recibido ninguna solicitud de autoconsumo", result.estadoSolicitud)
        assertEquals("Con excedentes y compensación individual - Consumo", result.tipoAutoconsumo)
        assertEquals("Precio PVPC", result.compensacionExcendentes)
        assertEquals("5kWp", result.potenciaInstalacion)
    }

    @Test
    fun getDetallesUseCase_invokeWithEmptyDetails_returnsEmptyDetails() = runTest {
        // Arrange
        val emptyDetalles = Detalles(
            cau = "",
            estadoSolicitud = "",
            tipoAutoconsumo = "",
            compensacionExcendentes = "",
            potenciaInstalacion = ""
        )
        whenever(detallesRepository.getDetalles()).thenReturn(emptyDetalles)

        // Act
        val result = getDetallesUseCase()

        // Assert
        assertEquals(emptyDetalles, result)
        assertEquals("", result.cau)
        assertEquals("", result.estadoSolicitud)
    }
}