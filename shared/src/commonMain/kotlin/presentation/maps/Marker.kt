package presentation.maps

import kotlinx.serialization.Serializable

typealias MarkerIdStr = String // e.g. "M2580", always starts with "M"

@Serializable
data class Marker(
    // Basic info (from marker index html page)
    val id: MarkerIdStr = "",
    val position: LatLong = LatLong(0.0, 0.0),
    val title: String = "",
    val subtitle: String = "",
    val alpha: Float = 1.0f,

    // For Map/Speaking
    val isSeen: Boolean = false, // Has been within the talkRadius of the user
    val isSpoken: Boolean = false, // Has been spoken by the user or automatically by the app

    // Marker Details (from from marker details html page
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

    // LEAVE FOR FUTURE REFACTOR
    val markerPhotos2: List<MarkerPhoto> = listOf(), // todo consolidate with markerPhotos, photoCaptions, photoAttributions
    val lastUpdatedDetailsEpochSeconds: Long = 0, // for cache expiry
)
