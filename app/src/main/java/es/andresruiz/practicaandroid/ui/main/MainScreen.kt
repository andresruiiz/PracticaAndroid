package es.andresruiz.practicaandroid.ui.main

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavController
import es.andresruiz.practicaandroid.BuildConfig
import es.andresruiz.practicaandroid.PracticaAndroidApplication
import es.andresruiz.practicaandroid.R
import es.andresruiz.practicaandroid.dataStore
import es.andresruiz.practicaandroid.navigation.Facturas
import es.andresruiz.practicaandroid.navigation.SmartSolar
import es.andresruiz.practicaandroid.ui.theme.AppShapes
import es.andresruiz.practicaandroid.ui.theme.AppTheme
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Pantalla principal de la aplicación
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(navController: NavController) {
    val context = LocalContext.current.applicationContext as PracticaAndroidApplication
    val dataStore = context.dataStore
    val coroutineScope = remember { CoroutineScope(Dispatchers.IO) }

    var useMock by remember { mutableStateOf(false) }

    // Consulto en DataStore el estado de use_mock (solo en DEBUG)
    if (BuildConfig.DEBUG) {
        LaunchedEffect(Unit) {
            dataStore.data.collect { preferences ->
                useMock = preferences[booleanPreferencesKey("use_mock")] == true
            }
        }
    }

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.iber_logo),
                            contentDescription = stringResource(R.string.desc_logo),
                            modifier = Modifier
                                .height(56.dp)
                                .padding(vertical = 8.dp)
                        )
                    }
                },
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    titleContentColor = MaterialTheme.colorScheme.primary
                )
            )
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Título de bienvenida
            Text(
                text = stringResource(R.string.bienvenido),
                style = MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(vertical = 16.dp)
            )

            // Tarjetas de opciones principales
            HomeButtons(
                modifier = Modifier.fillMaxWidth(),
                navController = navController
            )

            Spacer(modifier = Modifier.weight(1f))

            // Sección de configuración (solo en DEBUG)
            if (BuildConfig.DEBUG) {
                DevSettingsCard(
                    useMock = useMock,
                    onToggleMock = { isChecked ->
                        useMock = isChecked
                        coroutineScope.launch {
                            dataStore.edit { preferences ->
                                preferences[booleanPreferencesKey("use_mock")] = isChecked
                            }
                        }
                        Toast.makeText(
                            context,
                            if (isChecked) R.string.mocks_activado else R.string.mocks_desactivado,
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }
}

/**
 * Sección con los botones de la pantalla de inicio
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeButtons(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(AppTheme.Spacing.medium)
    ) {
        // Facturas Card
        ElevatedCard(
            onClick = { navController.navigate(Facturas) },
            shape = AppShapes.CardShape,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = AppTheme.Elevation.small),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.Spacing.large),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.Spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_facturas),
                    contentDescription = stringResource(R.string.desc_ic_facturas),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = stringResource(R.string.facturas),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.subtitulo_facturas),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Smart Solar Card
        ElevatedCard(
            onClick = { navController.navigate(SmartSolar) },
            shape = AppShapes.CardShape,
            elevation = CardDefaults.elevatedCardElevation(defaultElevation = AppTheme.Elevation.small),
            colors = CardDefaults.elevatedCardColors(
                containerColor = MaterialTheme.colorScheme.surface,
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(AppTheme.Spacing.large),
                horizontalArrangement = Arrangement.spacedBy(AppTheme.Spacing.medium),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_smart_solar),
                    contentDescription = stringResource(R.string.desc_ic_smartsolar),
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
                Column {
                    Text(
                        text = stringResource(R.string.smart_solar),
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.subtitulo_smartsolar),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * Sección con las ajustes de desarrollador (activar mocks)
 */
@Composable
fun DevSettingsCard(
    useMock: Boolean,
    onToggleMock: (Boolean) -> Unit
) {
    OutlinedCard(
        shape = AppShapes.CardShape,
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = AppTheme.Spacing.large)
    ) {
        Column(
            modifier = Modifier.padding(AppTheme.Spacing.medium),
            verticalArrangement = Arrangement.spacedBy(AppTheme.Spacing.small)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppTheme.Spacing.small)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_dev),
                    contentDescription = stringResource(R.string.config_desarrollo),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.config_desarrollo),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Divider(
                modifier = Modifier.padding(vertical = AppTheme.Spacing.small),
                color = MaterialTheme.colorScheme.secondary
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.usar_mocks),
                        style = MaterialTheme.typography.titleSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.subtitulo_mocks),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
                Switch(
                    checked = useMock,
                    onCheckedChange = onToggleMock,
                    colors = SwitchDefaults.colors(
                        checkedThumbColor = MaterialTheme.colorScheme.primary,
                        checkedTrackColor = MaterialTheme.colorScheme.primaryContainer,
                        uncheckedThumbColor = MaterialTheme.colorScheme.outline,
                        uncheckedTrackColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                )
            }
        }
    }
}