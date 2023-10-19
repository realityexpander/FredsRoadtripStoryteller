import com.russhwolf.settings.Settings
import kotlinx.serialization.encodeToString
import loadMarkers.MarkersResult
import co.touchlab.kermit.Logger as Log

// Settings Keys
const val kCachedMarkersResult = "cachedMarkersResult"
const val kCachedMarkersLastUpdatedEpochSeconds = "cachedMarkersLastUpdatedEpochSeconds"
const val kCachedMarkersLastLoadLocation = "cachedMarkersLastLocation"
const val kLastKnownUserLocation = "lastKnownUserLocation"
const val kStartBackgroundUpdatesWhenAppLaunches = "startBackgroundUpdatesWhenAppLaunches"
const val kTalkRadiusMilesSetting = "talkRadiusMiles"
const val kShouldShowMarkersLastUpdatedLocation = "shouldShowMarkersLastUpdatedLocation"

fun Settings.printAppSettings() {
    // Show current settings
    Log.d { "keys from settings: $keys" }
    Log.d("Settings: cachedMarkersResult markerInfos.size= " +
            json.decodeFromString<MarkersResult>(getString(kCachedMarkersResult, "{}")).markerInfos.size.toString())
    Log.d("Settings: cachedMarkersLastUpdatedEpochSeconds= " +
            getLong(kCachedMarkersLastUpdatedEpochSeconds, 0L).toString())
    Log.d("Settings: cachedMarkersLastLocation= " +
            getString(kCachedMarkersLastLoadLocation, "{latitude:0.0, longitude:0.0}"))
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

// Cached Markers Last Loaded Location
fun Settings.setCachedMarkersLastUpdatedLocation(location: Location) {
    putString(kCachedMarkersLastLoadLocation, json.encodeToString(location))
}
fun Settings.cachedMarkersLastUpdatedLocation(): Location {
    return json.decodeFromString(getString(kCachedMarkersLastLoadLocation, "{latitude:0.0, longitude:0.0}"))
}

// Last Known User Location
fun Settings.setLastKnownUserLocation(location: Location) {
    putString(kLastKnownUserLocation, json.encodeToString(location))
}
fun Settings.lastKnownUserLocation(): Location {
    return json.decodeFromString(getString(kLastKnownUserLocation, "{latitude:0.0, longitude:0.0}"))
}

// • For Settings panel
fun Settings.setShouldAutomaticallyStartTrackingWhenAppLaunches(shouldStartBackgroundUpdatesWhenAppLaunches: Boolean) {
    putBoolean(kStartBackgroundUpdatesWhenAppLaunches, shouldStartBackgroundUpdatesWhenAppLaunches)
}
fun Settings.shouldAutomaticallyStartTrackingWhenAppLaunches(): Boolean {
    return getBoolean(kStartBackgroundUpdatesWhenAppLaunches, false)
}

fun Settings.setTalkRadiusMiles(talkRadiusMiles: Double) {
    putDouble(kTalkRadiusMilesSetting, talkRadiusMiles)
}
fun Settings.talkRadiusMiles(): Double {
    return getDouble(kTalkRadiusMilesSetting, 0.5)
}

fun Settings.shouldShowMarkersLastUpdatedLocation(): Boolean {
    return getBoolean(kShouldShowMarkersLastUpdatedLocation, false)
}
fun Settings.setShouldShowMarkersLastUpdatedLocation(shouldShowMarkersLastUpdatedLocation: Boolean) {
    putBoolean(kShouldShowMarkersLastUpdatedLocation, shouldShowMarkersLastUpdatedLocation)
}
