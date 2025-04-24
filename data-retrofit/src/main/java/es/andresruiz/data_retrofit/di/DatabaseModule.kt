package es.andresruiz.data_retrofit.di

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import es.andresruiz.data_retrofit.database.FacturaDao
import es.andresruiz.data_retrofit.database.FacturaDatabase
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideFacturaDatabase(
        @ApplicationContext context: Context
    ): FacturaDatabase {
        return Room.databaseBuilder(
            context.applicationContext,
            FacturaDatabase::class.java,
            "factura_database"
        ).build()
    }

    @Provides
    @Singleton
    fun provideFacturaDao(database: FacturaDatabase): FacturaDao {
        return database.facturaDao()
    }
}