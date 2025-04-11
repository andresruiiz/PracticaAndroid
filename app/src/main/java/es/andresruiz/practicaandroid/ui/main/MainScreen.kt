package es.andresruiz.practicaandroid.ui.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import es.andresruiz.practicaandroid.R
import es.andresruiz.practicaandroid.navigation.Facturas
import es.andresruiz.practicaandroid.navigation.SmartSolar

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController
    ) {
    Scaffold(
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Image(
                        painter = painterResource(id = R.drawable.iber_logo),
                        contentDescription = "Logo de la aplicación"
                    )
                }
            )
        },
        modifier = Modifier.fillMaxSize()) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HomeButtons(modifier = Modifier, navController)
        }
    }
}

@Composable
fun HomeButtons(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Column(
        modifier.fillMaxSize()
            .padding(32.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Button(
            onClick = {
                navController.navigate(Facturas)
            },
            modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.facturas)
            )
        }
        Button(
            onClick = {
                navController.navigate(SmartSolar)
            },
            modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.smart_solar)
            )
        }
    }
}