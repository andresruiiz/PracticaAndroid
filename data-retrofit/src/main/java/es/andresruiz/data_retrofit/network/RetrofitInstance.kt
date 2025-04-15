package es.andresruiz.data_retrofit.network

import android.annotation.SuppressLint
import android.content.Context
import co.infinum.retromock.Retromock
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@SuppressLint("StaticFieldLeak")
object RetrofitInstance {
    private const val BASE_URL = "https://d25ffc74-c8b3-45aa-9094-eb029fba47a0.mock.pstmn.io"

    private lateinit var context: Context

    fun init(context: Context) {
        this.context = context.applicationContext
    }

    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    private val retromock: Retromock by lazy {
        Retromock.Builder()
            .retrofit(retrofit)
            .defaultBodyFactory(context.assets::open)
            .build()
    }

    val facturasApiService: FacturasApiService by lazy {
        if (useMock()) {
            retromock.create(FacturasApiService::class.java)
        } else {
            retrofit.create(FacturasApiService::class.java)
        }
    }

    // Función para determinar si está activado el sistema de Mocks
    private fun useMock(): Boolean {
        return true // True a mano por ahora
    }
}