package es.andresruiz.data_retrofit.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import es.andresruiz.data_retrofit.repository.DetallesRepository
import es.andresruiz.data_retrofit.repository.FacturasRepository
import es.andresruiz.data_retrofit.usecases.GetDetallesUseCaseImpl
import es.andresruiz.data_retrofit.usecases.GetFacturasUseCaseImpl
import es.andresruiz.data_retrofit.usecases.RefreshFacturasUseCaseImpl
import es.andresruiz.domain.usecases.GetDetallesUseCase
import es.andresruiz.domain.usecases.GetFacturasUseCase
import es.andresruiz.domain.usecases.RefreshFacturasUseCase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object UseCaseModule {

    @Provides
    @Singleton
    fun provideGetFacturasUseCase(
        facturasRepository: FacturasRepository
    ): GetFacturasUseCase {
        return GetFacturasUseCaseImpl(facturasRepository)
    }

    @Provides
    @Singleton
    fun provideRefreshFacturasUseCase(
        facturasRepository: FacturasRepository
    ): RefreshFacturasUseCase {
        return RefreshFacturasUseCaseImpl(facturasRepository)
    }

    @Provides
    @Singleton
    fun provideGetDetallesUseCase(
        detallesRepository: DetallesRepository
    ): GetDetallesUseCase {
        return GetDetallesUseCaseImpl(detallesRepository)
    }
}