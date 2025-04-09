package es.andresruiz.practicaandroid.ui.facturas

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LargeTopAppBar
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.material3.rememberTopAppBarState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import es.andresruiz.practicaandroid.R
import es.andresruiz.domain.models.Factura
import es.andresruiz.practicaandroid.ui.components.FacturaItem

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacturasScreen(navController: NavController, viewModel: FacturasViewModel = viewModel()) {

    val facturas = viewModel.facturas.collectAsState().value

    val scrollBehavior =
        TopAppBarDefaults.exitUntilCollapsedScrollBehavior(rememberTopAppBarState())

    Scaffold(
        modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
        topBar = {
            FacturasTopBar(navController, scrollBehavior)
        },
    ) { innerPadding ->
        FacturasList(facturas, innerPadding)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FacturasTopBar(navController: NavController, scrollBehavior: TopAppBarScrollBehavior) {
    MediumTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        title = {
            Text(
                text = stringResource(R.string.facturas),
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
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
                        "Consumo",
                        fontSize = 20.sp
                    )
                }
            }
        },
        actions = {
            IconButton(onClick = { /* do something */ }) {

                val icon: Painter = painterResource(id = R.drawable.filtericon_3x)

                Icon(
                    painter = icon,
                    contentDescription = "Botón de filtros",
                    modifier = Modifier
                        .size(30.dp)
                )
            }
        },
        scrollBehavior = scrollBehavior
    )
}

@Composable
fun FacturasList(facturas: List<Factura>, innerPadding: PaddingValues) {
    LazyColumn (
        modifier = Modifier
            .fillMaxSize()
            .padding(innerPadding),
    ) {
        items(facturas) { factura ->
            FacturaItem(factura = factura)
        }
    }
}