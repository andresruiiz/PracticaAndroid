package es.andresruiz.practicaandroid.ui.filtros

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FiltrosViewModel : ViewModel() {

    private val _fechaDesde = MutableStateFlow("")
    val fechaDesde: StateFlow<String> = _fechaDesde.asStateFlow()

    private val _fechaHasta = MutableStateFlow("")
    val fechaHasta: StateFlow<String> = _fechaHasta.asStateFlow()

    private val _importeMin = MutableStateFlow(0)
    val importeMin: StateFlow<Int> = _importeMin.asStateFlow()

    private val _importeMax = MutableStateFlow(300)
    val importeMax: StateFlow<Int> = _importeMax.asStateFlow()

    private val _estados = MutableStateFlow(
        mapOf(
            "Pagadas" to false,
            "Anuladas" to false,
            "Cuota Fija" to false,
            "Pendientes de pago" to false,
            "Plan de pago" to false
        )
    )
    val estados: StateFlow<Map<String, Boolean>> = _estados.asStateFlow()

    fun setFechaDesde(fecha: String) {
        _fechaDesde.value = fecha
    }

    fun setFechaHasta(fecha: String) {
        _fechaHasta.value = fecha
    }

    fun setImporteMin(valor: Int) {
        _importeMin.value = valor
    }

    fun setImporteMax(valor: Int) {
        _importeMax.value = valor
    }

    fun toggleEstado(estado: String) {
        _estados.value = _estados.value.toMutableMap().apply {
            this[estado] = !(this[estado] ?: false)
        }
    }

    fun limpiarFiltros() {
        _fechaDesde.value = ""
        _fechaHasta.value = ""
        _importeMin.value = 1
        _importeMax.value = 300
        _estados.value = _estados.value.mapValues { false }
    }
}