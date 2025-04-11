package es.andresruiz.practicaandroid.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.MediumTopAppBar
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.TopAppBarScrollBehavior
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import es.andresruiz.practicaandroid.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    navController: NavController,
    scrollBehavior: TopAppBarScrollBehavior,
    title: String,
    backText: String? = null,
    actionsIcon: Painter? = null,
    actionsOnClick: () -> Unit = {}
) {
    MediumTopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background,
            titleContentColor = MaterialTheme.colorScheme.onBackground,
        ),
        title = {
            Text(
                text = title,
                fontWeight = FontWeight.Bold,
                fontSize = 40.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        },
        navigationIcon = {
            if (!backText.isNullOrEmpty()) {
                TextButton(onClick = { navController.popBackStack() }) {

                    val icon: Painter = painterResource(id = R.drawable.ic_arrow_back)

                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            painter = icon,
                            contentDescription = "Botón para volver",
                            modifier = Modifier
                                .padding(end = 8.dp)
                                .size(16.dp)
                        )
                        Text(
                            text = backText,
                            fontSize = 18.sp
                        )
                    }
                }
            }
        },
        actions = {
            if (actionsIcon != null) {
                IconButton(onClick = actionsOnClick) {

                    val icon: Painter = actionsIcon

                    Icon(
                        painter = icon,
                        contentDescription = "Botón de filtros",
                        modifier = Modifier
                            .size(30.dp)
                    )
                }
            }
        },
        scrollBehavior = scrollBehavior
    )
}