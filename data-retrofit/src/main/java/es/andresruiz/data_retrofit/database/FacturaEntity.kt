package es.andresruiz.data_retrofit.database

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "facturas")
data class FacturaEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    @ColumnInfo(name = "desc_estado") val descEstado: String,
    @ColumnInfo(name = "importe_ordenacion") val importeOrdenacion: Double,
    @ColumnInfo(name = "consumo_punta") val consumoPunta: Double,
    @ColumnInfo(name = "consumo_llenas") val consumoLlenas: Double,
    @ColumnInfo(name = "fecha") val fecha: String
)
