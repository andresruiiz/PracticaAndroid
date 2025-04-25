package es.andresruiz.practicaandroid.ui.facturas

import androidx.compose.foundation.layout.Box
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
import es.andresruiz.practicaandroid.ui.components.InfoDialog
import es.andresruiz.practicaandroid.ui.components.LoadingView
import es.andresruiz.practicaandroid.ui.components.TopBar
import es.andresruiz.practicaandroid.ui.facturas.FacturasViewModel.FacturasUiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacturasScreen(
    navController: NavController,
    viewModel: FacturasViewModel = hiltViewModel()
) {

    val uiState = viewModel.uiState.collectAsState().value
    val showDialog = viewModel.showDialog.collectAsState().value

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
                isRefreshing = uiState is FacturasUiState.Loading,
                onRefresh = { viewModel.refreshFacturas() },
                modifier = Modifier.fillMaxSize()
            ) {
                when (uiState) {
                    is FacturasUiState.Loading -> {
                        // Cuando está cargando, no sale nada
                    }
                    is FacturasUiState.Success -> {
                        FacturasList(
                            facturas = uiState.facturas,
                            onFacturaClick = { viewModel.showDialog() }
                        )
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