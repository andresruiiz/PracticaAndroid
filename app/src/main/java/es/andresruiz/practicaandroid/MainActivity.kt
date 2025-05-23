package es.andresruiz.practicaandroid

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import dagger.hilt.android.AndroidEntryPoint
import es.andresruiz.practicaandroid.navigation.NavigationWrapper
import es.andresruiz.practicaandroid.ui.theme.PracticaAndroidTheme

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PracticaAndroidTheme {
                NavigationWrapper()
            }
        }
    }
}