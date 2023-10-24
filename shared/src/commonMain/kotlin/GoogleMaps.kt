import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import kotlinx.serialization.Serializable

@Serializable
data class LatLong(val latitude: Double = 0.0, val longitude: Double = 0.0)

@Serializable
data class MapMarker(  // todo move to proper package
    val key: String = "",
    val position: LatLong = LatLong(0.0, 0.0),
    val title: String = "",
    val alpha: Float = 1.0f,
    val subtitle: String = "",
    val markerInfoPageUrl: String = "",

    // For MarkerInfoScreen (from markerInfoPageUrl)
    val isDescriptionLoaded: Boolean = false,
    val mainPhotoUrl: String = "",
    val markerPhotos: List<String> = listOf(),
    val photoCaptions: List<String> = listOf(),
    val photoAttributions: List<String> = listOf(),
    val inscription: String = "",
    val erected: String = "",
    val credits: String = "",
    val location: String = "",
)

class RecentMapMarker( // todo move to proper package
    val marker: MapMarker,
    val timeAddedToRecentList: Long = 0,
    val seenOrder: Int = 0
) {
    fun mapMarker() = marker
    fun key() = marker.key
}

class CameraLocationBounds( // todo move to proper package
    val coordinates: List<LatLong> = listOf(),
    val padding: Int = 0
)

class CameraPosition( // todo move to proper package
    val target: LatLong = LatLong(0.0, 0.0),
    val zoom: Float = 0f
)

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


//onMarkerClick: (MapMarker) -> Unit = {},
//onMapClick: (LatLong) -> Unit = {},
//onMapLongClick: (LatLong) -> Unit = {},

expect fun triggerDeveloperFeedback()
