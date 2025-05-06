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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDefaults
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RangeSlider
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SelectableDates
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
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import es.andresruiz.core.utils.convertMillisToDate
import es.andresruiz.core.utils.getCurrentDateInMillis
import es.andresruiz.practicaandroid.R
import es.andresruiz.practicaandroid.ui.components.CheckboxItem
import es.andresruiz.practicaandroid.ui.components.TopBar
import es.andresruiz.practicaandroid.ui.theme.AppShapes
import es.andresruiz.practicaandroid.ui.theme.AppTheme

/**
 * Pantalla para mostrar los filtros
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FiltrosScreen(
    navController: NavController,
    viewModel: FiltrosViewModel = hiltViewModel()
) {

    val fechaDesde by viewModel.fechaDesde.collectAsState()
    val fechaHasta by viewModel.fechaHasta.collectAsState()

    // Valores seleccionados por el usuario en el slider
    val selectedImporteMin by viewModel.importeMin.collectAsState()
    val selectedImporteMax by viewModel.importeMax.collectAsState()

    // Límites reales del slider obtenidos del ViewModel
    val actualMinImporte by viewModel.actualMinImporte.collectAsState()
    val actualMaxImporte by viewModel.actualMaxImporte.collectAsState()

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
                // Los valores seleccionados actualmente por el usuario
                selectedRangeStart = selectedImporteMin.toFloat(),
                selectedRangeEnd = selectedImporteMax.toFloat(),
                // Los límites MÍNIMO y MÁXIMO posibles según los datos
                actualMinBound = actualMinImporte.toFloat(),
                actualMaxBound = actualMaxImporte.toFloat(),
                // Callback para cuando el usuario mueve el slider
                onSliderValueChange = { newRange ->
                    viewModel.setImporteMin(newRange.start.toInt())
                    viewModel.setImporteMax(newRange.endInclusive.toInt())
                }
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

/**
 * Sección con los selectores de fechas
 */
@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DateFilterSection(
    viewModel: FiltrosViewModel,
    fechaDesde: String,
    fechaHasta: String
) {
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Fecha actual en milisegundos (límite superior para ambos calendarios)
    val currentDateMillis = getCurrentDateInMillis()

    // Fecha "desde" en milisegundos (límite inferior para el calendario "hasta")
    val fechaDesdeMillis = if (fechaDesde.isNotEmpty()) {
        es.andresruiz.core.utils.convertDateStringToMillis(fechaDesde)
    } else null

    // Fecha "hasta" en milisegundos (límite superior para el calendario "desde")
    val fechaHastaMillis = if (fechaHasta.isNotEmpty()) {
        es.andresruiz.core.utils.convertDateStringToMillis(fechaHasta)
    } else null

    Text(
        text = stringResource(R.string.con_fecha_emision),
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        modifier = Modifier.padding(top = AppTheme.Spacing.medium, bottom = AppTheme.Spacing.small)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
    ) {
        // Desde
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.desde),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = AppTheme.Spacing.extraSmall)
            )
            SimpleDateField(
                value = fechaDesde,
                placeholder = stringResource(R.string.dia_mes_anyo),
                onClick = { showStartDatePicker = true }
            )
        }

        Spacer(modifier = Modifier.width(AppTheme.Spacing.medium))

        // Hasta
        Column(
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.hasta),
                color = MaterialTheme.colorScheme.secondary,
                fontSize = 14.sp,
                modifier = Modifier.padding(bottom = AppTheme.Spacing.extraSmall)
            )
            SimpleDateField(
                value = fechaHasta,
                placeholder = stringResource(R.string.dia_mes_anyo),
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
            onDismiss = { showStartDatePicker = false },
            maxDate = if (fechaHastaMillis != null) {
                // No permitir seleccionar fechas posteriores a la fecha "hasta"
                minOf(fechaHastaMillis, currentDateMillis)
            } else {
                // Si no hay fecha "hasta", el límite es la fecha actual
                currentDateMillis
            },
            minDate = null, // Sin restricción en la fecha mínima para "desde"
            initialSelectedDateMillis = fechaDesdeMillis ?: currentDateMillis
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
            onDismiss = { showEndDatePicker = false },
            maxDate = currentDateMillis, // No permitir seleccionar fechas posteriores a hoy
            minDate = fechaDesdeMillis, // No permitir seleccionar fechas anteriores a "desde"
            initialSelectedDateMillis = fechaHastaMillis ?: currentDateMillis
        )
    }
}

/**
 * Componente para los campos de fecha
 */
@Composable
fun SimpleDateField(
    value: String,
    placeholder: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .height(38.dp)
            .background(
                color = MaterialTheme.colorScheme.secondary,
                shape = AppShapes.DateFieldShape
            )
            .clickable { onClick() }
            .padding(horizontal = AppTheme.Spacing.medium),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = if (value.isEmpty()) placeholder else value,
            color = MaterialTheme.colorScheme.onSecondary
        )
    }
}

