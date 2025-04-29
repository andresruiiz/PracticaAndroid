package es.andresruiz.domain.usecases

import es.andresruiz.domain.models.Detalles

/**
 * Caso de uso para obtener los detalles de Smart Solar
 */
interface GetDetallesUseCase {
    suspend operator fun invoke(): Detalles
}