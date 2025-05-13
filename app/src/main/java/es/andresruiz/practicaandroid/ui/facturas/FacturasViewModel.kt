package es.andresruiz.practicaandroid.ui.facturas

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.andresruiz.domain.models.Factura
import es.andresruiz.domain.usecases.GetFacturasUseCase
import es.andresruiz.domain.usecases.RefreshFacturasUseCase
import es.andresruiz.practicaandroid.R
import es.andresruiz.practicaandroid.ui.filtros.FilterManager
import es.andresruiz.core.utils.ResourceProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
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
    private val filterManager: FilterManager,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    // Estado de UI
    private val _uiState = MutableStateFlow<FacturasUiState>(FacturasUiState.Loading)
    val uiState: StateFlow<FacturasUiState> = _uiState.asStateFlow()

    // Estado del popup para el diálogo de filtros
    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    // Estado de carga para el indicador de refresh
    private val _isRefreshing = MutableStateFlow(false)
    val isRefreshing: StateFlow<Boolean> = _isRefreshing.asStateFlow()

    // Almacenamiento local de todas las facturas
    private val _allFacturas = MutableStateFlow<List<Factura>>(emptyList())

    // Estado para el tipo de gráfica (precio o consumo)
    private val _chartMode = MutableStateFlow(ChartMode.PRICE)
    val chartMode: StateFlow<ChartMode> = _chartMode.asStateFlow()

    init {
        // Iniciar la carga de datos
        loadData()

        // Observar cambios en los filtros y en las facturas
        viewModelScope.launch {
            // Combinamos el estado de filterManager con las facturas disponibles
            combine(
                filterManager.filterState,
                _allFacturas
            ) { filterState, facturas ->
                if (facturas.isEmpty()) {
                    // Si no hay facturas, mantenemos el estado de carga o error actual
                    if (_uiState.value is FacturasUiState.Error) {
                        _uiState.value
                    } else if (_isRefreshing.value) {
                        FacturasUiState.Loading
                    } else {
                        FacturasUiState.Empty(resourceProvider.getString(R.string.no_facturas))
                    }
                } else {
                    // Aplicamos filtros a las facturas disponibles
                    val filteredFacturas = filterState.aplicarFiltros(facturas)
                    if (filteredFacturas.isEmpty()) {
                        FacturasUiState.Empty(resourceProvider.getString(R.string.no_facturas_filtros))
                    } else {
                        FacturasUiState.Success(filteredFacturas)
                    }
                }
            }.collect { state ->
                // No actualizamos el estado si estamos refrescando
                if (!_isRefreshing.value || state is FacturasUiState.Error) {
                    _uiState.value = state
                }
            }
        }
    }

    private fun loadData() {
        viewModelScope.launch {
            try {
                getFacturasUseCase().collect { facturas ->
                    _allFacturas.value = facturas

                    // Si la base de datos está vacía, cargamos desde la API
                    if (facturas.isEmpty() && !_isRefreshing.value) {
                        refreshFacturas()
                    } else {
                        // Actualizamos los límites del filtro basados en los datos
                        updateFilterManagerBounds(facturas)
                    }
                }
            } catch (e: Exception) {
                _uiState.value = FacturasUiState.Error(e.message ?: resourceProvider.getString(R.string.error_cargar_facturas))
            }
        }
    }

    // Función para calcular y actualizar los límites del FilterManager
    private fun updateFilterManagerBounds(facturas: List<Factura>) {
        if (facturas.isNotEmpty()) {
            // Obtenemos el mínimo y máximo real
            val actualMinImporte = facturas.minOfOrNull { it.importeOrdenacion }
            val actualMaxImporte = facturas.maxOfOrNull { it.importeOrdenacion }

            // Calculamos límites del slider usando floor y ceil
            val sliderMin = actualMinImporte?.let { floor(it).toInt() } ?: 1
            val sliderMax = actualMaxImporte?.let { ceil(it).toInt() } ?: 300

            // Nos aseguramos que el mínimo sea al menos 1 y que el máximo sea >= mínimo
            val finalSliderMin = sliderMin.coerceAtLeast(1)
            val finalSliderMax = sliderMax.coerceAtLeast(finalSliderMin)

            // Actualizamos el FilterManager con los límites enteros calculados
            filterManager.updateDataBounds(finalSliderMin, finalSliderMax)
        } else {
            filterManager.updateDataBounds(1, 300)
        }
    }

    fun refreshFacturas() {
        // Evitamos iniciar múltiples recargas simultáneas
        if (_isRefreshing.value) return

        viewModelScope.launch {
            _isRefreshing.value = true

            try {
                refreshFacturasUseCase()
            } catch (e: Exception) {
                _uiState.value = FacturasUiState.Error(e.message ?: resourceProvider.getString(R.string.error_regargar_facturas))
            } finally {
                _isRefreshing.value = false
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

    fun toggleChartMode() {
        _chartMode.value = if (_chartMode.value == ChartMode.PRICE) ChartMode.CONSUMPTION else ChartMode.PRICE
    }

    /**
     * Estados posibles de la UI de Facturas
     */
    sealed class FacturasUiState {
        data object Loading : FacturasUiState()
        data class Success(val facturas: List<Factura>) : FacturasUiState()
        data class Empty(val message: String) : FacturasUiState()
        data class Error(val message: String) : FacturasUiState()
    }

    /**
     * Modos del gráfico
     */
    enum class ChartMode {
        PRICE,
        CONSUMPTION
    }
}