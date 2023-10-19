import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import loadMarkers.MarkersResult
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

// Cached Markers
fun Settings.setCachedMarkersResult(markersResult: MarkersResult) {
    putString(kCachedMarkersResult, json.encodeToString(markersResult))
}
fun Settings.cachedMarkersResult(): MarkersResult {
    return json.decodeFromString(getString(kCachedMarkersResult, "{}"))
}

// Cached Markers Last Updated
fun Settings.setCachedMarkersLastUpdatedEpochSeconds(epochSeconds: Long) {
    putLong(kCachedMarkersLastUpdatedEpochSeconds, epochSeconds)
}
fun Settings.cachedMarkersLastUpdatedEpochSeconds(): Long {
    return getLong(kCachedMarkersLastUpdatedEpochSeconds, 0L)
}

// Cached Markers Last Location
fun Settings.setCachedMarkersLastLocation(location: Location) {
    putString(kCachedMarkersLastLocation, json.encodeToString(location))
}
fun Settings.cachedMarkersLastLocation(): Location {
    return json.decodeFromString(getString(kCachedMarkersLastLocation, "{latitude:0.0, longitude:0.0}"))
}

// Last Known User Location
fun Settings.setLastKnownUserLocation(location: Location) {
    putString(kLastKnownUserLocation, json.encodeToString(location))
}
fun Settings.lastKnownUserLocation(): Location {
    return json.decodeFromString(getString(kLastKnownUserLocation, "{latitude:0.0, longitude:0.0}"))
}

// For Settings panel
fun Settings.setShowTalkRadius(showTalkRadius: Boolean) {
    putBoolean("showTalkRadius", showTalkRadius)
}
fun Settings.showTalkRadius(): Boolean {
    return getBoolean("showTalkRadius", true)
}

fun Settings.setTalkRadiusMiles(talkRadiusMiles: Double) {
    putDouble("talkRadius", talkRadiusMiles)
}
fun Settings.talkRadiusMiles(): Double {
    return getDouble("talkRadius", 0.5)
}


