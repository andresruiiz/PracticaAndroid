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
            cau = "ES002100000000199LJ1FA000",
            estadoSolicitud = "No hemos recibido ninguna solicitud de autoconsumo",
            tipoAutoconsumo = "Con excedentes y compensación individual - Consumo",
            compensacionExcendentes = "Precio PVPC",
            potenciaInstalacion = "5kWp"
        )
        whenever(facturasApiService.getDetallesSmartSolar()).thenReturn(expectedDetalles)

        // Act
        val result = repository.getDetalles()

        // Assert
        assertEquals(expectedDetalles, result)
        assertEquals("ES002100000000199LJ1FA000", result.cau)
        assertEquals("No hemos recibido ninguna solicitud de autoconsumo", result.estadoSolicitud)
        assertEquals("Con excedentes y compensación individual - Consumo", result.tipoAutoconsumo)
        assertEquals("Precio PVPC", result.compensacionExcendentes)
        assertEquals("5kWp", result.potenciaInstalacion)
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