package es.andresruiz.domain.models

data class FacturasResponse(
    val numFacturas: Int,
    val facturas: List<Factura>
)
