package maps

import kotlinx.serialization.Serializable

typealias MarkerIdStr = String // e.g. "M2580"

@Serializable
data class Marker(
    val id: MarkerIdStr = "",
    val position: LatLong = LatLong(0.0, 0.0),
    val title: String = "",
    val alpha: Float = 1.0f,
    val subtitle: String = "",

    // For Map/Speaking
    val isSeen: Boolean = false, // Has been within the talkRadius of the user

    // For MarkerInfoScreen (fetched from markerInfoPageUrl)
    val isDetailsLoaded: Boolean = false,
    val markerDetailsPageUrl: String = "",
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

    val markerPhotos2: List<MarkerPhoto> = listOf(), // todo consolidate with markerPhotos, photoCaptions, photoAttributions
    val lastUpdatedDetailsEpochSeconds: Long = 0, // for cache expiry
)
