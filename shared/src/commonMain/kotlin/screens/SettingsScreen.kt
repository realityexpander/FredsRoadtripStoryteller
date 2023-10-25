package screens

import Location
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import data.cachedMarkersResult
import com.russhwolf.settings.Settings
import screens.uiComponents.SettingsSlider
import screens.uiComponents.SettingsSwitch
import getPlatformName
import data.isMarkersLastUpdatedLocationVisible
import kotlinx.coroutines.launch
import data.loadMarkers.MarkersResult
import data.setCachedMarkersLastUpdatedLocation
import data.setCachedMarkersResult
import data.setIsMarkersLastUpdatedLocationVisible
import data.setShouldAutomaticallyStartTrackingWhenAppLaunches
import data.setTalkRadiusMiles
import data.shouldAutomaticallyStartTrackingWhenAppLaunches
import triggerDeveloperFeedback

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SettingsScreen(
    settings: Settings? = null,
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    talkRadiusMiles: Double,
    onTalkRadiusChange: (Double) -> Unit = {},
    onShouldShowMarkerDataLastSearchedLocationChange: ((Boolean) -> Unit) = {},
    onShouldResetMarkerSettingsCache: (() -> Unit) = {}
) {
    val scrollState = rememberScrollState()
    var isResetCacheAlertDialogVisible by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    var shouldStartTrackingAutomaticallyWhenAppLaunches by remember {
        mutableStateOf(settings?.shouldAutomaticallyStartTrackingWhenAppLaunches() ?: false)
    }
    var shouldShowMarkerDataLastSearchedLocation by remember {
        mutableStateOf(settings?.isMarkersLastUpdatedLocationVisible() ?: false)
    }

    Column(
        Modifier.fillMaxWidth()
            .padding(16.dp)
            .scrollable(scrollState, orientation = Orientation.Vertical),
        horizontalAlignment = Alignment.Start,
    ) {
        Row {
            Text(
                "Settings",
                fontSize = MaterialTheme.typography.h5.fontSize,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .weight(3f)
            )
            IconButton(
                modifier = Modifier
                    .offset(16.dp, (-16).dp),
                onClick = {
                    coroutineScope.launch {
                        bottomSheetScaffoldState.bottomSheetState.collapse()
                    }
                }) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close"
                )
            }
        }

        SettingsSwitch(
            title = "Start tracking automatically when app launches",
            isChecked = shouldStartTrackingAutomaticallyWhenAppLaunches,
            onCheckedChange = {
                settings?.setShouldAutomaticallyStartTrackingWhenAppLaunches(it)
                shouldStartTrackingAutomaticallyWhenAppLaunches = it
            }
        )

        SettingsSlider(
            title = "Talk Radius (miles)",
            currentValue = talkRadiusMiles,
            onValueChange = {
                settings?.setTalkRadiusMiles(it)
                onTalkRadiusChange(it)
            }
        )

        SettingsSwitch(
            title = "Show marker data last searched location",
            isChecked = shouldShowMarkerDataLastSearchedLocation,
            onCheckedChange = {
                settings?.setIsMarkersLastUpdatedLocationVisible(it)
                shouldShowMarkerDataLastSearchedLocation = it
                onShouldShowMarkerDataLastSearchedLocationChange(it)
            }
        )

        // Show feedback button on Android only
        // - to turn on dev mode: adb shell setprop debug.firebase.appdistro.devmode true // false to turn off
        if (getPlatformName().contains("Android")) {
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
        Divider(modifier = Modifier.fillMaxWidth())
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
                    isResetCacheAlertDialogVisible = true
                }
            }) {
                Text("Reset Marker Info Cache")
            }
        Text(
            "Cache size: ${settings?.cachedMarkersResult()?.markerIdToMapMarker?.size} markers",
            modifier = Modifier
                .align(Alignment.CenterHorizontally),
        )

        // Show Reset Cache Alert Dialog
        if (isResetCacheAlertDialogVisible)
            ShowResetCacheAlert(
                onClose = {
                    coroutineScope.launch {
                        isResetCacheAlertDialogVisible = false
                    }
                },
                onSuccess = {
                    coroutineScope.launch {
                        isResetCacheAlertDialogVisible = false
                        // reset cache
                        settings?.setCachedMarkersResult(MarkersResult())
                        settings?.setCachedMarkersLastUpdatedLocation(Location(0.0,0.0))
                        onShouldResetMarkerSettingsCache()
                    }
                }
            )
    }
}

@Composable
private fun ShowResetCacheAlert(
    onClose: () -> Unit = {},
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
                    onClose()
                },
            ) {
                Text("Cancel")
            }
        },
        onDismissRequest = {
            // dismiss dialog
            onClose()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = true
        )
    )
