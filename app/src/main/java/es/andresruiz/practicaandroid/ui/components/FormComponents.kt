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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import es.andresruiz.practicaandroid.R
import es.andresruiz.practicaandroid.ui.theme.AppTheme
import es.andresruiz.practicaandroid.ui.theme.StatusInfo
import es.andresruiz.practicaandroid.ui.theme.TextSecondary

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
                color = MaterialTheme.colorScheme.surface,
                shape = MaterialTheme.shapes.small
            )
            .padding(bottom = AppTheme.Spacing.large),
        textStyle = LocalTextStyle.current.copy(
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Start
        ),
        decorationBox = { innerTextField ->
            Column {
                Text(
                    text = label,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = AppTheme.Spacing.extraSmall)
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
                                    contentDescription = stringResource(R.string.desc_boton_info),
                                    tint = StatusInfo
                                )
                            }
                        }
                    }
                }
                Divider(
                    modifier = Modifier.fillMaxWidth(),
                    color = MaterialTheme.colorScheme.outline
                )
            }
        }
    )
}

/**
 * Componente para mostrar un elemento de checkbox
 */
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
                checkedColor = MaterialTheme.colorScheme.primary,
                uncheckedColor = MaterialTheme.colorScheme.outline
            )
        )
        Text(
            text = text
        )
    }
}