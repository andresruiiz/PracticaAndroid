package es.andresruiz.practicaandroid.ui.smartsolar

import android.R.attr.maxWidth
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.max
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import es.andresruiz.practicaandroid.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSolarScreen(navController: NavController) {

    // Estado para controlar la pestaña seleccionada
    var selectedTabIndex by remember { mutableStateOf(0) }

    val tabs = listOf("Mi instalación", "Energía", "Detalles")

    val scrollBehavior =
        TopAppBarDefaults.pinnedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            SmartSolarTopBar(navController, scrollBehavior)
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
            text = "Aquí tienes los datos de tu instalación fotovoltaica en tiempo real.",
            modifier = Modifier.padding(bottom = 24.dp)
        )

        Row(modifier = Modifier.padding(bottom = 8.dp)) {
            Text(
                text = "Autoconsumo: ",
                color = MaterialTheme.colorScheme.secondary
            )
            Text(
                text = "92%"
            )
        }

        Image(
            painter = painterResource(R.drawable.grafico1),
            contentDescription = "Gráfico de autoconsumo",
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
            contentDescription = "Gráfico de página en mantenimiento",
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            contentScale = ContentScale.Crop
        )

        Text(
            text = "Estamos trabajando en mejorar la App. Tus paneles solares siguen produciendo, en breve podrás seguir viendo tu producción solar. Sentimos las molestias.",
            textAlign = TextAlign.Center
        )
    }
}

@Composable
fun DetallesScreen() {

    var showDialog by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        DetallesTextField(
            label = "CAU (Código Autoconsumo)",
            text = "ES002100000000199LJ1FA000",
            onValueChange = {},
            showDialog = {}
        )

        DetallesTextField(
            label = "Estado solicitud alta autoconsumidor",
            text = "No hemos recibido ninguna solicitud de autoconsumo",
            onValueChange = {},
            showDialog = { showDialog = true },
            infoIcon = true
        )

        DetallesTextField(
            label = "Tipo autoconsumo",
            text = "Con excedentes y compensación individual - Consumo",
            onValueChange = {},
            showDialog = {}
        )

        DetallesTextField(
            label = "Compensación de excedentes",
            text = "Precio PVPC",
            onValueChange = {},
            showDialog = {}
        )

        DetallesTextField(
            label = "Potencia de instalación",
            text = "5kWp",
            onValueChange = {},
            showDialog = {}
        )
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = {
                Text(
                    text = "Estado solicitud autoconsumo",
                    modifier = Modifier.fillMaxWidth(),
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.titleMedium
                )
            },
            text = {
                Text(
                    text = "El tiempo estimado de activación de tu autoconsumo es de 1 a 2 meses, éste variará en función de tu comunidad autónoma y distribuidora",
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
                        onClick = { showDialog = false },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 30.dp)
                    ) {
                        Text("Aceptar")
                    }
                }
            }
        )
    }
}

@Composable
private fun DetallesTextField(
    label: String,
    text: String,
    onValueChange: (String) -> Unit,
    infoIcon: Boolean = false,
    showDialog: () -> Unit
) {
    BasicTextField(
        enabled = false,
        value = text,
        onValueChange = onValueChange,
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color.Transparent,
                shape = MaterialTheme.shapes.small
            )
            .padding(bottom = 32.dp),
        textStyle = LocalTextStyle.current.copy(
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Start
        ),
        decorationBox = { innerTextField ->
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    innerTextField()
                    if (infoIcon) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                        ) {
                            IconButton(
                                onClick = { showDialog() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_info),
                                    contentDescription = "Información",
                                    tint = Color(0xFF2196F3)
                                )
                            }
                        }
                    }
                }
                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Gray
                )
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmartSolarTopBar(navController: NavController, scrollBehavior: TopAppBarScrollBehavior) {
    MediumTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        title = {
            Text(
                text = stringResource(R.string.smart_solar),
                fontWeight = FontWeight.Bold,
                fontSize = 35.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            TextButton(onClick = { navController.popBackStack() }) {

                val icon: Painter = painterResource(id = R.drawable.ic_arrow_back)

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        painter = icon,
                        contentDescription = "Botón para volver a consumo",
                        modifier = Modifier
                            .padding(end = 8.dp)
                            .size(20.dp)
                    )
                    Text(
                        "Atrás",
                        fontSize = 20.sp
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}