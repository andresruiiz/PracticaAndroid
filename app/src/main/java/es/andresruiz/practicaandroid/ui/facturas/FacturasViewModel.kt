package es.andresruiz.practicaandroid.ui.facturas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.andresruiz.data_retrofit.repository.FacturasRepository
import es.andresruiz.domain.models.Factura
import es.andresruiz.practicaandroid.ui.filtros.FilterManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.floor

/**
 * ViewModel para la pantalla de facturas
 * Gestiona la carga, filtrado y visualización de las facturas
 */
@HiltViewModel
class FacturasViewModel @Inject constructor(
    private val repository: FacturasRepository,
    private val filterManager: FilterManager
) : ViewModel() {

    // Estado de todas las facturas (sin filtrar)
    private val _allFacturas = MutableStateFlow<List<Factura>>(emptyList())

    // Estado de la UI
    private val _uiState = MutableStateFlow<FacturasUiState>(FacturasUiState.Loading)
    val uiState: StateFlow<FacturasUiState> = _uiState.asStateFlow()

    // Estado del popup
    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    init {
        loadFacturasFromDatabase()

        // Observar cambios en los filtros
        viewModelScope.launch {
            filterManager.filterState.collectLatest { filterState ->
                if (_allFacturas.value.isNotEmpty()) {
                    val filteredFacturas = filterState.aplicarFiltros(_allFacturas.value)

                    if (filteredFacturas.isEmpty()) {
                        _uiState.value = FacturasUiState.Empty(
                            "No hay facturas que coincidan con los filtros seleccionados"
                        )
                    } else {
                        _uiState.value = FacturasUiState.Success(filteredFacturas)
                    }
                }
            }
        }
    }

    private fun loadFacturasFromDatabase() {

        _uiState.value = FacturasUiState.Loading

        viewModelScope.launch {
            try {
                repository.getFacturas()
                    .catch { e ->
                        _uiState.value = FacturasUiState.Error("Error al cargar las facturas: ${e.message ?: "Error desconocido"}")
                    }
                    .collect { facturas ->
                        if (facturas.isEmpty()) {
                            try {
                                // Cargo las facturas desde la API si la base de datos está vacía
                                refreshFacturas()
                            } catch (e: Exception) {
                                _uiState.value = FacturasUiState.Error("Error al refrescar las facturas: ${e.message ?: "Error desconocido"}")
                            }
                        } else {
                            _allFacturas.value = facturas
                            // Calculo y actualizo los límites en FilterManager
                            updateFilterManagerBounds(facturas)
                            // Aplico los filtros actuales a las facturas cargadas
                            val filteredFacturas = filterManager.getCurrentFilter().aplicarFiltros(facturas)

                            if (filteredFacturas.isEmpty()) {
                                _uiState.value = FacturasUiState.Empty(
                                    "No hay facturas que coincidan con los filtros seleccionados"
                                )
                            } else {
                                _uiState.value = FacturasUiState.Success(filteredFacturas)
                            }
                        }
                }
            } catch (e: Exception) {
                updateUiState(FacturasUiState.Error("Error al cargar las facturas: ${e.message ?: "Error desconocido"}"))
            }
        }
    }

    // Función para calcular y actualizar los límites del FilterManager
    private fun updateFilterManagerBounds(facturas: List<Factura>) {
        if (facturas.isNotEmpty()) {
            // Obtengo el mínimo y máximo real
            val actualMinImporte = facturas.minOfOrNull { it.importeOrdenacion }
            val actualMaxImporte = facturas.maxOfOrNull { it.importeOrdenacion }

            // Calculo límites del slider usando floor y ceil
            val sliderMin = actualMinImporte?.let { floor(it).toInt() } ?: 1
            val sliderMax = actualMaxImporte?.let { ceil(it).toInt() } ?: 300

            // Aseguro que el mínimo sea al menos 1 y que el máximo sea >= mínimo
            val finalSliderMin = sliderMin.coerceAtLeast(1)
            val finalSliderMax = sliderMax.coerceAtLeast(finalSliderMin)

            // Actualizo el FilterManager con los límites enteros calculados
            filterManager.updateDataBounds(finalSliderMin, finalSliderMax)
        } else {
            filterManager.updateDataBounds(1, 300)
        }
    }

    private fun updateUiState(newState: FacturasUiState) {
        _uiState.value = newState
    }

    fun refreshFacturas() {
        if (_uiState.value is FacturasUiState.Loading) return

        viewModelScope.launch {
            _uiState.value = FacturasUiState.Loading
            try {
                repository.refreshFacturas()
            } catch (e: Exception) {
                updateUiState(FacturasUiState.Error("Error al refrescar las facturas: ${e.message ?: "Error desconocido"}"))
            }
        }
    }

    fun retry() {
        refreshFacturas()
    }

    fun showDialog() {
        _showDialog.value = true
    }

    fun hideDialog() {
        _showDialog.value = false
    }

    /**
     * Estados posibles de la UI de Facturas
     */
    sealed class FacturasUiState {
        data object Loading: FacturasUiState()
        data class Success(val facturas: List<Factura>): FacturasUiState()
        data class Empty(val message: String): FacturasUiState()
        data class Error(val message: String): FacturasUiState()
    }
}