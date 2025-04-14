package es.andresruiz.domain.models

// Data class para representar los Detalles de Smart Solar
data class Detalles(
    val cau: String,
    val estadoSolicitud: String,
    val tipoAutoconsumo: String,
    val compensacionExcendentes: String,
    val potenciaInstalacion: String
)
