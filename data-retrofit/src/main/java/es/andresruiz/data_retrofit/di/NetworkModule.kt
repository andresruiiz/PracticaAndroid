package es.andresruiz.data_retrofit.di

import android.content.Context
import co.infinum.retromock.Retromock
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import es.andresruiz.data_retrofit.network.FacturasApiService
import es.andresruiz.domain.UseMockProvider
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object NetworkModule {

    private const val BASE_URL = "https://5e39f79a-1210-4746-b61f-4671459628b8.mock.pstmn.io"

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
    fun provideFacturasApiService(
        retrofit: Retrofit,
        retromock: Retromock,
        mockProvider: UseMockProvider
    ): FacturasApiService {
        return if (mockProvider.isMockEnabled()) {
            retromock.create(FacturasApiService::class.java)
        } else {
            retrofit.create(FacturasApiService::class.java)
        }
    }

}