package es.andresruiz.practicaandroid.ui.theme

import android.os.Build
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp

private val DarkColorScheme = darkColorScheme(
    primary = Green,
    onPrimary = White,
    primaryContainer = Green.copy(alpha = 0.8f),
    onPrimaryContainer = White,

    secondary = LightGrey,
    onSecondary = Black,
    secondaryContainer = LightGrey.copy(alpha = 0.7f),
    onSecondaryContainer = Black,

    tertiary = Orange,
    onTertiary = White,
    tertiaryContainer = Orange.copy(alpha = 0.8f),
    onTertiaryContainer = White,

    error = Red,
    onError = White,
    errorContainer = Red.copy(alpha = 0.8f),
    onErrorContainer = White,

    background = Black,
    onBackground = White,
    surface = Color(0xFF121212),
    onSurface = White,

    surfaceVariant = SurfaceVariantDark,
    onSurfaceVariant = OnSurfaceVariantDark,
    outline = OutlineDark
)

private val LightColorScheme = lightColorScheme(
    primary = Green,
    onPrimary = White,
    primaryContainer = Green.copy(alpha = 0.1f),
    onPrimaryContainer = Green,

    secondary = LightGrey,
    onSecondary = Black,
    secondaryContainer = LightGrey.copy(alpha = 0.5f),
    onSecondaryContainer = Black,

    tertiary = Orange,
    onTertiary = White,
    tertiaryContainer = Orange.copy(alpha = 0.1f),
    onTertiaryContainer = Orange,

    error = Red,
    onError = White,
    errorContainer = Red.copy(alpha = 0.1f),
    onErrorContainer = Red,

    background = White,
    onBackground = Black,
    surface = White,
    onSurface = Black,

    surfaceVariant = SurfaceVariant,
    onSurfaceVariant = OnSurfaceVariant,
    outline = Outline
)

@Composable
fun PracticaAndroidTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    // Dynamic color is available on Android 12+
    dynamicColor: Boolean = false, // Dynamic color en falso, si está a true se ponen otros colores
    content: @Composable () -> Unit
) {
    val colorScheme = when {
        dynamicColor && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S -> {
            val context = LocalContext.current
            if (darkTheme) dynamicDarkColorScheme(context) else dynamicLightColorScheme(context)
        }

        darkTheme -> DarkColorScheme
        else -> LightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        shapes = Shapes,
        content = content
    )
}

/**
 * Objeto con valores específicos para la aplicación
 */
object AppTheme {

    // Espaciados
    object Spacing {
        val extraSmall = 4.dp
        val small = 8.dp
        val medium = 16.dp
        val large = 24.dp
        val extraLarge = 32.dp
        val huge = 48.dp
        val extraHuge = 64.dp
    }

    // Elevaciones
    object Elevation {
        val none = 0.dp
        val extraSmall = 1.dp
        val small = 2.dp
        val medium = 4.dp
        val large = 8.dp
        val extraLarge = 16.dp
    }
}