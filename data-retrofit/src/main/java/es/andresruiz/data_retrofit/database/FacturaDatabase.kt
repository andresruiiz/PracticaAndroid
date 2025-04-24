package es.andresruiz.data_retrofit.database

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FacturaEntity::class], version = 1, exportSchema = false)
abstract class FacturaDatabase : RoomDatabase() {
    abstract fun facturaDao(): FacturaDao
}