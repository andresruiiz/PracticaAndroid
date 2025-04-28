package es.andresruiz.practicaandroid.ui.smartsolar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import es.andresruiz.practicaandroid.R
import es.andresruiz.practicaandroid.ui.components.EmptyStateView
import es.andresruiz.practicaandroid.ui.components.ErrorView
import es.andresruiz.practicaandroid.ui.components.InfoDialog
import es.andresruiz.practicaandroid.ui.components.LoadingView
import es.andresruiz.practicaandroid.ui.components.ReadOnlyTextField
import es.andresruiz.practicaandroid.ui.components.TopBar


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSolarScreen(navController: NavController) {

    // Estado para controlar la pestaña seleccionada
    var selectedTabIndex by remember { mutableStateOf(0) }

    val tabs = listOf(stringResource(R.string.mi_instalacion), stringResource(R.string.energia), stringResource(R.string.detalles))

    val scrollBehavior =
        TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            TopBar(
                navController = navController,
                scrollBehavior = scrollBehavior,
                title = stringResource(R.string.smart_solar),
                backText = stringResource(R.string.atras)
            )
        },
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
        ) {
            ScrollableTabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = MaterialTheme.colorScheme.background,
                contentColor = MaterialTheme.colorScheme.onBackground,
                edgePadding = 0.dp,
                indicator = { tabPositions ->
                    // Indicador negro personalizado
                    TabRowDefaults.SecondaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = MaterialTheme.colorScheme.onBackground,
                    )
                },
                divider = {} // Sin línea divisoria inferior
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                textAlign = TextAlign.Center
                            )
                        },
                        selectedContentColor = MaterialTheme.colorScheme.onBackground,
                        unselectedContentColor = MaterialTheme.colorScheme.onBackground.copy(alpha = 0.7f)
                    )
                }
            }

            when (selectedTabIndex) {
                0 -> MiInstalacionScreen()
                1 -> EnergiaScreen()
                2 -> DetallesScreen()
            }
        }
    }
}

@Composable
fun MiInstalacionScreen() {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Text(
            text = stringResource(R.string.texto_instalacion),
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(modifier = Modifier.padding(bottom = 8.dp)) {
            Text(
                text = stringResource(R.string.autoconsumo),
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = stringResource(R.string.porc_instalacion)
            )
        }

        Image(
            painter = painterResource(R.drawable.grafico1),
            contentDescription = stringResource(R.string.desc_graf_autoconsumo),
            modifier = Modifier.fillMaxWidth(),
            contentScale = ContentScale.Crop
        )
    }
}

@Composable
fun EnergiaScreen() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(80.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Image(
            painter = painterResource(R.drawable.plan_gestiones),
            contentDescription = stringResource(R.string.desc_graf_mantenimiento),
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            contentScale = ContentScale.Crop
        )

        Text(
            text = stringResource(R.string.texto_energia),
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DetallesScreen(viewModel: DetallesViewModel = hiltViewModel()) {

    val uiState by viewModel.uiState.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        when (val state = uiState) {
            is DetallesUiState.Loading -> {
                LoadingView()
            }

            is DetallesUiState.Success -> {
                val detalles = state.detalles

                ReadOnlyTextField(
                    label = stringResource(R.string.cau),
                    text = detalles.cau
                )

                ReadOnlyTextField(
                    label = stringResource(R.string.estado_solicitud),
                    text = detalles.estadoSolicitud,
                    onInfoClick = { showDialog = true },
                    infoIcon = true
                )

                ReadOnlyTextField(
                    label = stringResource(R.string.tipo_autoconsumo),
                    text = detalles.tipoAutoconsumo
                )

                ReadOnlyTextField(
                    label = stringResource(R.string.compensacion_excedentes),
                    text = detalles.compensacionExcendentes
                )

                ReadOnlyTextField(
                    label = stringResource(R.string.potencia_instalacion),
                    text = detalles.potenciaInstalacion
                )
            }

            is DetallesUiState.Empty -> {
                EmptyStateView(message = state.message)
            }

            is DetallesUiState.Error -> {
                ErrorView(
                    message = state.message,
                    onRetry = { viewModel.retry() }
                )
            }
        }
    }

    if (showDialog) {
        InfoDialog(
            title = stringResource(R.string.titulo_dialog_detalles),
            message = stringResource(R.string.mensaje_dialog_detalles),
            buttonText = stringResource(R.string.aceptar),
            onDismiss = { showDialog = false }
        )
    }
}