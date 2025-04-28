package es.andresruiz.practicaandroid.ui.facturas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.andresruiz.domain.models.Factura
import es.andresruiz.domain.usecases.GetFacturasUseCase
import es.andresruiz.domain.usecases.RefreshFacturasUseCase
import es.andresruiz.practicaandroid.ui.filtros.FilterManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val getFacturasUseCase: GetFacturasUseCase,
    private val refreshFacturasUseCase: RefreshFacturasUseCase,
    private val filterManager: FilterManager
) : ViewModel() {

    // Estado de todas las facturas (sin filtrar)
    private val _allFacturas = MutableStateFlow<List<Factura>>(emptyList())

    // Estado de las facturas filtradas que se muestran
    private val _facturas = MutableStateFlow<List<Factura>>(emptyList())
    val facturas: StateFlow<List<Factura>> = _facturas.asStateFlow()

    // Estado del popup
    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    // Estado de carga
    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    // Estado de error
    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()

    // Estado de vacío
    private val _isEmpty = MutableStateFlow(false)
    val isEmpty: StateFlow<Boolean> = _isEmpty.asStateFlow()

    init {
        loadFacturasFromDatabase()

        // Observar cambios en los filtros
        viewModelScope.launch {
            filterManager.filterState.collectLatest { filterState ->
                val filteredFacturas = filterState.aplicarFiltros(_allFacturas.value)
                _facturas.value = filteredFacturas
                _isEmpty.value = filteredFacturas.isEmpty() && _allFacturas.value.isNotEmpty()
            }
        }
    }

    private fun loadFacturasFromDatabase() {
        viewModelScope.launch {
            try {
                getFacturasUseCase().collect { facturas ->
                    if (facturas.isEmpty() && !_isLoading.value) {
                        // Cargo las facturas desde la API si la base de datos está vacía
                        refreshFacturas()
                    } else {
                        _allFacturas.value = facturas
                        // Calculo y actualizo los límites en FilterManager
                        updateFilterManagerBounds(facturas)
                        // Aplico los filtros actuales a las facturas cargadas
                        val filteredFacturas = filterManager.getCurrentFilter().aplicarFiltros(facturas)
                        _facturas.value = filteredFacturas
                        _isEmpty.value = filteredFacturas.isEmpty() && facturas.isNotEmpty()
                        _error.value = null
                    }
                }
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido al cargar facturas"
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

    fun refreshFacturas() {
        if (_isLoading.value) return

        viewModelScope.launch {
            _isLoading.value = true
            _error.value = null

            try {
                refreshFacturasUseCase()
            } catch (e: Exception) {
                _error.value = e.message ?: "Error desconocido al refrescar facturas"
            } finally {
                _isLoading.value = false
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

    sealed class FacturasUiState {
        data object Loading: FacturasUiState()
        data class Success(val facturas: List<Factura>): FacturasUiState()
        data class Empty(val message: String): FacturasUiState()
        data class Error(val message: String): FacturasUiState()
    }

    // Convertir los estados separados al estado unificado para la UI
    val uiState: StateFlow<FacturasUiState> = MutableStateFlow<FacturasUiState>(FacturasUiState.Loading).apply {
        viewModelScope.launch {
            launch {
                _isLoading.collect { isLoading ->
                    if (isLoading) {
                        value = FacturasUiState.Loading
                    } else if (_error.value != null) {
                        value = FacturasUiState.Error(_error.value ?: "Error desconocido")
                    } else if (_isEmpty.value) {
                        value = FacturasUiState.Empty("No hay facturas que coincidan con los filtros seleccionados")
                    } else if (_facturas.value.isNotEmpty()) {
                        value = FacturasUiState.Success(_facturas.value)
                    } else if (_facturas.value.isEmpty() && _allFacturas.value.isEmpty()) {
                        // No hay facturas en absoluto
                        value = FacturasUiState.Empty("No hay facturas disponibles")
                    }
                }
            }

            launch {
                _error.collect { errorMsg ->
                    if (errorMsg != null && !_isLoading.value) {
                        value = FacturasUiState.Error(errorMsg)
                    }
                }
            }

            launch {
                _isEmpty.collect { empty ->
                    if (empty && !_isLoading.value && _error.value == null) {
                        value = FacturasUiState.Empty("No hay facturas que coincidan con los filtros seleccionados")
                    }
                }
            }

            launch {
                _facturas.collect { facturas ->
                    if (facturas.isNotEmpty() && !_isLoading.value && _error.value == null) {
                        value = FacturasUiState.Success(facturas)
                    }
                }
            }
        }
    }.asStateFlow()
}