package es.andresruiz.practicaandroid.ui.facturas

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import es.andresruiz.data_retrofit.database.FacturasRepositoryProvider
import es.andresruiz.data_retrofit.repository.FacturasRepository
import es.andresruiz.domain.models.Factura
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class FacturasViewModel(private val repository: FacturasRepository) : ViewModel() {

    // Estado de las facturas
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
    }

    private fun loadFacturasFromDatabase() {
        viewModelScope.launch {
            repository.getFacturas().collect { facturas ->
                if (facturas.isEmpty()) {
                    refreshFacturas() // Cargo las facturas desde la API si la base de datos está vacía
                } else {
                    _facturas.value = facturas
                }
            }
        }
    }

    fun refreshFacturas() {
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

    companion object {
        fun provideFactory(context: Context): ViewModelProvider.Factory {
            val repository = FacturasRepositoryProvider.provideRepository(context)
            return object : ViewModelProvider.Factory {
                override fun <T : ViewModel> create(modelClass: Class<T>): T {
                    if (modelClass.isAssignableFrom(FacturasViewModel::class.java)) {
                        @Suppress("UNCHECKED_CAST")
                        return FacturasViewModel(repository) as T
                    }
                    throw IllegalArgumentException("Unknown ViewModel class")
                }
            }
        }
    }
}