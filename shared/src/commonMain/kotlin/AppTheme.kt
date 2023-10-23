import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Text
import androidx.compose.material.Typography
import androidx.compose.material.darkColors
import androidx.compose.material.lightColors
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.FlowPreview

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
//        darkColorScheme(
        darkColors(
            primary = Color(0xFF6200EE),
            onPrimary = Color(0xFFEEEEEE),
            secondary = Color(0xFF03DAC5),
//            tertiary = Color(0xFF3700B3)
            surface = Color(0xFF222222),
            onSurface = Color(0xFFEEEEEE),
        )
    } else {
//        lightColorScheme(
        lightColors(
            primary = Color(0xFF6200EE),
            secondary = Color(0xFF03DAC5),
//            tertiary = Color(0xFF3700B3)
            background = Color(0xFF0000FF),
//            onSurface = Color(0xFF111111),
            surface = Color(0xFFEEEEEE),
            onSurface = Color(0xFF000000),
        )
    }
    val typography = Typography(
//        bodyMedium = TextStyle(
        body2 = TextStyle(
            fontFamily = FontFamily.Default,
            fontWeight = FontWeight.Normal,
            fontSize = 16.sp
        )
    )
    val shapes = Shapes(
        small = RoundedCornerShape(4.dp),
        medium = RoundedCornerShape(4.dp),
        large = RoundedCornerShape(0.dp)
    )

    MaterialTheme(
//        colorScheme = colors,
        colors = colors,
        typography = typography,
        shapes = shapes,
        content = content
    )
}
