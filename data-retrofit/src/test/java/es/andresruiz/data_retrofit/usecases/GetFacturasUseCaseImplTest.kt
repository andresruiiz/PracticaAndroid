package es.andresruiz.data_retrofit.usecases

import es.andresruiz.data_retrofit.repository.FacturasRepository
import es.andresruiz.domain.models.Factura
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class GetFacturasUseCaseImplTest {

    private lateinit var facturasRepository: FacturasRepository
    private lateinit var getFacturasUseCase: GetFacturasUseCaseImpl

    @Before
    fun setup() {
        facturasRepository = mock()
        getFacturasUseCase = GetFacturasUseCaseImpl(facturasRepository)
    }

    @Test
    fun getFacturasUseCase_invokeWithEmptyRepository_returnsEmptyList() = runTest {
        // Arrange
        val emptyList = emptyList<Factura>()
        whenever(facturasRepository.getFacturas()).thenReturn(flowOf(emptyList))

        // Act
        val result = getFacturasUseCase().first()

        // Assert
        assertEquals(emptyList, result)
    }

    @Test
    fun getFacturasUseCase_invokeWithFacturas_returnsFacturasList() = runTest {
        // Arrange
        val facturas = listOf(
            Factura("Pagada", 100.0, "01/01/2025"),
            Factura("Anulada", 200.0, "15/02/2025")
        )
        whenever(facturasRepository.getFacturas()).thenReturn(flowOf(facturas))

        // Act
        val result = getFacturasUseCase().first()

        // Assert
        assertEquals(facturas, result)
        assertEquals(2, result.size)
        assertEquals("Pagada", result[0].descEstado)
        assertEquals(100.0, result[0].importeOrdenacion, 0.01)
        assertEquals("01/01/2025", result[0].fecha)
    }
}