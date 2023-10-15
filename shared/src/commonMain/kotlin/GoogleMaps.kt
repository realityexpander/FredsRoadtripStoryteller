

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
    onMarkerClick: ((MapMarker) -> Unit)? = {},
    onMapClick: ((LatLong) -> Unit)? = {},
    onMapLongClick: ((LatLong) -> Unit)? = {},
    markers: List<MapMarker>? = null,
    shouldUpdateMapMarkers: Boolean = false,
    cameraLocationLatLong: LatLong? = null,
    cameraLocationBounds: CameraLocationBounds? = null,
    initialCameraPosition: CameraPosition? = null,
    polyLine: List<LatLong>? = null,
    userLocation: LatLong? = null,
)


//onMarkerClick: (MapMarker) -> Unit = {},
//onMapClick: (LatLong) -> Unit = {},
//onMapLongClick: (LatLong) -> Unit = {},
