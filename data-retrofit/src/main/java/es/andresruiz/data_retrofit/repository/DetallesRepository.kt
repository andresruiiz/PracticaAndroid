package es.andresruiz.data_retrofit.repository

import es.andresruiz.data_retrofit.network.FacturasApiService
import es.andresruiz.domain.models.Detalles

/**
 * Repositorio que obtiene los detalles desde la API de detalles
 */
interface DetallesRepository {
    suspend fun getDetalles(): Detalles
}

/**
 * Implementación Network del Repositorio que obtiene las facturas de la API de facturas
 */
class NetworkDetallesRepository(
    private val facturasApiService: FacturasApiService,
) : DetallesRepository {

    override suspend fun getDetalles(): Detalles {
        return facturasApiService.getDetallesSmartSolar()
    }
}