/**
 * Componente para el selector de fecha
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerModal(
    onDateSelected: (Long?) -> Unit,
    onDismiss: () -> Unit,
    initialSelectedDateMillis: Long? = null,
    maxDate: Long? = null,
    minDate: Long? = null
) {
    // Asegurar que la fecha inicial esté dentro de los límites permitidos
    val validInitialDate = when {
        initialSelectedDateMillis == null -> maxDate ?: getCurrentDateInMillis()
        maxDate != null && initialSelectedDateMillis > maxDate -> maxDate
        minDate != null && initialSelectedDateMillis < minDate -> minDate
        else -> initialSelectedDateMillis
    }

    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = validInitialDate,
        selectableDates = object : SelectableDates {
            override fun isSelectableDate(utcTimeMillis: Long): Boolean {
                // Restringir la selección entre las fechas mínima y máxima
                return (minDate == null || utcTimeMillis >= minDate) &&
                        (maxDate == null || utcTimeMillis <= maxDate)
            }

            override fun isSelectableYear(year: Int): Boolean = true
        }
    )

    DatePickerDialog(
        onDismissRequest = onDismiss,
        shape = AppShapes.DialogShape,
        confirmButton = {
            Button(onClick = {
                // Validar la fecha seleccionada antes de confirmar
                val selectedMillis = datePickerState.selectedDateMillis
                if (selectedMillis != null) {
                    val isValid = (minDate == null || selectedMillis >= minDate) &&
                            (maxDate == null || selectedMillis <= maxDate)

                    if (isValid) {
                        onDateSelected(selectedMillis)
                        onDismiss()
                    }
                    // Si la fecha no es válida, no hacemos nada y mantenemos el diálogo abierto
                } else {
                    // Si no hay fecha seleccionada, cerramos el diálogo sin seleccionar
                    onDismiss()
                }
            }) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss
            ) {
                Text(
                    text = stringResource(R.string.cancelar),
                    color = MaterialTheme.colorScheme.onBackground
                )
            }
        }
    ) {
        DatePicker(
            state = datePickerState,
            colors = DatePickerDefaults.colors(
                containerColor = MaterialTheme.colorScheme.background,
                titleContentColor = MaterialTheme.colorScheme.primary,
                headlineContentColor = MaterialTheme.colorScheme.onBackground,
                weekdayContentColor = MaterialTheme.colorScheme.onSurface,
                subheadContentColor = MaterialTheme.colorScheme.onSurface
            )
        )
    }
}

/**
 * Sección con el selector de importe
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AmountFilterSection(
    selectedRangeStart: Float,
    selectedRangeEnd: Float,
    actualMinBound: Float,
    actualMaxBound: Float,
    onSliderValueChange: (ClosedFloatingPointRange<Float>) -> Unit
) {

    // Asegurarse de que los límites reales sean válidos (max >= min)
    val validActualMin = if (actualMinBound > actualMaxBound) actualMaxBound else actualMinBound
    val validActualMax = if (actualMaxBound < actualMinBound) actualMinBound else actualMaxBound

    // Aseguro de que los valores seleccionados estén dentro de los límites reales válidos
    val sliderCurrentValue = selectedRangeStart.coerceIn(validActualMin, validActualMax)..selectedRangeEnd.coerceIn(validActualMin, validActualMax)

    // Crear el rango posible para el slider
    val sliderValueRange = validActualMin..validActualMax

    Text(
        text = stringResource(R.string.por_importe),
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        modifier = Modifier.padding(bottom = AppTheme.Spacing.medium)
    )

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "${sliderCurrentValue.start.toInt()} € - ${sliderCurrentValue.endInclusive.toInt()} €",
            color = MaterialTheme.colorScheme.primary,
            fontWeight = FontWeight.Medium
        )
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppTheme.Spacing.extraSmall)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(text = "${validActualMin.toInt()} €", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
            Text(text = "${validActualMax.toInt()} €", color = MaterialTheme.colorScheme.secondary, fontSize = 14.sp)
        }

        RangeSlider(
            value = sliderCurrentValue,
            onValueChange = onSliderValueChange,
            valueRange = sliderValueRange,
            colors = SliderDefaults.colors(
                thumbColor = MaterialTheme.colorScheme.primary,
                activeTrackColor = MaterialTheme.colorScheme.primary,
                inactiveTrackColor = MaterialTheme.colorScheme.secondary
            ),
            modifier = Modifier.padding(top = AppTheme.Spacing.medium),
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

/**
 * Sección con los checkboxes de estado
 */
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
        text = stringResource(R.string.por_estado),
        fontWeight = FontWeight.Medium,
        fontSize = 16.sp,
        modifier = Modifier.padding(bottom = AppTheme.Spacing.small)
    )

    CheckboxItem(
        text = stringResource(R.string.pagadas),
        isChecked = isPagadasChecked,
        onCheckedChange = onPagadasChange
    )

    CheckboxItem(
        text = stringResource(R.string.anuladas),
        isChecked = isAnuladasChecked,
        onCheckedChange = onAnuladasChange
    )

    CheckboxItem(
        text = stringResource(R.string.cuota_fija),
        isChecked = isCuotaFijaChecked,
        onCheckedChange = onCuotaFijaChange
    )

    CheckboxItem(
        text = stringResource(R.string.pendientes),
        isChecked = isPendientesChecked,
        onCheckedChange = onPendientesChange
    )

    CheckboxItem(
        text = stringResource(R.string.plan_pago),
        isChecked = isPlanPagoChecked,
        onCheckedChange = onPlanPagoChange
    )
}

/**
 * Sección con los botones de aplicar y eliminar
 */
@Composable
fun ButtonsSection(
    onClearFilters: () -> Unit,
    onApplyFilters: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = AppTheme.Spacing.medium),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Button(
            onClick = onApplyFilters,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = AppTheme.Spacing.extraLarge)
        ) {
            Text(stringResource(R.string.aplicar))
        }

        TextButton(
            onClick = onClearFilters,
            colors = ButtonDefaults.textButtonColors(
                contentColor = MaterialTheme.colorScheme.secondary
            )
        ) {
            Text(stringResource(R.string.eliminar_filtros))
        }
    }
}