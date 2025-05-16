package es.andresruiz.domain.models

// Data class para representar las facturas
data class Factura(
    val descEstado: String,         // Descripción del estado
    val importeOrdenacion: Double,  // Importe de la factura
    val consumoPunta: Double,              // Consumo en kWh en hora punta
    val consumoLlenas: Double,             // Consumo en kWh en hora llena
    val fecha: String               // Fecha
)
