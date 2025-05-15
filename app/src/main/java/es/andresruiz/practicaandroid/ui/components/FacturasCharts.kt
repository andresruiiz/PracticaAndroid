package es.andresruiz.practicaandroid.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.material3.SwitchDefaults
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import es.andresruiz.practicaandroid.ui.theme.AppTheme
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.BarProperties
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorCount
import ir.ehsannarmani.compose_charts.models.IndicatorPosition
import ir.ehsannarmani.compose_charts.models.LabelProperties
import ir.ehsannarmani.compose_charts.models.Line
import ir.ehsannarmani.compose_charts.models.ZeroLineProperties

/**
 * Componente que muestra una gráfica de las facturas
 */
@Composable
fun FacturasChart(
    isConsumptionMode: Boolean,
    onModeToggle: () -> Unit,
    priceData: List<Pair<String, Double>>,
    consumptionData: List<Triple<String, Double, Double>>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        // Switch para cambiar tipo de gráfica
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Text(
                text = "€",
                style = MaterialTheme.typography.bodyMedium,
                color = if (!isConsumptionMode)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondary
            )

            Switch(
                modifier = Modifier.padding(horizontal = AppTheme.Spacing.small),
                checked = isConsumptionMode,
                onCheckedChange = { onModeToggle() },
                colors = SwitchDefaults.colors(
                    checkedThumbColor = MaterialTheme.colorScheme.surface,
                    checkedTrackColor = MaterialTheme.colorScheme.outline,
                    uncheckedThumbColor = MaterialTheme.colorScheme.surface,
                    uncheckedTrackColor = MaterialTheme.colorScheme.primary
                )
            )

            Text(
                text = "kWh",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isConsumptionMode)
                    MaterialTheme.colorScheme.primary
                else
                    MaterialTheme.colorScheme.secondary
            )
        }

        // Si no hay datos, mostramos un mensaje
        if ((!isConsumptionMode && priceData.isEmpty()) ||
            (isConsumptionMode && consumptionData.isEmpty())) {
            Text(
                text = "no hay datos",
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            )
        } else {
            if (!isConsumptionMode) {
                PriceLineChart(priceData)
            } else {
                ConsumptionBarChart(consumptionData)
            }
        }
    }
}

@Composable
fun PriceLineChart(chartData: List<Pair<String, Double>>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val textMeasurer = rememberTextMeasurer()

    LineChart(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(horizontal = AppTheme.Spacing.small, vertical = AppTheme.Spacing.small),
        data = remember {
            listOf(
                Line(
                    label = "Precio",
                    values = chartData.map { it.second },
                    color = SolidColor(primaryColor),
                    firstGradientFillColor = primaryColor.copy(alpha = 0.3f),
                    secondGradientFillColor = primaryColor.copy(alpha = 0.3f),
                    drawStyle = DrawStyle.Stroke(width = 2.dp),
                    curvedEdges = true,
                    dotProperties = DotProperties(
                        enabled = true,
                        color = SolidColor(primaryColor),
                        strokeWidth = 2.dp,
                        radius = 5.dp,
                        strokeColor = SolidColor(primaryColor),
                    )
                )
            )
        },
        //animationMode = AnimationMode.Together(delayBuilder = { it * 300L }),
        // Configuración de línea cero
        zeroLineProperties = ZeroLineProperties(
            enabled = false
        ),
        // Configuración de la cuadrícula
        gridProperties = GridProperties(
            enabled = true,
            yAxisProperties = GridProperties.AxisProperties(
                enabled = false,
            ),
            xAxisProperties = GridProperties.AxisProperties(
                enabled = true,
                lineCount = 3
            )
        ),
        // Configuración de etiquetas
        labelProperties = LabelProperties(
            enabled = true,
            textStyle = TextStyle(
                color = MaterialTheme.colorScheme.onSurface,
                fontSize = 10.sp,
                fontWeight = FontWeight.Medium
            ),
            labels = chartData.map { it.first.replaceFirstChar { c -> c.uppercase() } }
        ),
        // Configuración del indicador horizontal
        indicatorProperties = HorizontalIndicatorProperties(
            enabled = true,
            textStyle = TextStyle(
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface
            ),
            padding = 16.dp,
            position = IndicatorPosition.Horizontal.End,
            count = IndicatorCount.CountBased(
                count = 3
            ),
            contentBuilder = {
                "%.0f €".format(it)
            }
        ),
        textMeasurer = textMeasurer)
}

@Composable
fun ConsumptionBarChart(consumptionData: List<Triple<String, Double, Double>>) {
    val primaryColor = MaterialTheme.colorScheme.primary
    val celesteColor = Color(0xFFA9CAF9) // Azul celeste para "llenas" (a mano por ahora)

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        // Gráfico de columnas con consumo
        ColumnChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(horizontal = 8.dp),
            data = remember {
                consumptionData.map { data ->
                    val total = data.second + data.third
                    val puntaPorc = if (total > 0) data.second / total else 0.0

                    Bars(
                        label = data.first,
                        values = listOf(
                            Bars.Data(
                                label = "Total",
                                value = total,
                                color = Brush.verticalGradient(
                                    0f to celesteColor,
                                    puntaPorc.toFloat() to celesteColor,
                                    puntaPorc.toFloat() to primaryColor,
                                    1f to primaryColor
                                )
                            )
                        )
                    )
                }
            },
            // Configuración de la cuadrícula
            gridProperties = GridProperties(
                enabled = true,
                yAxisProperties = GridProperties.AxisProperties(
                    enabled = false,
                ),
                xAxisProperties = GridProperties.AxisProperties(
                    enabled = true,
                    lineCount = 3
                )
            ),
            // Configuración del indicador horizontal
            indicatorProperties = HorizontalIndicatorProperties(
                enabled = true,
                textStyle = TextStyle(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface
                ),
                padding = 16.dp,
                position = IndicatorPosition.Horizontal.End,
                count = IndicatorCount.CountBased(
                    count = 3
                ),
                contentBuilder = {
                    "%.0f kWh".format(it)
                }
            ),
            barProperties = BarProperties(
                spacing = 4.dp
            ),
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )
    }
}
