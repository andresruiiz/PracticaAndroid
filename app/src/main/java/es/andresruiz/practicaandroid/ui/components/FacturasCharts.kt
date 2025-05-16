package es.andresruiz.practicaandroid.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.SwitchDefaults
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.rememberTextMeasurer
import androidx.compose.ui.unit.sp
import es.andresruiz.practicaandroid.R
import es.andresruiz.practicaandroid.ui.theme.AppTheme
import es.andresruiz.practicaandroid.ui.theme.Celeste
import ir.ehsannarmani.compose_charts.ColumnChart
import ir.ehsannarmani.compose_charts.LineChart
import ir.ehsannarmani.compose_charts.models.Bars
import ir.ehsannarmani.compose_charts.models.DotProperties
import ir.ehsannarmani.compose_charts.models.DrawStyle
import ir.ehsannarmani.compose_charts.models.GridProperties
import ir.ehsannarmani.compose_charts.models.HorizontalIndicatorProperties
import ir.ehsannarmani.compose_charts.models.IndicatorCount
import ir.ehsannarmani.compose_charts.models.IndicatorPosition
import ir.ehsannarmani.compose_charts.models.LabelHelperProperties
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
                text = stringResource(R.string.no_datos),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 32.dp)
            )
        } else {
            if (!isConsumptionMode) {
                PriceLineChartPaged(priceData)
            } else {
                ConsumptionBarChartPaged(consumptionData)
            }

        }
    }
}

@Composable
fun PriceLineChartPaged(priceData: List<Pair<String, Double>>) {
    val chunkSize = 4
    val pageCount = (priceData.size + chunkSize - 1) / chunkSize
    var currentPage by remember { mutableStateOf(0) }

    val currentChunk = priceData.drop(currentPage * chunkSize).take(chunkSize)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        // Gráfico solo con los datos de esta página
        PriceLineChart(chartData = currentChunk)

        // Controles de navegación
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { if (currentPage > 0) currentPage-- },
                enabled = currentPage > 0
            ) {
                Text("<")
            }
            Text("${currentPage + 1} de $pageCount")
            IconButton(
                onClick = { if (currentPage < pageCount - 1) currentPage++ },
                enabled = currentPage < pageCount - 1
            ) {
                Text(">")
            }
        }
    }
}

@Composable
fun ConsumptionBarChartPaged(consumptionData: List<Triple<String, Double, Double>>) {
    val chunkSize = 4
    val pageCount = (consumptionData.size + chunkSize - 1) / chunkSize
    var currentPage by remember { mutableStateOf(0) }

    val currentChunk = consumptionData.drop(currentPage * chunkSize).take(chunkSize)

    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        ConsumptionBarChart(consumptionData = currentChunk)

        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { if (currentPage > 0) currentPage-- },
                enabled = currentPage > 0
            ) {
                Text("<")
            }
            Text("${currentPage + 1} de $pageCount")
            IconButton(
                onClick = { if (currentPage < pageCount - 1) currentPage++ },
                enabled = currentPage < pageCount - 1
            ) {
                Text(">")
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
            .padding(
                start = AppTheme.Spacing.small,
                end = AppTheme.Spacing.small,
                top = AppTheme.Spacing.extraLarge,
                bottom = AppTheme.Spacing.small
            ),
        data = listOf(
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
                    strokeColor = SolidColor(primaryColor)
                )
            )
        ),
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
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center
            ),
            labels = chartData.map { it.first.replaceFirstChar { c -> c.uppercase() } }
        ),
        labelHelperProperties = LabelHelperProperties(enabled = false),
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
    val celesteColor = Celeste

    Column(
        modifier = Modifier.fillMaxWidth()
    ) {

        // Gráfico de columnas con consumo
        ColumnChart(
            modifier = Modifier
                .fillMaxWidth()
                .height(250.dp)
                .padding(
                    start = AppTheme.Spacing.small,
                    end = AppTheme.Spacing.small,
                    top = AppTheme.Spacing.extraLarge,
                    bottom = AppTheme.Spacing.small
                ),
            data = consumptionData.map { data ->
                Bars(
                    label = data.first,
                    values = listOf(
                        Bars.Data(
                            label = stringResource(R.string.punta),
                            value = data.second,
                            color = SolidColor(primaryColor)
                        ),
                        Bars.Data(
                            label = stringResource(R.string.llenas),
                            value = data.third,
                            color = SolidColor(celesteColor)
                        )
                    )
                ) },
            labelProperties = LabelProperties(
                enabled = true,
                textStyle = TextStyle(
                    color = MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium,
                    textAlign = TextAlign.Center
                ),
            ),
            labelHelperProperties = LabelHelperProperties(enabled = false),
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
            animationSpec = spring(
                dampingRatio = Spring.DampingRatioMediumBouncy,
                stiffness = Spring.StiffnessLow
            )
        )

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(primaryColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.punta),
                    style = MaterialTheme.typography.bodySmall,
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(celesteColor)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = stringResource(R.string.llenas),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
