package es.andresruiz.data_retrofit.network

import co.infinum.retromock.meta.Mock
import co.infinum.retromock.meta.MockCircular
import co.infinum.retromock.meta.MockResponse
import co.infinum.retromock.meta.MockResponses
import es.andresruiz.domain.models.Detalles
import es.andresruiz.domain.models.FacturasResponse
import retrofit2.http.GET

interface FacturasApiService {

    @Mock
    @MockCircular
    @MockResponses(
        MockResponse(body = "mock_facturas_no_pagadas.json"),
        MockResponse(body = "mock_facturas.json"),
        MockResponse(body = "mock_facturas_pagadas.json")
    )
    @GET("facturas")
    suspend fun getFacturas(): FacturasResponse

    @Mock
    @MockResponse(body = "mock_detalles.json")
    @GET("detalles")
    suspend fun getDetallesSmartSolar(): Detalles
}