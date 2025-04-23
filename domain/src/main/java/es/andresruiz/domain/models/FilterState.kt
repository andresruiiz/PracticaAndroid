package es.andresruiz.domain.models

import java.text.SimpleDateFormat
import java.util.Locale

data class FilterState(
    val fechaDesde: String = "",
    val fechaHasta: String = "",
    val importeMin: Int = 1,
    val importeMax: Int = 300,
    val estados: Map<String, Boolean> = mapOf(
        "Pagada" to false,
        "Anulada" to false,
        "Cuota Fija" to false,
        "Pendiente de pago" to false,
        "Plan de pago" to false
    )
) {
    fun aplicarFiltros(facturas: List<Factura>): List<Factura> {
        return facturas.filter { factura ->
            // Filtro por fechas
            val cumpleFechaDesde = if (fechaDesde.isNotEmpty()) {
                val fechaFactura = factura.fecha
                try {
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val fechaDesdeDate = formatter.parse(fechaDesde)
                    val fechaFacturaDate = formatter.parse(fechaFactura)
                    fechaFacturaDate != null && fechaDesdeDate != null && fechaFacturaDate >= fechaDesdeDate
                } catch (e: Exception) {
                    true // Si hay error haciendo el parsing, no filtramos
                }
            } else true

            val cumpleFechaHasta = if (fechaHasta.isNotEmpty()) {
                val fechaFactura = factura.fecha
                try {
                    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val fechaHastaDate = formatter.parse(fechaHasta)
                    val fechaFacturaDate = formatter.parse(fechaFactura)
                    fechaFacturaDate != null && fechaHastaDate != null && fechaFacturaDate <= fechaHastaDate
                } catch (e: Exception) {
                    true
                }
            } else true

            // Filtro por importe
            val cumpleImporte = factura.importeOrdenacion >= importeMin && factura.importeOrdenacion <= importeMax

            // Filtro por estado
            val estadosSeleccionados = estados.filter { it.value }.keys
            val cumpleEstado = if (estadosSeleccionados.isEmpty()) {
                true
            } else {
                estadosSeleccionados.contains(factura.descEstado)
            }

            cumpleFechaDesde && cumpleFechaHasta && cumpleImporte && cumpleEstado
        }
    }
}
