package es.andresruiz.practicaandroid.ui.smartsolar

import androidx.lifecycle.ViewModel
import es.andresruiz.domain.models.Detalles
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class DetallesViewModel : ViewModel() {

    private val _detalles = MutableStateFlow<Detalles?>(null)
    val detalles: StateFlow<Detalles?> = _detalles.asStateFlow()

    init {
        fetchDetalles()
    }

    private fun fetchDetalles() {
        //Obtengo los detalles, por ahora hardcodeados
        _detalles.value = Detalles(
            "ES002100000000199LJ1FA000",
            "No hemos recibido ninguna solicitud de autoconsumo",
            "Con excedentes y compensación individual - Consumo",
            "Precio PVPC",
            "5kWp"
        )
    }
}