package es.andresruiz.data_retrofit.di

import android.content.Context
import co.infinum.retromock.Retromock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import es.andresruiz.data_retrofit.network.FacturasApiService
import es.andresruiz.data_retrofit.network.FacturasApiServiceFactory
import es.andresruiz.domain.UseMockProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://b2937533-70f8-461f-b493-6a10a566b2b3.mock.pstmn.io"

    @Provides
    @Singleton
    fun provideRetrofit(): Retrofit {
        return Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    @Provides
    @Singleton
    fun provideRetromock(
        retrofit: Retrofit,
        @ApplicationContext context: Context
    ): Retromock {
        return Retromock.Builder()
            .retrofit(retrofit)
            .defaultBodyFactory(context.assets::open)
            .build()
    }

    @Provides
    @Singleton
    fun provideFacturasApiServiceFactory(
        retrofit: Retrofit,
        retromock: Retromock,
        mockProvider: UseMockProvider
    ): FacturasApiServiceFactory {
        return FacturasApiServiceFactory(
            retrofit.create(FacturasApiService::class.java),
            retromock.create(FacturasApiService::class.java),
            mockProvider
        )
    }

    @Provides
    fun provideFacturasApiService(
        factory: FacturasApiServiceFactory
    ): FacturasApiService {
        return factory.getApiService()
    }

}