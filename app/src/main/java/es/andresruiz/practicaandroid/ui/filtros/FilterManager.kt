package es.andresruiz.practicaandroid.ui.filtros

import es.andresruiz.domain.models.FilterState
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FilterManager private constructor() {
    private val _filterState = MutableStateFlow(FilterState())
    val filterState: StateFlow<FilterState> = _filterState.asStateFlow()

    private val _filtersApplied = MutableStateFlow(false)
    val filtersApplied: StateFlow<Boolean> = _filtersApplied.asStateFlow()

    private val _dataMinImporte = MutableStateFlow(1)
    val dataMinImporte: StateFlow<Int> = _dataMinImporte.asStateFlow()

    private val _dataMaxImporte = MutableStateFlow(300)
    val dataMaxImporte: StateFlow<Int> = _dataMaxImporte.asStateFlow()

    fun getCurrentFilter(): FilterState = _filterState.value

    fun saveFilter(filterState: FilterState) {
        _filterState.value = filterState
        _filtersApplied.value = isAnyFilterActive(filterState)
    }

    fun updateDataBounds(min: Int, max: Int) {
        val validMin = if (min <= 0) 1 else min
        val validMax = if (max < validMin) validMin else max

        _dataMinImporte.value = validMin
        _dataMaxImporte.value = validMax
    }

    private fun isAnyFilterActive(filterState: FilterState): Boolean {
        return filterState.fechaDesde.isNotEmpty() ||
                filterState.fechaHasta.isNotEmpty() ||
                filterState.importeMin > 1 ||
                filterState.importeMax < 300 ||
                filterState.estados.any { it.value }
    }

    fun clearFilters() {
        _filterState.value = FilterState(
            importeMin = _dataMinImporte.value,
            importeMax = _dataMaxImporte.value
        )
        _filtersApplied.value = false
    }

    companion object {
        @Volatile
        private var instance: FilterManager? = null

        fun getInstance(): FilterManager {
            return instance ?: synchronized(this) {
                instance ?: FilterManager().also { instance = it }
            }
        }
    }
}