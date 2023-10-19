package composables

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Slider
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun SettingsSlider(
    title: String,
    currentValue: Double,
    onValueChange: (Double) -> Unit
) {
    val currentValueAsString = remember(currentValue) {
        currentValue.toString()
            .substringBefore(".", "")
            .plus(".")
            .plus(currentValue.toString().substringAfter(".", "").take(2))
    }

    Row {
        Text(
            title,
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
        )
        Slider(
            modifier = Modifier
                .weight(2f)
                .align(Alignment.CenterVertically),
            value = currentValue.toFloat(),
            steps = 5,
            valueRange = 0.10f..1f,
            onValueChange = {
                onValueChange(it.toDouble())
            }
        )
        Text(
            text = currentValueAsString,
            modifier = Modifier
                .weight(.5f)
                .align(Alignment.CenterVertically),
        )
    }
}
