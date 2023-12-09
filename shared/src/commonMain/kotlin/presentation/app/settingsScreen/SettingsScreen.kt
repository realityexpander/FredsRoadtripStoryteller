package presentation.app.settingsScreen

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.AlertDialog
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.Button
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import appMetadata
import data.AppSettings
import data.MarkersRepo
import data.appSettings
import getPlatformName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import presentation.uiComponents.SettingsSlider
import presentation.uiComponents.SettingsSwitch
import triggerDeveloperFeedback

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsScreen(
    appSettings: AppSettings? = null,
    markersRepo: MarkersRepo? = null,
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    seenRadiusMiles: Double,
    appSettingsIsSpeakWhenUnseenMarkerFoundEnabledState: Boolean = false,
    onSeenRadiusChange: (Double) -> Unit = {},
    onIsCachedMarkersLastUpdatedLocationVisibleChange: (Boolean) -> Unit = {},
    onResetMarkerSettings: () -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val scrollState = rememberScrollState()
    var isResetMarkerSettingsAlertDialogVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var isStartBackgroundTrackingWhenAppLaunchesEnabled by remember {
        mutableStateOf(appSettings?.isStartBackgroundTrackingWhenAppLaunchesEnabled ?: false)
    }
    var isShowMarkerDataLastSearchedLocationEnabled by remember {
        mutableStateOf(appSettings?.isMarkersLastUpdatedLocationVisible ?: false)
    }
    var isSpeakWhenUnseenMarkerFoundEnabled by remember(appSettingsIsSpeakWhenUnseenMarkerFoundEnabledState) {
        mutableStateOf(appSettings?.isSpeakWhenUnseenMarkerFoundEnabled ?: false)
    }
    var isSpeakDetailsWhenUnseenMarkerFoundEnabled by remember {
        mutableStateOf(appSettings?.isSpeakDetailsWhenUnseenMarkerFoundEnabled ?: false)
    }

    // Poll for changes from the App Foreground Notification cancelling the Speak Marker feature
    isSpeakWhenUnseenMarkerFoundEnabled =
        PollForNotificationActionSettingsChanges(
            bottomSheetScaffoldState,
            coroutineScope,
            isSpeakWhenUnseenMarkerFoundEnabled
        )

    Column(
        Modifier.fillMaxWidth()
            .background(MaterialTheme.colors.surface)
            .padding(16.dp)
        ,
        horizontalAlignment = Alignment.Start,
    ) {
        // Title & Close
        Row {
            Text(
                "Settings",
                fontSize = MaterialTheme.typography.h5.fontSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(3f),
                color = MaterialTheme.colors.onSurface
            )
            IconButton(
                modifier = Modifier
                    .offset(16.dp, (-16).dp),
                onClick = {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                        onDismiss()
                    }
                }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = MaterialTheme.colors.onSurface
                )
            }
        }

        // Settings
        Column(
            modifier = Modifier
                .verticalScroll(scrollState),
        ) {

            SettingsSwitch(
                title = "Speak marker when new marker is found",
                isChecked = isSpeakWhenUnseenMarkerFoundEnabled,
                onUpdateChecked = {
                    appSettings?.isSpeakWhenUnseenMarkerFoundEnabled = it
                    isSpeakWhenUnseenMarkerFoundEnabled = it
                }
            )

            SettingsSwitch(
                title = "Speak full marker details when new marker is found",
                isChecked = isSpeakDetailsWhenUnseenMarkerFoundEnabled,
                enabled = isSpeakWhenUnseenMarkerFoundEnabled, // linked to above setting
                onUpdateChecked = {
                    appSettings?.isSpeakDetailsWhenUnseenMarkerFoundEnabled = it
                    isSpeakDetailsWhenUnseenMarkerFoundEnabled = it
                }
            )

            SettingsSwitch(
                title = "Start background tracking when app launches",
                isChecked = isStartBackgroundTrackingWhenAppLaunchesEnabled,
                onUpdateChecked = {
                    appSettings?.isStartBackgroundTrackingWhenAppLaunchesEnabled = it
                    isStartBackgroundTrackingWhenAppLaunchesEnabled = it
                }
            )

            SettingsSlider(
                title = "Seen Radius",
                currentValue = seenRadiusMiles,
                unitsPostfix = " mi",
                onUpdateValue = {
                    appSettings?.seenRadiusMiles = it
                    onSeenRadiusChange(it)
                }
            )

            SettingsSwitch(
                title = "Show marker data last searched location",
                isChecked = isShowMarkerDataLastSearchedLocationEnabled,
                onUpdateChecked = {
                    appSettings?.isMarkersLastUpdatedLocationVisible = it
                    isShowMarkerDataLastSearchedLocationEnabled = it
                    onIsCachedMarkersLastUpdatedLocationVisibleChange(it)
                }
            )

            // Show feedback button on Android only
            // - to turn on dev mode: adb shell setprop debug.firebase.appdistro.devmode true // false to turn off
            if (getPlatformName().contains("Android") && appMetadata.isDebuggable) {
                Spacer(modifier = Modifier.padding(8.dp))
                Button(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally),
                    onClick = {
                        coroutineScope.launch {
                            bottomSheetScaffoldState.bottomSheetState.collapse()
                            // trigger feedback
                            triggerDeveloperFeedback()
                        }
                    }) {
                    Text("Send Feedback to Developer")
                }
            }

            Spacer(modifier = Modifier.padding(8.dp))
            Divider(
                modifier = Modifier.fillMaxWidth(),
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.25f)
            )
            Spacer(modifier = Modifier.padding(8.dp))

            // Reset Marker Info Cache
            Button(
                modifier = Modifier.align(Alignment.CenterHorizontally),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.error,
                    contentColor = MaterialTheme.colors.onError
                ),
                onClick = {
                    coroutineScope.launch {
                        // show confirmation dialog
                        isResetMarkerSettingsAlertDialogVisible = true
                    }
                }) {
                Text("Reset Marker Info Cache")
            }
            Text(
                "Cache size: ${markersRepo?.markers()?.size} markers",
                modifier = Modifier
                    .align(Alignment.CenterHorizontally),
                color = MaterialTheme.colors.onSurface
            )
        }

        // Show Reset Settings Alert Dialog
        if (isResetMarkerSettingsAlertDialogVisible)
            ShowResetMarkerSettingsAlert(
                onDismiss = {
                    coroutineScope.launch {
                        isResetMarkerSettingsAlertDialogVisible = false
                    }
                },
                onSuccess = {
                    coroutineScope.launch {
                        isResetMarkerSettingsAlertDialogVisible = false
                        onResetMarkerSettings()
                    }
                }
            )
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun PollForNotificationActionSettingsChanges(
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    coroutineScope: CoroutineScope,
    shouldSpeakWhenUnseenMarkerFound: Boolean
): Boolean {
    var localShouldSpeakWhenUnseenMarkerFound = shouldSpeakWhenUnseenMarkerFound

    // Poll for changes from the App Foreground Notification disabling the speak marker feature
    DisposableEffect(bottomSheetScaffoldState.bottomSheetState.isExpanded) {
        var isFinished = false
        if (bottomSheetScaffoldState.bottomSheetState.isExpanded) {
            // Poll the appSettings for changes to the Speak Marker feature
            coroutineScope.launch {
                while (!isFinished) {
                    localShouldSpeakWhenUnseenMarkerFound = appSettings.isSpeakWhenUnseenMarkerFoundEnabled
                    delay(1000)
                }
            }
        }
        onDispose {
            isFinished = true
        }
    }
    return localShouldSpeakWhenUnseenMarkerFound
}

@Composable
private fun ShowResetMarkerSettingsAlert(
    onDismiss: () -> Unit = {},
    onSuccess: () -> Unit = {},
) =
    AlertDialog(
        title = {
            Text(
                text = "Reset Marker Info Cache?",
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Text(
                text = "This will clear the cache of marker info, forcing the app to reload the data from the server. Are you sure?",
            )
        },
        confirmButton = {
            Button(
                onClick = {
                    onSuccess()
                },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = MaterialTheme.colors.error,
                    contentColor = MaterialTheme.colors.onError
                ),
            ) {
                Text("Reset Cache")
            }
        },
        dismissButton = {
            Button(
                onClick = {
                    // dismiss dialog
                    onDismiss()
                },
            ) {
                Text("Cancel")
            }
        },
        onDismissRequest = {
            // dismiss dialog
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = true
        )
    )
