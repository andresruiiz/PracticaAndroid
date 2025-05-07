package es.andresruiz.practicaandroid.ui.smartsolar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import es.andresruiz.domain.models.Detalles
import es.andresruiz.domain.usecases.GetDetallesUseCase
import es.andresruiz.practicaandroid.R
import es.andresruiz.practicaandroid.util.ResourceProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * ViewModel para la pantalla de detalles de SmartSolar
 * Gestiona la carga y visualización de los detalles de la instalación
 */
@HiltViewModel
class DetallesViewModel @Inject constructor(
    private val getDetallesUseCase: GetDetallesUseCase,
    private val resourceProvider: ResourceProvider
) : ViewModel() {

    private val _uiState = MutableStateFlow<DetallesUiState>(DetallesUiState.Loading)
    val uiState: StateFlow<DetallesUiState> = _uiState.asStateFlow()

    init {
        fetchDetalles()
    }

    fun fetchDetalles() {
        viewModelScope.launch {
            _uiState.value = DetallesUiState.Loading
            try {
                val detalles = getDetallesUseCase()

                if (detalles.cau.isBlank() && detalles.estadoSolicitud.isBlank()) {
                    _uiState.value = DetallesUiState.Empty(resourceProvider.getString(R.string.no_detalles))
                } else {
                    _uiState.value = DetallesUiState.Success(detalles)
                }
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

/**
 * Estados posibles de la UI de Detalles
 */
sealed class DetallesUiState {
    data object Loading : DetallesUiState()
    data class Success(val detalles: Detalles) : DetallesUiState()
    data class Empty(val message: String) : DetallesUiState()
    data class Error(val message: String) : DetallesUiState()
}