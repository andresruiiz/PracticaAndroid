package es.andresruiz.data_retrofit.repository

import es.andresruiz.data_retrofit.network.FacturasApiService
import es.andresruiz.data_retrofit.network.FacturasApiServiceFactory
import es.andresruiz.domain.models.Detalles
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class NetworkDetallesRepositoryTest {

    private lateinit var facturasApiService: FacturasApiService
    private lateinit var facturasApiServiceFactory: FacturasApiServiceFactory
    private lateinit var repository: NetworkDetallesRepository

    @Before
    fun setup() {
        facturasApiService = mock()
        facturasApiServiceFactory = mock()
        whenever(facturasApiServiceFactory.getApiService()).thenReturn(facturasApiService)
        repository = NetworkDetallesRepository(facturasApiServiceFactory)
    }

    @Test
    fun getDetalles_returnsDetallesFromApi() = runTest {
        // Arrange
        val expectedDetalles = Detalles(
            cau = "CAU123456",
            estadoSolicitud = "Activa",
            tipoAutoconsumo = "Individual",
            compensacionExcendentes = "Con compensación",
            potenciaInstalacion = "5kW"
        )
        whenever(facturasApiService.getDetallesSmartSolar()).thenReturn(expectedDetalles)

        // Act
        val result = repository.getDetalles()

        // Assert
        assertEquals(expectedDetalles, result)
        assertEquals("CAU123456", result.cau)
        assertEquals("Activa", result.estadoSolicitud)
        assertEquals("Individual", result.tipoAutoconsumo)
        assertEquals("Con compensación", result.compensacionExcendentes)
        assertEquals("5kW", result.potenciaInstalacion)
    }

    @Test
    fun getDetalles_withEmptyResponse_returnsEmptyDetalles() = runTest {
        // Arrange
        val emptyDetalles = Detalles(
            cau = "",
            estadoSolicitud = "",
            tipoAutoconsumo = "",
            compensacionExcendentes = "",
            potenciaInstalacion = ""
        )
        whenever(facturasApiService.getDetallesSmartSolar()).thenReturn(emptyDetalles)

        // Act
        val result = repository.getDetalles()

        // Assert
        assertEquals(emptyDetalles, result)
        assertEquals("", result.cau)
        assertEquals("", result.estadoSolicitud)
    }
}