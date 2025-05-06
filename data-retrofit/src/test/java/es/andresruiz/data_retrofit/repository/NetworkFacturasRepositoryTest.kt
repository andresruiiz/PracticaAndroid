package es.andresruiz.data_retrofit.repository

import es.andresruiz.data_retrofit.database.FacturaDao
import es.andresruiz.data_retrofit.database.FacturaEntity
import es.andresruiz.data_retrofit.network.FacturasApiService
import es.andresruiz.data_retrofit.network.FacturasApiServiceFactory
import es.andresruiz.domain.models.Factura
import es.andresruiz.domain.models.FacturasResponse
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever

class NetworkFacturasRepositoryTest {

    private lateinit var facturasApiService: FacturasApiService
    private lateinit var facturasApiServiceFactory: FacturasApiServiceFactory
    private lateinit var facturaDao: FacturaDao
    private lateinit var repository: NetworkFacturasRepository

    @Before
    fun setup() {
        facturasApiService = mock()
        facturasApiServiceFactory = mock()
        facturaDao = mock()

        whenever(facturasApiServiceFactory.getApiService()).thenReturn(facturasApiService)

        repository = NetworkFacturasRepository(facturasApiServiceFactory, facturaDao)
    }

    @Test
    fun getFacturas_returnsFacturasFromDb() = runTest {
        // Arrange
        val facturaEntities = listOf(
            FacturaEntity(id = 1, descEstado = "Pagada", importeOrdenacion = 100.0, fecha = "01/01/2025"),
            FacturaEntity(id = 2, descEstado = "Anulada", importeOrdenacion = 200.0, fecha = "15/02/2025")
        )

        whenever(facturaDao.getAllFacturas()).thenReturn(flowOf(facturaEntities))

        // Act
        val result = repository.getFacturas().first()

        // Assert
        assertEquals(2, result.size)
        assertEquals("Pagada", result[0].descEstado)
        assertEquals(100.0, result[0].importeOrdenacion, 0.01)
        assertEquals("01/01/2025", result[0].fecha)
    }

    @Test
    fun refreshFacturas_fetchesFromApiAndSavesToDb() = runTest {
        // Arrange
        val facturas = listOf(
            Factura(descEstado = "Pagada", importeOrdenacion = 100.0, fecha = "01/01/2025"),
            Factura(descEstado = "Anulada", importeOrdenacion = 200.0, fecha = "15/02/2025")
        )
        val response = FacturasResponse(numFacturas = 2, facturas = facturas)

        whenever(facturasApiService.getFacturas()).thenReturn(response)

        // Act
        repository.refreshFacturas()

        // Assert
        verify(facturaDao).deleteAllFacturas()
        verify(facturaDao).insertFacturas(any())
    }

    @Test
    fun getFacturas_withEmptyDb_returnsEmptyList() = runTest {
        // Arrange
        val emptyList = emptyList<FacturaEntity>()
        whenever(facturaDao.getAllFacturas()).thenReturn(flowOf(emptyList))

        // Act
        val result = repository.getFacturas().first()

        // Assert
        assertEquals(0, result.size)
    }
}