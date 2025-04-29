package es.andresruiz.data_retrofit.usecases

import es.andresruiz.data_retrofit.repository.FacturasRepository
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test
import org.mockito.kotlin.mock
import org.mockito.kotlin.verify
import org.mockito.kotlin.times

class RefreshFacturasUseCaseImplTest {

    private lateinit var facturasRepository: FacturasRepository
    private lateinit var refreshFacturasUseCase: RefreshFacturasUseCaseImpl

    @Before
    fun setup() {
        facturasRepository = mock()
        refreshFacturasUseCase = RefreshFacturasUseCaseImpl(facturasRepository)
    }

    @Test
    fun refreshFacturasUseCase_invoke_callsRepositoryRefresh() = runTest {

        // Act
        refreshFacturasUseCase()

        // Assert
        verify(facturasRepository, times(1)).refreshFacturas()
    }

    @Test
    fun refreshFacturasUseCase_invokeMultipleTimes_callsRepositoryRefreshMultipleTimes() = runTest {

        // Act
        refreshFacturasUseCase()
        refreshFacturasUseCase()
        refreshFacturasUseCase()

        // Assert
        verify(facturasRepository, times(3)).refreshFacturas()
    }
}