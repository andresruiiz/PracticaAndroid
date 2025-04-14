package es.andresruiz.data_retrofit.repository

import es.andresruiz.data_retrofit.network.FacturasApiService
import es.andresruiz.domain.models.Factura

/**
 * Repositorio que obtiene las facturas desde la API de facturas
 */
interface FacturasRepository {
    suspend fun getFacturas(): List<Factura>
}

/**
 * Implementación Network del Repositorio que obtiene las facturas de la API de facturas
 */
class NetworkFacturasRepository(
    private val facturasApiService: FacturasApiService
) : FacturasRepository {
    override suspend fun getFacturas(): List<Factura> = facturasApiService.getFacturas().facturas
}