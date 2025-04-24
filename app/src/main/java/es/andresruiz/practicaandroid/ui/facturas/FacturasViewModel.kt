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
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.ceil
import kotlin.math.floor

@HiltViewModel
class FacturasViewModel @Inject constructor(
    private val repository: FacturasRepository,
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

    init {
        loadFacturasFromDatabase()

        // Observar cambios en los filtros
        viewModelScope.launch {
            filterManager.filterState.collectLatest { filterState ->
                _facturas.value = filterState.aplicarFiltros(_allFacturas.value)
            }
        }
    }

    private fun loadFacturasFromDatabase() {
        viewModelScope.launch {
            repository.getFacturas().collect { facturas ->
                if (facturas.isEmpty() && !_isLoading.value) {
                    // Cargo las facturas desde la API si la base de datos está vacía
                    refreshFacturas()
                } else {
                    _allFacturas.value = facturas
                    // Calculo y actualizo los límites en FilterManager
                    updateFilterManagerBounds(facturas)
                    // Aplico los filtros actuales a las facturas cargadas
                    _facturas.value = filterManager.getCurrentFilter().aplicarFiltros(facturas)
                }
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
            try {
                repository.refreshFacturas()
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun showDialog() {
        _showDialog.value = true
    }

    fun hideDialog() {
        _showDialog.value = false
    }
}