package presentation.uiComponents

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.blur
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp

@Composable
fun SwitchWithLabel(
    modifier: Modifier = Modifier,
    label: String,
    state: Boolean,
    darkOnLightTextColor: Boolean = true,
    onStateChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .clickable(
                interactionSource = interactionSource,
                // This is for removing ripple when Row is clicked
                indication = null,
                role = Role.Switch,
                onClick = {
                    onStateChange(!state)
                }
            )
            .padding(2.dp),
        verticalAlignment = Alignment.CenterVertically

    ) {
        Switch(
            checked = state,
            onCheckedChange = {
                onStateChange(it)
            }
        )
        Spacer(modifier = Modifier.padding(start = 8.dp))
        Box {
            Text(
                modifier = modifier
                    .alpha(alpha = 1.0f)
                    .offset(x = 1.dp, y = 1.dp)
                .blur(radius = 1.dp)
                ,
                color = if(darkOnLightTextColor) Color.Black else Color.White, // colorResource(id = R.color.black),
                text = label
            )
            Text(
                modifier = modifier,
                color = if(darkOnLightTextColor) Color.White else Color.Black, //colorResource(id = R.color.black),
                text = label
            )
        }
    }
}
