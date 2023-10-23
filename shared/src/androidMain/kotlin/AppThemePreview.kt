import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.DrawerState
import androidx.compose.material.DrawerValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.Wallpapers
import components.SettingsSlider
import components.SettingsSwitch
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import screens.SettingsScreen

@Preview(name = "switches", group = "Settings")
@Composable
fun SettingsSwitchPreviews() {
    AppTheme {
        Surface {
            Column {
                SettingsSwitch(
                    title = "Show marker data last searched location",
                    isChecked = false,
                    onCheckedChange = {}
                )
                SettingsSwitch(
                    title = "Show marker data last searched location",
                    isChecked = true,
                    onCheckedChange = {}
                )
                SettingsSlider(
                    title = "Talk Radius",
                    currentValue = 1.0,
                    onValueChange = {}
                )
            }
        }
    }
}
@Preview(name = "switches", group = "Settings",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun SettingsSwitchPreviewsDark() {
    SettingsSwitchPreviews()
}


@OptIn(ExperimentalMaterialApi::class)
@Preview(
    showBackground = false, showSystemUi = false, backgroundColor = 0xFF8F8F8F,
    uiMode = Configuration.UI_MODE_NIGHT_NO or Configuration.UI_MODE_TYPE_NORMAL, group = "Settings",
    name = "Settings"
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
                            true
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
                talkRadiusMiles = 1.0,
            )
        }
    }
}
@Preview(name = "SettingsDark", group = "Settings", wallpaper = Wallpapers.NONE,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun SettingsDark() {
    SettingsPreview()
}

@OptIn(ExperimentalMaterialApi::class)
@Preview(
    backgroundColor = 0xFF8F8F8F,
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showSystemUi = false, showBackground = true, name = "Marker Info", group = "Marker Info"
)
@Composable
fun MarkerInfoPreview() {
    AppTheme {
        Surface {
            MarkerInfoScreen(
                bottomSheetScaffoldState = BottomSheetScaffoldState(
                    bottomSheetState = BottomSheetState(
                        initialValue = BottomSheetValue.Collapsed,
                        density = LocalDensity.current,
                        confirmValueChange = {
                            true
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
                marker = MapMarker(
                    key = "test key",
                    position = LatLong(
                        latitude = 0.0,
                        longitude = 0.0
                    ),
                    title = "Test Title",
                    subtitle = "Test Subtitle",
                    alpha = 1f
                )
            )
        }
    }
}
