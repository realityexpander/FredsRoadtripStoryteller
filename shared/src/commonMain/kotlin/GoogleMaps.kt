

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

data class LatLong(val latitude: Double = 0.0, val longitude: Double = 0.0)

class MapMarker(
    val key: String = "",
    val position: LatLong = LatLong(0.0, 0.0),
    val title: String = "",
    val alpha: Float = 1.0f,
    val subtitle: String = ""
)
class CameraLocationBounds(
    val coordinates: List<LatLong> = listOf(),
    val padding: Int = 0
)

class CameraPosition(
    val target: LatLong = LatLong(0.0, 0.0),
    val zoom: Float = 0f
)

@Composable
expect fun GoogleMaps(
    modifier: Modifier,
    isControlsVisible: Boolean = true,
    isTrackingEnabled: Boolean = false,
    userLocation: LatLong? = null,
    markers: List<MapMarker>? = null,
    shouldUpdateMapMarkers: Boolean = false,
    cameraOnetimePosition: CameraPosition? = null,
    cameraLocationLatLong: LatLong? = null,
    cameraLocationBounds: CameraLocationBounds? = null,
    polyLine: List<LatLong>? = null,
    onMapClick: ((LatLong) -> Unit)? = {},
    onMapLongClick: ((LatLong) -> Unit)? = {},
    onMarkerClick: ((MapMarker) -> Unit)? = {},
)


//onMarkerClick: (MapMarker) -> Unit = {},
//onMapClick: (LatLong) -> Unit = {},
//onMapLongClick: (LatLong) -> Unit = {},