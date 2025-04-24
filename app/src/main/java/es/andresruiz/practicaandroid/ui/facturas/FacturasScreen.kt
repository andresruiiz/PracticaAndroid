package es.andresruiz.practicaandroid.ui.facturas

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import es.andresruiz.practicaandroid.R
import es.andresruiz.domain.models.Factura
import es.andresruiz.practicaandroid.navigation.Filtros
import es.andresruiz.practicaandroid.ui.components.FacturaItem
import es.andresruiz.practicaandroid.ui.components.TopBar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacturasScreen(
    navController: NavController,
    viewModel: FacturasViewModel = hiltViewModel()
) {

    val facturas = viewModel.facturas.collectAsState().value
    val showDialog = viewModel.showDialog.collectAsState().value
    val isLoading = viewModel.isLoading.collectAsState().value

    val scrollBehavior =
        TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

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
                isRefreshing = isLoading,
                onRefresh = { viewModel.refreshFacturas() },
                modifier = Modifier.fillMaxSize()
            ) {
                FacturasList(
                    facturas = facturas,
                    onFacturaClick = { viewModel.showDialog() }
                )
            }
        }
    }

    if (showDialog) {
        FacturasDialog(onDismiss = { viewModel.hideDialog() })
    }
}

@Composable
fun FacturasList(
    facturas: List<Factura>,
    onFacturaClick: () -> Unit
) {
    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
    ) {
        items(facturas) { factura ->
            FacturaItem(factura = factura, onClick = onFacturaClick)
        }
    }
}

@Composable
fun FacturasDialog(onDismiss: () -> Unit) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Información",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = "Esta funcionalidad aún no está disponible",
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        shape = RectangleShape,
        confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 30.dp)
                ) {
                    Text("Cerrar")
                }
            }
        }
    )
}