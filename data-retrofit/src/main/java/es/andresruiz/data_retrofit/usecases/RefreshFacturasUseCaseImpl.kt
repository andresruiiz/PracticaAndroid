package es.andresruiz.data_retrofit.usecases

import es.andresruiz.data_retrofit.repository.FacturasRepository
import es.andresruiz.domain.usecases.RefreshFacturasUseCase
import javax.inject.Inject


/**
 * Implementación del caso de uso para refrescar la lista de facturas
 */
class RefreshFacturasUseCaseImpl @Inject constructor(
    private val facturasRepository: FacturasRepository
): RefreshFacturasUseCase {

    override suspend fun invoke() {
        facturasRepository.refreshFacturas()
    }
}