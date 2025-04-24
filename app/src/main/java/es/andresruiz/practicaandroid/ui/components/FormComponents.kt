package es.andresruiz.practicaandroid.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.andresruiz.practicaandroid.R

/**
 * Componente para mostrar campos de texto de solo lectura
 */
@Composable
fun ReadOnlyTextField(
    label: String,
    text: String,
    onValueChange: (String) -> Unit = {},
    infoIcon: Boolean = false,
    onInfoClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BasicTextField(
        enabled = false,
        value = text,
        onValueChange = onValueChange,
        modifier = modifier
            .fillMaxWidth()
            .background(
                color = Color.Transparent,
                shape = MaterialTheme.shapes.small
            )
            .padding(bottom = 32.dp),
        textStyle = LocalTextStyle.current.copy(
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Start
        ),
        decorationBox = { innerTextField ->
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp)
                ) {
                    innerTextField()
                    if (infoIcon) {
                        Box(
                            modifier = Modifier
                                .align(Alignment.CenterEnd)
                        ) {
                            IconButton(
                                onClick = { onInfoClick() },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    painter = painterResource(R.drawable.ic_info),
                                    contentDescription = "Información",
                                    tint = Color(0xFF549BFF)
                                )
                            }
                        }
                    }
                }
                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.Gray
                )
            }
        }
    )
}

@Composable
fun CheckboxItem(
    text: String,
    isChecked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .height(40.dp)
    ) {
        Checkbox(
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = CheckboxDefaults.colors(
                checkedColor = Color(0xFF8BC34A),
                uncheckedColor = Color(0xFFE0E0E0)
            )
        )
        Text(
            text = text
        )
    }
}