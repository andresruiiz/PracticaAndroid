package es.andresruiz.practicaandroid.ui.facturas

import androidx.lifecycle.ViewModel
import es.andresruiz.domain.models.Factura
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FacturasViewModel : ViewModel() {

    // Estado de las facturas
    private val _facturas = MutableStateFlow<List<Factura>>(emptyList())
    val facturas: StateFlow<List<Factura>> = _facturas.asStateFlow()

    // Estado del popup
    private val _showDialog = MutableStateFlow(false)
    val showDialog: StateFlow<Boolean> = _showDialog.asStateFlow()

    init {
        getFacturas()
    }

    // Obtengo las facturas, por ahora hardcodeadas
    fun getFacturas() {
        _facturas.value = listOf(
            Factura(descEstado = "Pendiente de pago", importeOrdenacion = 54.56, fecha = "31/08/2020"),
            Factura(descEstado = "Pendiente de pago", importeOrdenacion = 67.54, fecha = "31/07/2020"),
            Factura(descEstado = "Pendiente de pago", importeOrdenacion = 56.38, fecha = "22/06/2020"),
            Factura(descEstado = "Pagada", importeOrdenacion = 57.38, fecha = "31/05/2020")
        )
    }

    fun showDialog() {
        _showDialog.value = true
    }

    fun hideDialog() {
        _showDialog.value = false
    }
}