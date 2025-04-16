package es.andresruiz.data_retrofit.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FacturaDao {
    @Query("SELECT * FROM facturas")
    fun getAllFacturas(): Flow<List<FacturaEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFacturas(facturas: List<FacturaEntity>)

    @Query("DELETE FROM facturas")
    suspend fun deleteAllFacturas()
}