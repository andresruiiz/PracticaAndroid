package es.andresruiz.domain.usecases

import es.andresruiz.domain.models.Factura
import kotlinx.coroutines.flow.Flow

/**
 * Caso de uso para obtener la lista de facturas
 */
interface GetFacturasUseCase {
    operator fun invoke(): Flow<List<Factura>>
}