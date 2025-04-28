package es.andresruiz.data_retrofit.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.andresruiz.data_retrofit.database.FacturaDao
import es.andresruiz.data_retrofit.network.FacturasApiServiceFactory
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
        facturasApiServiceFactory: FacturasApiServiceFactory,
        facturaDao: FacturaDao
    ): FacturasRepository {
        return NetworkFacturasRepository(facturasApiServiceFactory, facturaDao)
    }

    @Provides
    @Singleton
    fun provideDetallesRepository(
        facturasApiServiceFactory: FacturasApiServiceFactory
    ): DetallesRepository {
        return NetworkDetallesRepository(facturasApiServiceFactory)
    }
}