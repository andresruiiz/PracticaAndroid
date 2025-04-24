package es.andresruiz.data_retrofit.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.andresruiz.data_retrofit.database.FacturaDao
import es.andresruiz.data_retrofit.network.FacturasApiService
import es.andresruiz.data_retrofit.repository.DetallesRepository
import es.andresruiz.data_retrofit.repository.FacturasRepository
import es.andresruiz.data_retrofit.repository.NetworkDetallesRepository
import es.andresruiz.data_retrofit.repository.NetworkFacturasRepository
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object RepositoryModule {

    @Provides
    @Singleton
    fun provideFacturasRepository(
        facturasApiService: FacturasApiService,
        facturaDao: FacturaDao
    ): FacturasRepository {
        return NetworkFacturasRepository(facturasApiService, facturaDao)
    }

    @Provides
    @Singleton
    fun provideDetallesRepository(
        facturasApiService: FacturasApiService
    ): DetallesRepository {
        return NetworkDetallesRepository(facturasApiService)
    }
}