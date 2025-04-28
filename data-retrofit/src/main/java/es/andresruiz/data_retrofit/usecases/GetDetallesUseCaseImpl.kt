package es.andresruiz.data_retrofit.usecases

import es.andresruiz.data_retrofit.repository.DetallesRepository
import es.andresruiz.domain.models.Detalles
import es.andresruiz.domain.usecases.GetDetallesUseCase
import javax.inject.Inject

/**
 * Implementación del caso de uso para obtener los detalles de Smart Solar
 */
class GetDetallesUseCaseImpl @Inject constructor(
    private val detallesRepository: DetallesRepository
): GetDetallesUseCase {

    override suspend fun invoke(): Detalles {
        return detallesRepository.getDetalles()
    }
}