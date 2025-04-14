package es.andresruiz.data_retrofit.network

import es.andresruiz.domain.models.FacturasResponse
import retrofit2.http.GET

interface FacturasApiService {

    @GET("facturas")
    suspend fun getFacturas(): FacturasResponse
}