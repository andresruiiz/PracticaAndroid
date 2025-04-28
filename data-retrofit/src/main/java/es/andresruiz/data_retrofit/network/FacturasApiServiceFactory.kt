package es.andresruiz.data_retrofit.network

import es.andresruiz.domain.UseMockProvider
import javax.inject.Inject

class FacturasApiServiceFactory @Inject constructor(
    private val retrofitService: FacturasApiService,
    private val retromockService: FacturasApiService,
    private val useMockProvider: UseMockProvider
) {
    fun getApiService(): FacturasApiService {
        return if (useMockProvider.isMockEnabled()) {
            retromockService
        } else {
            retrofitService
        }
    }
}