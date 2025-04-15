package es.andresruiz.practicaandroid.ui.main

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CenterAlignedTopAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.navigation.NavController
import es.andresruiz.practicaandroid.PracticaAndroidApplication
import es.andresruiz.practicaandroid.R
import es.andresruiz.practicaandroid.dataStore
import es.andresruiz.practicaandroid.navigation.Facturas
import es.andresruiz.practicaandroid.navigation.SmartSolar
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainScreen(
    navController: NavController
    ) {

    val context = LocalContext.current.applicationContext as PracticaAndroidApplication
    val dataStore = context.dataStore
    val coroutineScope = remember { CoroutineScope(Dispatchers.IO) }

    var useMock by remember { mutableStateOf(false) }

    // Consulto en DataStore el estado de use_mock (para activar o no los Mocks)
    LaunchedEffect(Unit) {
        dataStore.data.collect { preferences ->
            useMock = preferences[booleanPreferencesKey("use_mock")] == true
        }
    }

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
        modifier = Modifier.fillMaxSize()
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HomeButtons(modifier = Modifier, navController)

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = "Usar sistema de Mocks",
                    modifier = Modifier.padding(end = 12.dp)
                )
                Switch(
                    checked = useMock,
                    onCheckedChange = { isChecked ->
                        useMock = isChecked
                        coroutineScope.launch {
                            dataStore.edit { preferences ->
                                preferences[booleanPreferencesKey("use_mock")] = isChecked
                            }
                        }
                        Toast.makeText(
                            context,
                            if (isChecked) "Sistema de mocks activado" else "Sistema de mocks desactivado",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                )
            }
        }
    }
}

@Composable
fun HomeButtons(
    modifier: Modifier = Modifier,
    navController: NavController
) {
    Column(
        modifier.padding(horizontal = 32.dp),
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
            modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp)
        ) {
            Text(
                text = stringResource(R.string.smart_solar)
            )
        }
    }
}