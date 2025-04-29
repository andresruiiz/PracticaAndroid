package es.andresruiz.data_retrofit.repository

import es.andresruiz.data_retrofit.database.FacturaDao
import es.andresruiz.data_retrofit.database.FacturaEntity
import es.andresruiz.data_retrofit.network.FacturasApiServiceFactory
import es.andresruiz.domain.models.Factura
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

/**
 * Repositorio que obtiene las facturas desde la API de facturas
 */
interface FacturasRepository {
    fun getFacturas(): Flow<List<Factura>>
    suspend fun refreshFacturas()
}

/**
 * Implementación Network del Repositorio que obtiene las facturas de la API de facturas
 */
class NetworkFacturasRepository(
    private val facturasApiServiceFactory: FacturasApiServiceFactory,
    private val facturaDao: FacturaDao
) : FacturasRepository {

    override fun getFacturas(): Flow<List<Factura>> {
        return facturaDao.getAllFacturas().map { entities ->
            entities.map { entity ->
                Factura(
                    descEstado = entity.descEstado,
                    importeOrdenacion = entity.importeOrdenacion,
                    fecha = entity.fecha
                )
            }
        }
    }

    override suspend fun refreshFacturas() {
        // Obtengo la implementación actual del API service
        val facturasApiService = facturasApiServiceFactory.getApiService()
        val facturasFromApi = facturasApiService.getFacturas().facturas.map {
            FacturaEntity(
                descEstado = it.descEstado,
                importeOrdenacion = it.importeOrdenacion,
                fecha = it.fecha
            )
        }
        facturaDao.deleteAllFacturas()
        facturaDao.insertFacturas(facturasFromApi)
    }
}