import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import maps.CameraLocationBounds
import maps.CameraPosition
import maps.LatLong
import maps.MapMarker

@Composable
expect fun GoogleMaps(
    modifier: Modifier,
    isMapOptionSwitchesVisible: Boolean = true,
    isTrackingEnabled: Boolean = false,
    userLocation: LatLong? = null,
    markers: List<MapMarker>? = null,
    shouldRedrawMapMarkers: Boolean = false,
    cameraOnetimePosition: CameraPosition? = null,
    cameraLocationLatLong: LatLong? = null,
    cameraLocationBounds: CameraLocationBounds? = null,
    polyLine: List<LatLong>? = null,
    onMapClick: ((LatLong) -> Unit)? = {},
    onMapLongClick: ((LatLong) -> Unit)? = {},
    onMarkerClick: ((MapMarker) -> Unit)? = {},
    talkRadiusMiles: Double = .5,
    cachedMarkersLastUpdatedLocation: Location? = null,
    onToggleIsTrackingEnabledClick: (() -> Unit)? = null,
    onFindMeButtonClick: (() -> Unit)? = null,
    isMarkersLastUpdatedLocationVisible: Boolean = false,
)


//onMarkerClick: (maps.MapMarker) -> Unit = {},
//onMapClick: (maps.LatLong) -> Unit = {},
//onMapLongClick: (maps.LatLong) -> Unit = {},

expect fun triggerDeveloperFeedback()
