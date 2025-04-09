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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import es.andresruiz.domain.models.Factura
import es.andresruiz.practicaandroid.R

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
                .height(60.dp)
                .padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Columna de la derecha (Fecha y estado)
            Column(modifier = Modifier.weight(1f)) {

                // Falta darle formato a la fecha
                Text(
                    text = factura.fecha,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Medium
                )

                // El estado sólo aparece si la factura está pendiente de pago
                if (factura.descEstado == "Pendiente de pago") {
                    Text(
                        text = factura.descEstado,
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
                    fontWeight = FontWeight.Bold
                )

                Icon(
                    painter = painterResource(R.drawable.ic_arrow_forward),
                    contentDescription = "Botón para abrir detalles de la factura",
                    tint = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .padding(start = 8.dp)
                        .size(20.dp)
                )
            }
        }

        Divider(
            color = Color.LightGray,
            thickness = 1.dp,
            modifier = Modifier.padding(start = 12.dp)
        )
    }
}