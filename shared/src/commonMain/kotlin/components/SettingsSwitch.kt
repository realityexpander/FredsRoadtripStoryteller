package components

import androidx.compose.foundation.layout.Row
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

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
            onCheckedChange = onCheckedChange
        )
    }
}