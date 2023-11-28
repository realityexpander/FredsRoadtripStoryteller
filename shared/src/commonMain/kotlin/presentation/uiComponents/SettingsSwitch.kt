package presentation.uiComponents

import androidx.compose.foundation.layout.Row
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color


@Composable
fun SettingsSwitch(
    title: String,
    isChecked: Boolean,
    enabled: Boolean = true,
    onUpdateChecked: (Boolean) -> Unit
) {
    Row {
        Text(
            title,
            modifier = Modifier
                .weight(3f)
                .align(Alignment.CenterVertically),
            color = if(enabled)
                    MaterialTheme.colors.onSurface
                else
                    MaterialTheme.colors.onSurface.copy(alpha = 0.4f)
        )
        Switch(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            checked = isChecked,
            enabled = enabled,
            onCheckedChange = onUpdateChecked,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = //Color.Blue,
                if(MaterialTheme.colors.isLight)
                        Color.Blue
                    else
                        MaterialTheme.colors.primary.lightenBy(0.25f),
                uncheckedThumbColor = if(MaterialTheme.colors.isLight)
                        Color.White
                    else
                        Color.White,
                uncheckedTrackColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
            )
        )
    }
}

// 0.25f -> Lighten by adding 25% of original color (125%)
fun Color.lightenBy(factor: Float): Color {
    val color = this
//    val localFactor = 1f + factor
    return Color(
        red = (color.red + factor).coerceIn(0f, 1f),
        green = (color.green + factor).coerceIn(0f, 1f),
        blue = (color.blue + factor).coerceIn(0f, 1f),
        alpha = color.alpha
    )
}

// 0.25f -> Darken by subtracting 25% of original color (75%)
fun Color.darkenBy(factor: Float): Color {
    val color = this
    val localFactor = 1f - factor
    return Color(
        red = color.red * localFactor,
        green = color.green * localFactor,
        blue = color.blue * localFactor,
        alpha = color.alpha
    )
}
