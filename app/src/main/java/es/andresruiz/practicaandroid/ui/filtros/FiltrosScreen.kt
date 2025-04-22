package es.andresruiz.practicaandroid.ui.filtros

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SliderDefaults
import androidx.compose.material3.SliderDefaults.Thumb
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import es.andresruiz.practicaandroid.R
import es.andresruiz.practicaandroid.ui.components.TopBar
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltrosScreen(
    navController: NavController,
    viewModel: FiltrosViewModel = viewModel()
) {

    val fechaDesde by viewModel.fechaDesde.collectAsState()
    val fechaHasta by viewModel.fechaHasta.collectAsState()
    val importeMin = viewModel.importeMin.collectAsState().value.toFloat()
    val importeMax = viewModel.importeMax.collectAsState().value.toFloat()
    val sliderValueRange = importeMin..importeMax
    val estados by viewModel.estados.collectAsState()

    val scrollBehavior =
        TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                navController = navController,
                scrollBehavior = scrollBehavior,
                title = stringResource(R.string.filtrar_facturas),
                actionsIcon = painterResource(id = R.drawable.close_icon),
                actionsOnClick = { navController.popBackStack() }
            )
        },
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.Top
        ) {
            // Selector de fechas
            DateFilterSection(viewModel, fechaDesde, fechaHasta)

            Divider(
                modifier = Modifier.padding(vertical = 32.dp),
                color = MaterialTheme.colorScheme.secondary
            )

            // Sección de importe
            AmountFilterSection(
                sliderValueRange = sliderValueRange,
                onSliderValueChange = {
                    viewModel.setImporteMin(it.start.toInt())
                    viewModel.setImporteMax(it.endInclusive.toInt())
                },
                maxFacturaValue = 300f
            )

            Divider(
                modifier = Modifier.padding(vertical = 32.dp),
                color = MaterialTheme.colorScheme.secondary
            )

            // Sección de estado
            StatusFilterSection(
                isPagadasChecked = estados["Pagada"] == true,
                isAnuladasChecked = estados["Anulada"] == true,
                isCuotaFijaChecked = estados["Cuota Fija"] == true,
                isPendientesChecked = estados["Pendiente de pago"] == true,
                isPlanPagoChecked = estados["Plan de pago"] == true,
                onPagadasChange = { viewModel.toggleEstado("Pagada") },
                onAnuladasChange = { viewModel.toggleEstado("Anulada") },
                onCuotaFijaChange = { viewModel.toggleEstado("Cuota Fija") },
                onPendientesChange = { viewModel.toggleEstado("Pendiente de pago") },
                onPlanPagoChange = { viewModel.toggleEstado("Plan de pago") }
            )

            // Botones
            Spacer(modifier = Modifier.weight(1f))

            ButtonsSection(
                onClearFilters = {
                    viewModel.limpiarFiltros()
                },
                onApplyFilters = {
                    viewModel.aplicarFiltros()
                    navController.popBackStack() // Volver a la pantalla de facturas después de aplicar los filtros
                }
            )
        }
    }
}

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateFilterSection(
    viewModel: FiltrosViewModel,
    fechaDesde: String,
    fechaHasta: String
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    Text(
        text = "Con fecha de emisión",
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        modifier = Modifier.padding(top = 16.dp, bottom = 8.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Desde
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Desde:",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            SimpleDateField(
                value = fechaDesde,
                placeholder = "día/mes/año",
                onClick = { showStartDatePicker = true }
            )
        }

        Spacer(modifier = Modifier.width(16.dp))

        // Hasta
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Hasta:",
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = 4.dp)
            )
            SimpleDateField(
                value = fechaHasta,
                placeholder = "día/mes/año",
                onClick = { showEndDatePicker = true }
            )
        }
    }

    // Selector de fecha "Desde"
    if (showStartDatePicker) {
        DatePickerModal(
            onDateSelected = { millis ->
                millis?.let {
                    viewModel.setFechaDesde(convertMillisToDate(it))
                }
            },
            onDismiss = { showStartDatePicker = false }
        )
    }

    // Selector de fecha "Hasta"
    if (showEndDatePicker) {
        DatePickerModal(
            onDateSelected = { millis ->
                millis?.let {
                    viewModel.setFechaHasta(convertMillisToDate(it))
                }
            },
            onDismiss = { showEndDatePicker = false }
        )
    }
}

