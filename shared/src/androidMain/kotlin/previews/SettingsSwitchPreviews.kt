package previews

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import presentation.SettingsScreen
import presentation.app.AppTheme
import presentation.uiComponents.SettingsSlider
import presentation.uiComponents.SettingsSwitch

@Preview(
    name = "switches",
    group = "Switches Sliders"
)
@Composable
fun SettingsSwitchPreviews() {
    AppTheme {
        Surface {
            Column {
                SettingsSwitch(
                    title = "Show marker data last searched location",
                    isChecked = false,
                    onUpdateChecked = {}
                )
                SettingsSwitch(
                    title = "Show marker data last searched location",
                    isChecked = true,
                    onUpdateChecked = {}
                )
                SettingsSlider(
                    title = "Talk Radius",
                    currentValue = 0.5,
                    onUpdateValue = {}
                )
            }
        }
    }
}
@Preview(
    name = "switches (night)",
    group = "Switches Sliders",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun SettingsSwitchPreviewsDark() {
    SettingsSwitchPreviews()
}
@OptIn(ExperimentalMaterialApi::class)
@Preview(
    name = "Settings (light)",
    group = "Settings Panel",
    showBackground = false,
    showSystemUi = false,
    backgroundColor = 0xFF8F8F8F,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL,
)
@Composable
fun SettingsPreview() {
    AppTheme {
        Surface {
            SettingsScreen(
                settings = null,
                bottomSheetScaffoldState = BottomSheetScaffoldState(
                    bottomSheetState = BottomSheetState(
                        initialValue = BottomSheetValue.Collapsed,
                        density = LocalDensity.current,
                        confirmValueChange = {
                            false
                        },
                    ),
                    drawerState = DrawerState(
                        initialValue = DrawerValue.Closed,
                        confirmStateChange = {
                            true
                        }
                    ),
                    snackbarHostState = SnackbarHostState()
                ),
                seenRadiusMiles = .5,
            )
        }
    }
}
@Preview(
    name = "Settings (dark)",
    group = "Settings Panel",
    wallpaper = Wallpapers.NONE,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun SettingsDark() {
    SettingsPreview()
}
