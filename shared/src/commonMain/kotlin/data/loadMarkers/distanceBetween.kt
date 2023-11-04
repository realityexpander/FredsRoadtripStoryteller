package data.loadMarkers

import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

fun distanceBetweenInMiles(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double =
    distanceBetween(lat1, lon1, lat2, lon2, shouldUseKm = false)

fun distanceBetweenInKm(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double =
    distanceBetween(lat1, lon1, lat2, lon2, shouldUseKm = true)

// Returns distance in miles (or kilometers if shouldUseKM is true)
// from https://dzone.com/articles/distance-calculation-using-3
fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double, shouldUseKm: Boolean = true): Double {
    val theta = lon1 - lon2
    var dist: Double = (sin(deg2rad(lat1))
            * sin(deg2rad(lat2))
            + (cos(deg2rad(lat1))
            * cos(deg2rad(lat2))
            * cos(deg2rad(theta))))
    dist = acos(dist)
    dist = rad2deg(dist)
    dist *= 60 * 1.1515

    if(shouldUseKm)
        dist *= 1.609344

    return dist
}
private fun deg2rad(deg: Double): Double {
    return deg * PI / 180.0
}
private fun rad2deg(rad: Double): Double {
    return rad * 180.0 / PI
}

// Todo - test these work
public fun Double.milesToDegrees(): Double {
    val miles = this
    val earthRadius = 3960.0 // in miles
    val radiansToDegrees = 180.0 / PI
    return (miles / earthRadius) * radiansToDegrees
}
public fun distanceAtLongitude(startLongitude: Double, miles: Double): Double {
    return distanceBetween(
        0.0,
        startLongitude,
        0.0,
        startLongitude + miles.milesToDegrees()
    )
}

public fun distanceAtLatitude(startLatitude: Double, miles: Double): Double {
    return distanceBetween(
        startLatitude,
        0.0,
        startLatitude + miles.milesToDegrees(),
        0.0
    )
}
