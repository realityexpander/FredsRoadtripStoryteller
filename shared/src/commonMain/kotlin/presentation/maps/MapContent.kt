package presentation.maps

import GoogleMaps
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
fun MapContent(
    modifier: Modifier = Modifier,
    isFinishedLoadingMarkerData: Boolean = false,  // only sets the initial position, not tracked. Use `userLocation` for tracking.
    initialUserLocation: Location,
    userLocation: Location,
    markers: List<Marker>,
    mapBounds: List<LatLong>? = null,
    shouldRedrawMapMarkers: Boolean,
    onDidRedrawMapMarkers: () -> Unit = {},
    isTrackingEnabled: Boolean = false,
    centerOnUserCameraLocation: Location? = null,
    seenRadiusMiles: Double = .5,
    cachedMarkersLastUpdatedLocation: Location? = null,
    onToggleIsTrackingEnabled: (() -> Unit)? = null,
    onFindMeButtonClicked: (() -> Unit)? = null,
    isMarkersLastUpdatedLocationVisible: Boolean = false,
    isMapOptionSwitchesVisible: Boolean = true,
    onMarkerClick: ((Marker) -> Unit)? = null,
    shouldShowInfoMarker: Marker? = null,
    onDidShowInfoMarker: () -> Unit = {}
): Boolean {
    var didMapMarkersRedraw by remember(shouldRedrawMapMarkers) { mutableStateOf(true) }
    var isFirstUpdate by remember { mutableStateOf(true) } // force map to update at least once

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (isFinishedLoadingMarkerData || !isFirstUpdate || didMapMarkersRedraw) {

            GoogleMaps(
                modifier = modifier,
                isMapOptionSwitchesVisible = isMapOptionSwitchesVisible,
                isTrackingEnabled = isTrackingEnabled,
                userLocation = LatLong( // passed to map to track location
                    userLocation.latitude,
                    userLocation.longitude
                ),
                markers = markers.ifEmpty { null },
                shouldRedrawMapMarkers = shouldRedrawMapMarkers,
                cameraOnetimePosition =
                    if (isFirstUpdate) {  // set initial camera position
                        CameraPosition(
                            target = LatLong(
                                initialUserLocation.latitude,
                                initialUserLocation.longitude
                            ),
                            zoom = 12f  // note: forced zoom level
                        )
                    } else
                        null,
                cameraLocationLatLong = remember(centerOnUserCameraLocation) {
                    // 37.422160,
                    // -122.084270  // googleplex
                    centerOnUserCameraLocation?.let {
                        LatLong(
                            centerOnUserCameraLocation.latitude,
                            centerOnUserCameraLocation.longitude
                        )
                    } ?: run {
                        null
                    }
                },
                cameraLocationBounds = remember {  // Center around bound of markers
                    mapBounds?.let {
                        CameraLocationBounds(
                            coordinates = mapBounds,
                            padding = 80  // in pixels
                        )
                    } ?: run {
                        null // won't center around bounds
                    }
                },
                onMarkerClick = onMarkerClick,
                seenRadiusMiles = seenRadiusMiles,
                cachedMarkersLastUpdatedLocation = cachedMarkersLastUpdatedLocation,
                onToggleIsTrackingEnabledClick = onToggleIsTrackingEnabled,
                onFindMeButtonClick = onFindMeButtonClicked,
                isMarkersLastUpdatedLocationVisible = isMarkersLastUpdatedLocationVisible,
                shouldShowInfoMarker = shouldShowInfoMarker,
                onDidShowInfoMarker = onDidShowInfoMarker
            )

            // Guard against initial location being (0.0, 0.0)
            if(initialUserLocation.isLocationValid()) isFirstUpdate = false
            if(initialUserLocation.isLocationValid()) didMapMarkersRedraw = false
            if(initialUserLocation.isLocationValid()) onDidRedrawMapMarkers()
        }
    }

    return didMapMarkersRedraw
}