@Composable
fun SimpleDateField(
    value: String,
    placeholder: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            //.fillMaxWidth()
            .height(38.dp)
            .background(
                color = MaterialTheme.colorScheme.secondary,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (value.isEmpty()) placeholder else value,
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit
) {
    val datePickerState = rememberDatePickerState()

    DatePickerDialog(
        onDismissRequest = onDismiss,
        shape = RectangleShape,
        confirmButton = {
            Button(onClick = {
                onDateSelected(datePickerState.selectedDateMillis)
                onDismiss()
            }) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = "Cancelar",
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    ) {
        DatePicker(state = datePickerState)
    }
}

fun convertMillisToDate(millis: Long): String {
    val formatter = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
    return formatter.format(Date(millis))
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountFilterSection(
    sliderValueRange: ClosedFloatingPointRange<Float>,
    onSliderValueChange: (ClosedFloatingPointRange<Float>) -> Unit,
    maxFacturaValue: Float
) {
    Text(
        text = "Por un importe",
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        modifier = Modifier.padding(bottom = 16.dp)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${sliderValueRange.start.toInt()} € - ${sliderValueRange.endInclusive.toInt()} €",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "1 €", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
            Text(text = "$maxFacturaValue €", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
        }

        RangeSlider(
            value = sliderValueRange,
            onValueChange = onSliderValueChange,
            valueRange = 1f..maxFacturaValue,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier.padding(top = 16.dp),
            startThumb = {
                Thumb(
                    modifier = Modifier
                        .size(30.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                    interactionSource = remember { MutableInteractionSource() },
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                )
            },
            endThumb = {
                Thumb(
                    modifier = Modifier
                        .size(30.dp)
                        .background(MaterialTheme.colorScheme.primary, shape = CircleShape),
                    interactionSource = remember { MutableInteractionSource() },
                    colors = SliderDefaults.colors(thumbColor = MaterialTheme.colorScheme.primary)
                )
//                Box(
//                    modifier = Modifier
//                        .size(30.dp)
//                        .clip(CircleShape)
//                        .background(MaterialTheme.colorScheme.primary)
//                )
            },
            track = { sliderState ->
                SliderDefaults.Track(
                    modifier = Modifier.height(4.dp),
                    rangeSliderState = sliderState,
                    drawStopIndicator = null,
                    thumbTrackGapSize = 0.dp
                )
            }
        )
    }
}

@Composable
fun StatusFilterSection(
    isPagadasChecked: Boolean,
    isAnuladasChecked: Boolean,
    isCuotaFijaChecked: Boolean,
    isPendientesChecked: Boolean,
    isPlanPagoChecked: Boolean,
    onPagadasChange: (Boolean) -> Unit,
    onAnuladasChange: (Boolean) -> Unit,
    onCuotaFijaChange: (Boolean) -> Unit,
    onPendientesChange: (Boolean) -> Unit,
    onPlanPagoChange: (Boolean) -> Unit
) {
    Text(
        text = "Por estado",
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        modifier = Modifier.padding(bottom = 6.dp)
    )

    CheckboxItem(
        text = "Pagadas",
        isChecked = isPagadasChecked,
        onCheckedChange = onPagadasChange
    )

    CheckboxItem(
        text = "Anuladas",
        isChecked = isAnuladasChecked,
        onCheckedChange = onAnuladasChange
    )

    CheckboxItem(
        text = "Cuota Fija",
        isChecked = isCuotaFijaChecked,
        onCheckedChange = onCuotaFijaChange
    )

    CheckboxItem(
        text = "Pendientes de pago",
        isChecked = isPendientesChecked,
        onCheckedChange = onPendientesChange
    )

    CheckboxItem(
        text = "Plan de pago",
        isChecked = isPlanPagoChecked,
        onCheckedChange = onPlanPagoChange
    )
}

@Composable
fun ButtonsSection(
    onClearFilters: () -> Unit,
    onApplyFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button(
            onClick = onApplyFilters,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 30.dp)
        ) {
            Text("Aplicar")
        }

        TextButton(
            onClick = onClearFilters,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text("Eliminar filtros")
        }
    }
}

@Composable
fun CheckboxItem(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF8BC34A),
                uncheckedColor = Color(0xFFE0E0E0)
            )
        )
        Text(
            text = text
        )
    }
}