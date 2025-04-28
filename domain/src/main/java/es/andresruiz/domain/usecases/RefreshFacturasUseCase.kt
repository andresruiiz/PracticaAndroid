package es.andresruiz.domain.usecases

/**
 * Caso de uso para refrescar la lista de facturas desde la API
 */
interface RefreshFacturasUseCase {
    suspend operator fun invoke()
}