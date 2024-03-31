package previews

import android.content.res.Configuration
import androidx.compose.foundation.layout.Column
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.BottomSheetState
import androidx.compose.material.BottomSheetValue
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.SnackbarHostState
import androidx.compose.material.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewFontScale
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.tooling.preview.Wallpapers
import data.AppSettings
import data.FakeSettings
import presentation.app.settingsScreen.SettingsScreen
import presentation.uiComponents.AppTheme
import presentation.uiComponents.SettingsSlider
import presentation.uiComponents.SettingsSwitch

@Composable
fun SettingsSwitchPreview() {
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
                SettingsSwitch(
                    title = "This is a disabled option",
                    isChecked = true,
                    enabled = false,
                    onUpdateChecked = {}
                )
                SettingsSlider(
                    title = "Seen Radius",
                    currentValue = 0.5,
                    onUpdateValue = {}
                )
            }
        }
    }
}

@Preview(
    name = "Switches (light)",
    group = "Switches Sliders"
)
@Composable
fun SettingsSwitchPreviewLight() {
    AppTheme {
        SettingsSwitchPreview()
    }
}

@Preview(
    name = "Switches (dark)",
    group = "Switches Sliders",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun SettingsSwitchPreviewsDark() {
    AppTheme(darkTheme = true) {
        SettingsSwitchPreview()
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsPanelPreview() {
    val settings = AppSettings.use(FakeSettings())
    settings.isStartBackgroundTrackingWhenAppLaunchesEnabled = true
    settings.isSpeakWhenUnseenMarkerFoundEnabled = false

    Surface {
        SettingsScreen(
            appSettings = settings,
            bottomSheetScaffoldState = BottomSheetScaffoldState(
                bottomSheetState = BottomSheetState(
                    initialValue = BottomSheetValue.Collapsed,
                    density = LocalDensity.current,
                    confirmValueChange = {
                        false
                    },
                ),
                snackbarHostState = SnackbarHostState()
            ),
            seenRadiusMiles = .5
        )
    }
}


//@Preview(
//    name = "Switches (dark)",
//    group = "Temp",
//    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
//)
//@Composable
//@OptIn(ExperimentalMaterialApi::class)
//fun BottomSheetScaffoldWithDrawerSample() {
//    val scope = rememberCoroutineScope()
//    val scaffoldState = rememberBottomSheetScaffoldState()
//    val drawerState = rememberDrawerState(DrawerValue.Closed)
//
//    val colors = listOf(
//        Color(0xFFffd7d7.toInt()),
//        Color(0xFFffe9d6.toInt()),
//        Color(0xFFfffbd0.toInt()),
//        Color(0xFFe3ffd9.toInt()),
//        Color(0xFFd0fff8.toInt())
//    )
//
//    ModalDrawer(
//        drawerState = drawerState,
//        drawerContent = {
//            Column(
//                Modifier
//                    .fillMaxWidth()
//                    .padding(16.dp),
//                horizontalAlignment = Alignment.CenterHorizontally
//            ) {
//                Text("Drawer content")
//                Spacer(Modifier.height(20.dp))
//                Button(onClick = { scope.launch { drawerState.close() } }) {
//                    Text("Click to close drawer")
//                }
//            }
//        }
//    ) {
//        BottomSheetScaffold(
//            sheetContent = {
//                Box(
//                    Modifier
//                        .fillMaxWidth()
//                        .height(128.dp),
//                    contentAlignment = Alignment.Center
//                ) {
//                    Text("Swipe up to expand sheet")
//                }
//                Column(
//                    Modifier
//                        .fillMaxWidth()
//                        .padding(64.dp),
//                    horizontalAlignment = Alignment.CenterHorizontally
//                ) {
//                    Text("Sheet content")
//                    Spacer(Modifier.height(20.dp))
//                    Button(
//                        onClick = {
//                            scope.launch { scaffoldState.bottomSheetState.collapse() }
//                        }
//                    ) {
//                        Text("Click to collapse sheet")
//                    }
//                }
//            },
//            scaffoldState = scaffoldState,
//            topBar = {
//                TopAppBar(
//                    title = { Text("Bottom sheet scaffold") },
//                    navigationIcon = {
//                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
//                            Icon(Icons.Default.Menu, contentDescription = "Localized description")
//                        }
//                    }
//                )
//            },
//            floatingActionButton = {
//                var clickCount by remember { mutableStateOf(0) }
//                FloatingActionButton(
//                    onClick = {
//                        // show snackbar as a suspend function
//                        scope.launch {
//                            scaffoldState.snackbarHostState
//                                .showSnackbar("Snackbar #${++clickCount}")
//                        }
//                    }
//                ) {
//                    Icon(Icons.Default.Favorite, contentDescription = "Localized description")
//                }
//            },
//            floatingActionButtonPosition = FabPosition.End,
//            sheetPeekHeight = 128.dp
//        ) { innerPadding ->
//            LazyColumn(contentPadding = innerPadding) {
//                items(100) {
//                    Box(
//                        Modifier
//                            .fillMaxWidth()
//                            .height(50.dp)
//                            .background(colors[it % colors.size])
//                    )
//                }
//            }
//        }
//    }
//}


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
fun SettingsPanelPreviewLight() {
    val settings = AppSettings.use(FakeSettings())
    settings.isStartBackgroundTrackingWhenAppLaunchesEnabled = true
    settings.isSpeakWhenUnseenMarkerFoundEnabled = false

    AppTheme {
        SettingsPanelPreview()
    }
}

@Preview(
    name = "Settings (dark)",
    group = "Settings Panel",
    uiMode = Configuration.UI_MODE_NIGHT_YES or Configuration.UI_MODE_TYPE_NORMAL
)
@Composable
fun SettingsPanelPreviewDark() {
    AppTheme {
        SettingsPanelPreview()
    }
}

//////////////////////// FONT SIZES & SCREEN SIZES ///////////////////////////////////////////////////


@PreviewFontScale
@Preview(
    name = "Settings (fonts / dark)",
    group = "Settings Fonts",
    wallpaper = Wallpapers.NONE,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SettingsFontsDark() {
    AppTheme(darkTheme = true) {
        SettingsPanelPreview()
    }
}

@PreviewFontScale
@Preview(
    name = "Settings (fonts / light)",
    group = "Settings Fonts",
    wallpaper = Wallpapers.NONE,
//    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun SettingsFontsLight() {
    AppTheme(darkTheme = false) {
        SettingsPanelPreview()
    }
}

@PreviewScreenSizes
@Preview(
    name = "Settings (screen sizes / dark)",
    group = "Settings Screen Sizes",
    wallpaper = Wallpapers.NONE,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
fun SettingsScreenSizesDark() {
    AppTheme(darkTheme = true) {
        SettingsPanelPreview()
    }
}

@PreviewScreenSizes
@Preview(
    name = "Settings (screen sizes / light)",
    group = "Settings Screen Sizes",
    wallpaper = Wallpapers.NONE,
    uiMode = Configuration.UI_MODE_NIGHT_NO
)
@Composable
fun SettingsScreenSizesLight() {
    AppTheme(darkTheme = false) {
        SettingsPanelPreview()
    }
}


