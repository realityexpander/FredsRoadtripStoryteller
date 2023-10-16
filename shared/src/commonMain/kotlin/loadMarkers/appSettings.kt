package loadMarkers

import Location
import com.russhwolf.settings.Settings
import json
import kotlinx.serialization.encodeToString
import co.touchlab.kermit.Logger as Log


// Settings Keys
const val kCachedMarkersResult = "cachedMarkersResult"
const val kCachedMarkersLastUpdatedEpochSeconds = "cachedMarkersLastUpdatedEpochSeconds"
const val kCachedMarkersLastLocation = "cachedMarkersLastLocation"
const val kLastKnownUserLocation = "lastKnownUserLocation"

fun Settings.printAppSettings() {
    // Show current settings
    Log.d { "keys from settings: $keys" }
    Log.d("Settings: cachedMarkersResult markerInfos.size= " +
            json.decodeFromString<MarkersResult>(getString(kCachedMarkersResult, "{}")).markerInfos.size.toString())
    Log.d("Settings: cachedMarkersLastUpdatedEpochSeconds= " +
            getLong(kCachedMarkersLastUpdatedEpochSeconds, 0L).toString())
    Log.d("Settings: cachedMarkersLastLocation= " +
            getString(kCachedMarkersLastLocation, "{latitude:0.0, longitude:0.0}"))
    Log.d("Settings: LastKnownUserLocation= " +
            getString(kLastKnownUserLocation, "{latitude:0.0, longitude:0.0}"))
}

fun Settings.setCachedMarkersResult(markersResult: MarkersResult) {
    putString(kCachedMarkersResult, json.encodeToString(markersResult))
}
fun Settings.cachedMarkersResult(): MarkersResult {
    return json.decodeFromString(getString(kCachedMarkersResult, "{}"))
}

fun Settings.setCachedMarkersLastUpdatedEpochSeconds(epochSeconds: Long) {
    putLong(kCachedMarkersLastUpdatedEpochSeconds, epochSeconds)
}
fun Settings.cachedMarkersLastUpdatedEpochSeconds(): Long {
    return getLong(kCachedMarkersLastUpdatedEpochSeconds, 0L)
}

fun Settings.setCachedMarkersLastLocation(location: Location) {
    putString(kCachedMarkersLastLocation, json.encodeToString(location))
}
fun Settings.cachedMarkersLastLocation(): Location {
    return json.decodeFromString(getString(kCachedMarkersLastLocation, "{latitude:0.0, longitude:0.0}"))
}

fun Settings.setLastKnownUserLocation(location: Location) {
    putString(kLastKnownUserLocation, json.encodeToString(location))
}
fun Settings.lastKnownUserLocation(): Location {
    return json.decodeFromString(getString(kLastKnownUserLocation, "{latitude:0.0, longitude:0.0}"))
}
