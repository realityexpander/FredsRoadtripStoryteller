package maps

import kotlinx.serialization.Serializable

@Serializable
data class MapMarker(  // todo move to proper package
    val key: String = "",
    val position: LatLong = LatLong(0.0, 0.0),
    val title: String = "",
    val alpha: Float = 1.0f,
    val subtitle: String = "",
    val markerInfoPageUrl: String = "",

    // For MarkerInfoScreen (fetched from markerInfoPageUrl)
    val isDescriptionLoaded: Boolean = false,
    val mainPhotoUrl: String = "",
    val markerPhotos: List<String> = listOf(),
    val photoCaptions: List<String> = listOf(),
    val photoAttributions: List<String> = listOf(),
    val inscription: String = "",
    val englishInscription: String = "",
    val spanishInscription: String = "",
    val erected: String = "",
    val credits: String = "",
    val location: String = "",

    val markerPhotos2: List<MarkerPhoto> = listOf(),
)
