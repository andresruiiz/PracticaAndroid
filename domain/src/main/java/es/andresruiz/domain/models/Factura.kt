package es.andresruiz.domain.models

// Data class para representar las facturas
data class Factura(
    val descEstado: String,         // Descripción del estado
    val importeOrdenacion: Double,  // Importe de la factura
    val fecha: String               // Fecha
)
