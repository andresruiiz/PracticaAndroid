package es.andresruiz.data_retrofit.database

import android.content.Context
import es.andresruiz.data_retrofit.network.RetrofitInstance
import es.andresruiz.data_retrofit.repository.NetworkFacturasRepository

object FacturasRepositoryProvider {
    fun provideRepository(context: Context): NetworkFacturasRepository {
        val database = FacturaDatabase.getDatabase(context)
        return NetworkFacturasRepository(
            facturasApiService = RetrofitInstance.facturasApiService,
            facturaDao = database.facturaDao()
        )
    }
}