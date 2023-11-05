package presentation.maps

import kotlinx.serialization.Serializable

// Define a data class to store latitude and longitude coordinates
@Serializable
data class Location(val latitude: Double, val longitude: Double)

fun Location.isLocationValid(): Boolean {
    return latitude != 0.0 && longitude != 0.0
}

fun LatLong.toLocation(): Location {
    return Location(latitude, longitude)
}
