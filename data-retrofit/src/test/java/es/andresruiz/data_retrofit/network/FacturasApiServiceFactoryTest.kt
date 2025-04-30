package es.andresruiz.data_retrofit.network

import es.andresruiz.domain.UseMockProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.whenever

class FacturasApiServiceFactoryTest {

    // Mocks
    private lateinit var retrofitService: FacturasApiService
    private lateinit var retromockService: FacturasApiService
    private lateinit var useMockProvider: UseMockProvider

    private lateinit var factory: FacturasApiServiceFactory

    @Before
    fun setup() {
        // Inicializar mocks
        retrofitService = mock()
        retromockService = mock()
        useMockProvider = mock()

        // Inicializar el componente a testear
        factory = FacturasApiServiceFactory(retrofitService, retromockService, useMockProvider)
    }

    @Test
    fun facturasApiServiceFactory_mockEnabled_returnsRetromockService() {
        // Arrange
        whenever(useMockProvider.isMockEnabled()).thenReturn(true)

        // Act
        val result = factory.getApiService()

        // Assert
        assertEquals(retromockService, result)
    }

    @Test
    fun facturasApiServiceFactory_mockDisabled_returnsRetrofitService() {
        // Arrange
        whenever(useMockProvider.isMockEnabled()).thenReturn(false)

        // Act
        val result = factory.getApiService()

        // Assert
        assertEquals(retrofitService, result)
    }
}