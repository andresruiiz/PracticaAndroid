package es.andresruiz.data_retrofit.usecases

import es.andresruiz.data_retrofit.repository.FacturasRepository
import es.andresruiz.domain.models.Factura
import es.andresruiz.domain.usecases.GetFacturasUseCase
import kotlinx.coroutines.flow.Flow
import javax.inject.Inject

/**
 * Implementación del caso de uso para obtener la lista de facturas
 */
class GetFacturasUseCaseImpl @Inject constructor(
    private val facturasRepository: FacturasRepository
): GetFacturasUseCase {

    override fun invoke(): Flow<List<Factura>> {
        return facturasRepository.getFacturas()
    }
}