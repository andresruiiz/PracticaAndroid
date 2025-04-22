package es.andresruiz.practicaandroid.ui.filtros

import androidx.lifecycle.ViewModel
import es.andresruiz.domain.models.FilterState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FiltrosViewModel : ViewModel() {

    // Usamos el FilterManager para obtener y actualizar el estado compartido
    private val filterManager = FilterManager.getInstance()

    private val _fechaDesde = MutableStateFlow(filterManager.getCurrentFilter().fechaDesde)
    val fechaDesde: StateFlow<String> = _fechaDesde.asStateFlow()

    private val _fechaHasta = MutableStateFlow(filterManager.getCurrentFilter().fechaHasta)
    val fechaHasta: StateFlow<String> = _fechaHasta.asStateFlow()

    private val _importeMin = MutableStateFlow(filterManager.getCurrentFilter().importeMin)
    val importeMin: StateFlow<Int> = _importeMin.asStateFlow()

    private val _importeMax = MutableStateFlow(filterManager.getCurrentFilter().importeMax)
    val importeMax: StateFlow<Int> = _importeMax.asStateFlow()

    private val _estados = MutableStateFlow(filterManager.getCurrentFilter().estados)
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
        val updatedEstados = _estados.value.toMutableMap().apply {
            this[estado] = !(this[estado] ?: false)
        }
        _estados.value = updatedEstados
    }

    fun limpiarFiltros() {
        _fechaDesde.value = ""
        _fechaHasta.value = ""
        _importeMin.value = 1
        _importeMax.value = 300
        _estados.value = _estados.value.mapValues { false }

        aplicarFiltros()
    }

    fun aplicarFiltros() {
        // Crear un nuevo FilterState con los valores actuales
        val filterState = FilterState(
            fechaDesde = _fechaDesde.value,
            fechaHasta = _fechaHasta.value,
            importeMin = _importeMin.value,
            importeMax = _importeMax.value,
            estados = _estados.value
        )

        // Guardar el estado en el FilterManager
        filterManager.saveFilter(filterState)
    }
}