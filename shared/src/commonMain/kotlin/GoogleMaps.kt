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
    cameraOnetimePosition: CameraPosition? = null,
    cameraLocationLatLong: LatLong? = null,
    cameraLocationBounds: CameraLocationBounds? = null,
    polyLine: List<LatLong>? = null,
    onMapClick: ((LatLong) -> Unit)? = {},
    onMapLongClick: ((LatLong) -> Unit)? = {},
    onMarkerClick: ((Marker) -> Unit)? = {},
    seenRadiusMiles: Double = .5,
    cachedMarkersLastUpdatedLocation: Location? = null,
    onToggleIsTrackingEnabledClick: (() -> Unit)? = null,
    onFindMeButtonClick: (() -> Unit)? = null,
    isMarkersLastUpdatedLocationVisible: Boolean = false,
)


// LEAVE FOR REFERENCE
//onMarkerClick: (maps.MapMarker) -> Unit = {},
//onMapClick: (presentation.maps.LatLong) -> Unit = {},
//onMapLongClick: (presentation.maps.LatLong) -> Unit = {},

