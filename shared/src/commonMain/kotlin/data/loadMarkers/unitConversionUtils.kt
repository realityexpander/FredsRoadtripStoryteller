package data.loadMarkers

import kotlin.math.PI

fun Double.metersToMiles(): Double {
    return this * 0.000621371
}
fun Double.milesToMeters(): Double {
    return this * 1609.34
}
fun Double.milesToKilometers(): Double {
    return this * 1.60934
}

fun Double.kilometersToMiles(): Double {
    return this * 0.621371
}

fun Double.metersToDegrees(latitude: Double): Double {
    val earthRadius = 3960.0 // in miles
    val radiansToDegrees = 180.0 / PI
    return (this / earthRadius) * radiansToDegrees
}
