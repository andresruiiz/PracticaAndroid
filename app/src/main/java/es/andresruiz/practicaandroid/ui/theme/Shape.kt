package es.andresruiz.practicaandroid.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Shapes
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp

val Shapes = Shapes(
    small = RoundedCornerShape(4.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(20.dp)
)

object AppShapes {
    val CardShape = RoundedCornerShape(16.dp)

    val DateFieldShape = RoundedCornerShape(12.dp)

    val DialogShape = RectangleShape
}