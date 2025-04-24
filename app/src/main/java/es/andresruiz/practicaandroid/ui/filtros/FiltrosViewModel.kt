package es.andresruiz.practicaandroid.ui.filtros

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.andresruiz.domain.models.FilterState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class FiltrosViewModel @Inject constructor(
    private val filterManager: FilterManager
) : ViewModel() {

    val actualMinImporte: StateFlow<Int> = filterManager.dataMinImporte
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 1)

    val actualMaxImporte: StateFlow<Int> = filterManager.dataMaxImporte
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), 300)

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
        // Asegurar que el mínimo seleccionado no supere al máximo seleccionado
        if (valor <= _importeMax.value) {
            _importeMin.value = valor
        } else {
            // Si lo supera, ajustamos ambos al nuevo valor mínimo
            _importeMin.value = valor
            _importeMax.value = valor
        }
    }

    fun setImporteMax(valor: Int) {
        // Asegurar que el máximo seleccionado no sea menor al mínimo seleccionado
        if (valor >= _importeMin.value) {
            _importeMax.value = valor
        } else {
            // Si es menor, ajustamos ambos al nuevo valor máximo
            _importeMax.value = valor
            _importeMin.value = valor
        }
    }

    fun toggleEstado(estado: String) {
        val updatedEstados = _estados.value.toMutableMap().apply {
            this[estado] = !(this[estado] ?: false)
        }
        _estados.value = updatedEstados
    }

    fun limpiarFiltros() {
        filterManager.clearFilters()

        val clearedFilterState = filterManager.getCurrentFilter()
        _fechaDesde.value = clearedFilterState.fechaDesde
        _fechaHasta.value = clearedFilterState.fechaHasta
        _importeMin.value = clearedFilterState.importeMin
        _importeMax.value = clearedFilterState.importeMax
        _estados.value = clearedFilterState.estados
    }

    fun aplicarFiltros() {
        // Crear un nuevo FilterState con los valores actuales
        val filterState = FilterState(
            fechaDesde = _fechaDesde.value,
            fechaHasta = _fechaHasta.value,
            importeMin = _importeMin.value.coerceIn(actualMinImporte.value, actualMaxImporte.value),
            importeMax = _importeMax.value.coerceIn(actualMinImporte.value, actualMaxImporte.value),
            estados = _estados.value
        )

        // Validar que min no sea mayor que max después del coerceIn
        val finalFilterState = if (filterState.importeMin > filterState.importeMax) {
            filterState.copy(importeMax = filterState.importeMin)
        } else {
            filterState
        }

        // Guardar el estado en el FilterManager
        filterManager.saveFilter(filterState)
    }
}