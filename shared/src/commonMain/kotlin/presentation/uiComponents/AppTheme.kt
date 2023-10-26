package presentation.uiComponents

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
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

@Composable
fun AppTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) {
//        darkColorScheme(
        darkColors(
            primary = Color(0xFF111162),
            onPrimary = Color(0xFFBBBBBB),
            secondary = Color(0xFF03DAC5),
//            tertiary = Color(0xFF3700B3)
            surface = Color(0xFF222222),
            onSurface = Color(0xFFBBBBBB),
            background = Color(0xFF333344),
            onBackground = Color(0xFFDDDDDD),
        )
    } else {
//        lightColorScheme(
        lightColors(
            primary = Color(0xFF111162),
            secondary = Color(0xFF03DAC5),
//            tertiary = Color(0xFF3700B3)
//            onSurface = Color(0xFF111111),
            surface = Color(0xFFEEEEEE),
            onSurface = Color(0xFF000000),
            background = Color(0xFF444444),
            onBackground = Color(0xFFDDDDDD),
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
        medium = RoundedCornerShape(8.dp),
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
