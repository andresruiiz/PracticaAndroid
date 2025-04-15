package es.andresruiz.data_retrofit.network

import android.annotation.SuppressLint
import android.content.Context
import co.infinum.retromock.Retromock
import es.andresruiz.domain.UseMockProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

@SuppressLint("StaticFieldLeak")
object RetrofitInstance {
    private const val BASE_URL = "https://d25ffc74-c8b3-45aa-9094-eb029fba47a0.mock.pstmn.io"

    private lateinit var context: Context
    private lateinit var useMockProvider: UseMockProvider

    fun init(context: Context, useMockProvider: UseMockProvider) {
        this.context = context.applicationContext
        this.useMockProvider = useMockProvider
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

    val facturasApiService: FacturasApiService
        get() {
            return if (useMockProvider.isMockEnabled()) {
                retromock.create(FacturasApiService::class.java)
            } else {
                retrofit.create(FacturasApiService::class.java)
            }
    }
}