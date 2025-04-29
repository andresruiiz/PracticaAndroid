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
            cau = "CAU123456",
            estadoSolicitud = "Activa",
            tipoAutoconsumo = "Individual",
            compensacionExcendentes = "Con compensación",
            potenciaInstalacion = "5kW"
        )
        whenever(detallesRepository.getDetalles()).thenReturn(expectedDetalles)

        // Act
        val result = getDetallesUseCase()

        // Assert
        assertEquals(expectedDetalles, result)
        assertEquals("CAU123456", result.cau)
        assertEquals("Activa", result.estadoSolicitud)
        assertEquals("Individual", result.tipoAutoconsumo)
        assertEquals("Con compensación", result.compensacionExcendentes)
        assertEquals("5kW", result.potenciaInstalacion)
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