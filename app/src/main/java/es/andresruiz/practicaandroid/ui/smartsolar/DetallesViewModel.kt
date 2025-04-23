package es.andresruiz.practicaandroid.ui.smartsolar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import es.andresruiz.data_retrofit.network.RetrofitInstance
import es.andresruiz.data_retrofit.repository.DetallesRepository
import es.andresruiz.data_retrofit.repository.NetworkDetallesRepository
import es.andresruiz.domain.models.Detalles
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DetallesViewModel() : ViewModel() {

    private val detallesRepository = NetworkDetallesRepository(
        RetrofitInstance.facturasApiService
    )

    private val _uiState = MutableStateFlow<DetallesUiState>(DetallesUiState.Loading)
    val uiState: StateFlow<DetallesUiState> = _uiState.asStateFlow()

    init {
        fetchDetalles()
    }

    fun fetchDetalles() {
        viewModelScope.launch {
            _uiState.value = DetallesUiState.Loading
            try {
                val detalles = detallesRepository.getDetalles()
                _uiState.value = DetallesUiState.Success(detalles)
            } catch (e: Exception) {
                _uiState.value = DetallesUiState.Error(e.message ?: "Error al cargar los detalles")
            }
        }
    }

    // Función para reintentar la carga si falla
    fun retry() {
        fetchDetalles()
    }
}

sealed class DetallesUiState {
    data object Loading : DetallesUiState()
    data class Success(val detalles: Detalles) : DetallesUiState()
    data class Error(val message: String) : DetallesUiState()
}