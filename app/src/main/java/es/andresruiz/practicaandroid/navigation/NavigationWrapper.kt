package es.andresruiz.practicaandroid.navigation

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import es.andresruiz.practicaandroid.ui.facturas.FacturasScreen
import es.andresruiz.practicaandroid.ui.filtros.FiltrosScreen
import es.andresruiz.practicaandroid.ui.main.MainScreen
import es.andresruiz.practicaandroid.ui.smartsolar.SmartSolarScreen

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun NavigationWrapper() {
    val navController = rememberNavController()
    NavHost(navController = navController, startDestination = Main) {
        composable<Main> {
            MainScreen(navController)
        }

        composable<Facturas> {
            FacturasScreen(navController)
        }

        composable<SmartSolar> {
            SmartSolarScreen(navController)
        }

        composable<Filtros> {
            FiltrosScreen(navController)
        }
    }
}