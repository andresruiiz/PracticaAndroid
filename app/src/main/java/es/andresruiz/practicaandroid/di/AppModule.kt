package es.andresruiz.practicaandroid.di

import android.content.Context
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import es.andresruiz.domain.UseMockProvider
import es.andresruiz.practicaandroid.PracticaAndroidApplication
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {

    @Provides
    @Singleton
    fun provideUseMockProvider(
        @ApplicationContext context: Context
    ): UseMockProvider {
        return context as PracticaAndroidApplication
    }
}