package es.andresruiz.practicaandroid.ui.facturas

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import es.andresruiz.practicaandroid.R
import es.andresruiz.domain.models.Factura
import es.andresruiz.practicaandroid.navigation.Filtros
import es.andresruiz.practicaandroid.ui.components.EmptyStateView
import es.andresruiz.practicaandroid.ui.components.ErrorView
import es.andresruiz.practicaandroid.ui.components.FacturaItem
import es.andresruiz.practicaandroid.ui.components.FacturasChart
import es.andresruiz.practicaandroid.ui.components.InfoDialog
import es.andresruiz.practicaandroid.ui.components.TopBar
import es.andresruiz.practicaandroid.ui.facturas.FacturasViewModel.FacturasUiState
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Locale

/**
 * Pantalla en la que se muestran las facturas
 */
@RequiresApi(Build.VERSION_CODES.O)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacturasScreen(
    navController: NavController,
    viewModel: FacturasViewModel = hiltViewModel()
) {

    val uiState = viewModel.uiState.collectAsState().value
    val showDialog = viewModel.showDialog.collectAsState().value
    val isRefreshing = viewModel.isRefreshing.collectAsState().value
    val chartMode = viewModel.chartMode.collectAsState().value

    val scrollBehavior = TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                navController = navController,
                scrollBehavior = scrollBehavior,
                title = stringResource(R.string.facturas),
                backText = stringResource(R.string.consumo),
                actionsIcon = painterResource(id = R.drawable.filtericon_3x),
                actionsOnClick = { navController.navigate(Filtros) }
            )
        },
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            PullToRefreshBox(
                isRefreshing = isRefreshing,
                onRefresh = { viewModel.refreshFacturas() },
                modifier = Modifier.fillMaxSize()
            ) {
                when (uiState) {
                    is FacturasUiState.Loading -> {
                        // Cuando está cargando, no sale nada
                    }
                    is FacturasUiState.Success -> {
                        val inputFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy", Locale("es"))
                        val outputFormatter = DateTimeFormatter.ofPattern("LLL. yy", Locale("es"))

                        // Agrupar y sumar precios por mes (usando YearMonth como clave)
                        val priceData = uiState.facturas
                            .groupBy {
                                YearMonth.from(LocalDate.parse(it.fecha, inputFormatter))
                            }
                            .toSortedMap() // ordena por YearMonth directamente
                            .map { (yearMonth, facturasDelMes) ->
                                val totalImporte = facturasDelMes.sumOf { it.importeOrdenacion }
                                val formattedMonth = yearMonth.atDay(1).format(outputFormatter)
                                Pair(formattedMonth, totalImporte)
                            }

                        // Agrupar y sumar consumos por mes
                        val consumptionData = uiState.facturas
                            .groupBy {
                                YearMonth.from(LocalDate.parse(it.fecha, inputFormatter))
                            }
                            .toSortedMap()
                            .map { (yearMonth, facturasDelMes) ->
                                val totalPunta = facturasDelMes.sumOf { it.consumoPunta }
                                val totalLlenas = facturasDelMes.sumOf { it.consumoLlenas }
                                val formattedMonth = yearMonth.atDay(1).format(outputFormatter)
                                Triple(formattedMonth, totalPunta, totalLlenas)
                            }

                        Column(modifier = Modifier.fillMaxSize()) {
                            FacturasChart(
                                isConsumptionMode = chartMode == FacturasViewModel.ChartMode.CONSUMPTION,
                                onModeToggle = { viewModel.toggleChartMode() },
                                priceData = priceData,
                                consumptionData = consumptionData
                            )

                            FacturasList(
                                facturas = uiState.facturas,
                                onFacturaClick = { viewModel.showDialog() },
                                modifier = Modifier.weight(1f)
                            )
                        }
                    }
                    is FacturasUiState.Empty -> {
                        EmptyStateView(message = uiState.message)
                    }
                    is FacturasUiState.Error -> {
                        ErrorView(
                            message = uiState.message,
                            onRetry = { viewModel.retry() }
                        )
                    }
                }
            }
        }
    }

    if (showDialog) {
        InfoDialog(
            title = stringResource(R.string.informacion),
            message = stringResource(R.string.dialog_message),
            onDismiss = { viewModel.hideDialog() }
        )
    }
}

@Composable
fun FacturasList(
    facturas: List<Factura>,
    onFacturaClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    LazyColumn (modifier = modifier.fillMaxSize()) {
        items(facturas) { factura ->
            FacturaItem(factura = factura, onClick = onFacturaClick)
        }
    }
}