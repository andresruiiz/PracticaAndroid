package es.andresruiz.data_retrofit.network

import co.infinum.retromock.meta.Mock
import co.infinum.retromock.meta.MockResponse
import es.andresruiz.domain.models.FacturasResponse
import retrofit2.http.GET

interface FacturasApiService {

    @Mock
    @MockResponse(body = "mock_facturas.json")
    @GET("facturas")
    suspend fun getFacturas(): FacturasResponse
}