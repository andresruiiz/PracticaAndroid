package es.andresruiz.practicaandroid.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import es.andresruiz.practicaandroid.R
import es.andresruiz.practicaandroid.ui.theme.AppShapes
import es.andresruiz.practicaandroid.ui.theme.AppTheme

/**
 * Componente de diálogo informativo reutilizable
 */
@Composable
fun InfoDialog(
    title: String,
    message: String,
    buttonText: String = stringResource(R.string.cerrar),
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = title,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.titleMedium
            )
        },
        text = {
            Text(
                text = message,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )
        },
        containerColor = MaterialTheme.colorScheme.background,
        shape = AppShapes.DialogShape,
        confirmButton = {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                Button(
                    onClick = onDismiss,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = AppTheme.Spacing.extraLarge)
                ) {
                    Text(buttonText)
                }
            }
        }
    )
}