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
import screens.uiComponents.AppTheme
import screens.uiComponents.SettingsSlider
import screens.uiComponents.SettingsSwitch
import data.LoadingState
import maps.LatLong
import maps.MapMarker
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
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showSystemUi = false, showBackground = true, name = "Marker Info", group = "Marker Info"
)
@Composable
fun MarkerInfoPreview(
 loadingState: LoadingState<MapMarker> = LoadingState.Loaded(
    MapMarker(
        id = "test key",
        position = LatLong(
            latitude = 0.0,
            longitude = 0.0
        ),
//                    title = "First City Council of Tepoztlan",
        title = "El Tepozteco National Park",
        alpha = 1f,
        subtitle = "Test Subtitle",
        location = "Location Description",
        inscription = "Incised inscription",
        englishInscription = "",
        spanishInscription = ""
    ))
){
    AppTheme {
        Surface {
            MarkerDetailsScreen(
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
                marker = loadingState
            )
        }
    }
}
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showSystemUi = false, showBackground = true, name = "Marker Info", group = "Marker Info"
)
@Composable
fun MarkerInfoPreviewError() {
    MarkerInfoPreview(
        loadingState = LoadingState.Error(
            errorMessage = "Error loading marker"
        )
    )
}
@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showSystemUi = false, showBackground = true, name = "Marker Info", group = "Marker Info"
)
@Composable
fun MarkerInfoPreviewLoading() {
    MarkerInfoPreview(
        loadingState = LoadingState.Loading
    )
}

@Preview(
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL,
    showSystemUi = false,
    showBackground = true,
    name = "Splash Screen",
    group = "Splash Screen"
)
@Composable
fun SplashScreenPreview() {
    AppTheme {
        Surface {
            SplashScreenForPermissions()
        }
    }
}


