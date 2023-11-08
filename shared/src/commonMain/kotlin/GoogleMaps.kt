import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import presentation.maps.CameraLocationBounds
import presentation.maps.CameraPosition
import presentation.maps.LatLong
import presentation.maps.Location
import presentation.maps.Marker

@Composable
expect fun GoogleMaps(
    modifier: Modifier,
    isMapOptionSwitchesVisible: Boolean = true,
    isTrackingEnabled: Boolean = false,
    userLocation: LatLong? = null,
    markers: List<Marker>? = null,
    shouldRedrawMapMarkers: Boolean = false,
    onDidRedrawMapMarkers: () -> Unit = {},
    shouldSetInitialCameraPosition: CameraPosition? = null,
    shouldCenterCameraOnLatLong: LatLong? = null,
    onDidCenterCameraOnLatLong: () -> Unit,
    cameraLocationBounds: CameraLocationBounds? = null,
    polyLine: List<LatLong>? = null,
    onMapClick: ((LatLong) -> Unit)? = {},
    onMapLongClick: ((LatLong) -> Unit)? = {},
    onMarkerInfoClick: ((Marker) -> Unit)? = {},
    seenRadiusMiles: Double = .5,
    cachedMarkersLastUpdatedLocation: Location? = null,
    onToggleIsTrackingEnabledClick: (() -> Unit)? = null,
    onFindMeButtonClick: (() -> Unit)? = null,
    isMarkersLastUpdatedLocationVisible: Boolean = false,
    shouldShowInfoMarker: Marker? = null,
    onDidShowInfoMarker: () -> Unit = {},
)


// LEAVE FOR REFERENCE
//onMarkerClick: (maps.MapMarker) -> Unit = {},
//onMapClick: (presentation.maps.LatLong) -> Unit = {},
//onMapLongClick: (presentation.maps.LatLong) -> Unit = {},

