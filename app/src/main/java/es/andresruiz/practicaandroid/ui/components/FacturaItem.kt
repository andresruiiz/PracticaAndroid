package es.andresruiz.practicaandroid.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.andresruiz.core.utils.formatDateToDisplay
import es.andresruiz.domain.models.Factura
import es.andresruiz.practicaandroid.R
import es.andresruiz.practicaandroid.ui.theme.AppTheme
import es.andresruiz.practicaandroid.ui.theme.DividerColor

/**
 * Elemento reutilizable para cada factura individual
 */
@Composable
fun FacturaItem(factura: Factura, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable { onClick() }
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(70.dp)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Columna de la izquierda (Fecha y estado)
            Column(modifier = Modifier.weight(1f)) {

                // Falta darle formato a la fecha
                Text(
                    text = formatDateToDisplay(factura.fecha),
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Medium
                )

                // El estado sólo aparece si la factura está pendiente de pago
                if (factura.descEstado != "Pagada") {
                    Text(
                        text = when(factura.descEstado) {
                            "Anulada" -> stringResource(R.string.anulada)
                            "Cuota Fija" -> stringResource(R.string.cuota_fija)
                            "Pendiente de pago" -> stringResource(R.string.pendiente)
                            "Plan de pago" -> stringResource(R.string.plan_pago)
                            else -> ""
                        },
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }

            // Columna de la derecha (Importe y flecha)
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = String.format("%.2f €", factura.importeOrdenacion), // Formateado para 2 decimales
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onBackground,
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    painter = painterResource(R.drawable.ic_arrow_forward),
                    contentDescription = stringResource(R.string.desc_boton_factura),
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .padding(start = AppTheme.Spacing.small)
                        .size(20.dp)
                )
            }
        }

        Divider(
            color = DividerColor,
            thickness = 1.dp,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}