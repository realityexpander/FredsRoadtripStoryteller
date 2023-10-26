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
    onCheckedChange: (Boolean) -> Unit
) {
    Row {
        Text(
            title,
            modifier = Modifier
                .weight(3f)
                .align(Alignment.CenterVertically),
        )
        Switch(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            checked = isChecked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = MaterialTheme.colors.onSurface,
                checkedTrackColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
                uncheckedThumbColor = MaterialTheme.colors.onSurface.darkenBy(0.75f),
                uncheckedTrackColor = MaterialTheme.colors.onSurface.copy(alpha = 0.5f),
            )
        )
    }
}

fun Color.darkenBy(factor: Float): Color {
    val color = this
    return Color(
        red = color.red * factor,
        green = color.green * factor,
        blue = color.blue * factor,
        alpha = color.alpha
    )
}
