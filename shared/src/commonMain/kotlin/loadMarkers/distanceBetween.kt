package loadMarkers

import kotlin.math.PI
import kotlin.math.acos
import kotlin.math.cos
import kotlin.math.sin

// from https://dzone.com/articles/distance-calculation-using-3
fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double, shouldUseKM: Boolean = true): Double {
    val theta = lon1 - lon2
    var dist: Double = (sin(deg2rad(lat1))
            * sin(deg2rad(lat2))
            + (cos(deg2rad(lat1))
            * cos(deg2rad(lat2))
            * cos(deg2rad(theta))))
    dist = acos(dist)
    dist = rad2deg(dist)
    dist *= 60 * 1.1515

    if(shouldUseKM)
        dist *= 1.609344

    return dist
}
private fun deg2rad(deg: Double): Double {
    return deg * PI / 180.0
}
private fun rad2deg(rad: Double): Double {
    return rad * 180.0 / PI
}
