package data

import Location
import com.russhwolf.settings.Settings
import com.russhwolf.settings.set
import kotlinx.serialization.encodeToString
import data.loadMarkers.MarkersResult
import json
import co.touchlab.kermit.Logger as Log

// Settings Keys
const val kSettingMarkersResult =                         "kSettingMarkersResult"
const val kSettingMarkersLastUpdateEpochSeconds =         "kSettingMarkersLastUpdateEpochSeconds"
const val kSettingMarkersLastLoadLocation =               "kSettingMarkersLastLoadLocation"
const val kSettingLastKnownUserLocation =                 "kSettingLastKnownUserLocation"
const val kSettingStartBackgroundUpdatesWhenAppLaunches = "kSettingStartBackgroundUpdatesWhenAppLaunches"
const val kSettingTalkRadiusMiles =                       "kSettingTalkRadiusMiles"
const val kSettingIsMarkersLastUpdatedLocationVisible =   "kSettingIsMarkersLastUpdatedLocationVisible"
const val kSettingIsRecentlySeenMarkersPanelVisible =     "kSettingIsRecentlySeenMarkersPanelVisible"
const val kSettingIsPermissionsGranted =                  "kSettingIsPermissionsGranted"
const val kShouldSpeakAutomaticallyWhenUnseenMarkerFound ="kShouldSpeakAutomaticallyWhenUnseenMarkerFound"
const val kSettingRecentlySeenMarkers =                   "kSettingRecentlySeenMarkers"

fun Settings.printAppSettings() {
    // Show current settings
    Log.d { "All keys from settings: ${keys.joinToString("") { it + "\n" }}" }
    Log.d(" Settings: markersResult markerIdToMapMarkerMap.size= " +
            markersResult().markerIdToMapMarkerMap.size.toString())
    Log.d(" Settings: markersLastUpdateEpochSeconds= " +
            markersLastUpdateEpochSeconds().toString())
    Log.d(" Settings: markersLastUpdatedLocation= " +
            markersLastUpdatedLocation().toString())
    Log.d(" Settings: lastKnownUserLocation= " +
            lastKnownUserLocation().toString())
    Log.d(" Settings: isAutomaticStartBackgroundUpdatesWhenAppLaunchTurnedOn= " +
            isAutomaticStartBackgroundUpdatesWhenAppLaunchTurnedOn().toString())
    Log.d(" Settings: talkRadiusMiles= " +
           talkRadiusMiles().toString())
    Log.d(" Settings: isMarkersLastUpdatedLocationVisible= " +
            isMarkersLastUpdatedLocationVisible().toString())
    Log.d(" Settings: isRecentlySeenMarkersPanelVisible= " +
            isRecentlySeenMarkersPanelVisible().toString())
    Log.d(" Settings: shouldSpeakAutomaticallyWhenNewMarkersAreFound= " +
            shouldSpeakAutomaticallyWhenUnseenMarkerFound().toString())
}

fun Settings.setMarkersResult(markersResult: MarkersResult) {
    putString(kSettingMarkersResult, json.encodeToString(markersResult))
}
fun Settings.markersResult(): MarkersResult {
    return json.decodeFromString(getString(kSettingMarkersResult, "{}"))
}
fun Settings.clearMarkersResult() {
    set(kSettingMarkersResult, null)
}

fun Settings.setMarkersLastUpdateEpochSeconds(epochSeconds: Long) {
    putLong(kSettingMarkersLastUpdateEpochSeconds, epochSeconds)
}
fun Settings.markersLastUpdateEpochSeconds(): Long {
    return getLong(kSettingMarkersLastUpdateEpochSeconds, 0L)
}
fun Settings.clearMarkersLastUpdateEpochSeconds() {
    set(kSettingMarkersLastUpdateEpochSeconds, null)
}

fun Settings.setMarkersLastUpdatedLocation(location: Location) {
    putString(kSettingMarkersLastLoadLocation, json.encodeToString(location))
}
fun Settings.markersLastUpdatedLocation(): Location {
    return json.decodeFromString(getString(kSettingMarkersLastLoadLocation, "{latitude:0.0, longitude:0.0}"))
}
fun Settings.clearMarkersLastUpdatedLocation() {
    set(kSettingMarkersLastLoadLocation, null)
}

fun Settings.setLastKnownUserLocation(location: Location) {
    putString(kSettingLastKnownUserLocation, json.encodeToString(location))
}
fun Settings.lastKnownUserLocation(): Location {
    return json.decodeFromString(getString(kSettingLastKnownUserLocation, "{latitude:0.0, longitude:0.0}"))
}

// todo - make this a unified Data Class? (for recentlySeenMarkers & recentlySeenMarkersForUIList)
fun Settings.setRecentlySeenMarkers(recentlySeenMarkers: List<String>) {
    putString(kSettingRecentlySeenMarkers, json.encodeToString(recentlySeenMarkers))
}
fun Settings.recentlySeenMarkers(): List<String> {
    return json.decodeFromString(getString(kSettingRecentlySeenMarkers, "[]"))
}


// • For Settings panel

fun Settings.setShouldSpeakAutomaticallyWhenUnseenMarkerFound(shouldSpeakAutomaticallyWhenUnseenMarkerFound: Boolean) {
    putBoolean(kShouldSpeakAutomaticallyWhenUnseenMarkerFound, shouldSpeakAutomaticallyWhenUnseenMarkerFound)
}
fun Settings.shouldSpeakAutomaticallyWhenUnseenMarkerFound(): Boolean {
    return getBoolean(kShouldSpeakAutomaticallyWhenUnseenMarkerFound, false)
}

fun Settings.setIsAutomaticStartBackgroundUpdatesWhenAppLaunchTurnedOn(shouldStartBackgroundUpdatesWhenAppLaunches: Boolean) {
    putBoolean(kSettingStartBackgroundUpdatesWhenAppLaunches, shouldStartBackgroundUpdatesWhenAppLaunches)
}
fun Settings.isAutomaticStartBackgroundUpdatesWhenAppLaunchTurnedOn(): Boolean {
    return getBoolean(kSettingStartBackgroundUpdatesWhenAppLaunches, false)
}

fun Settings.setTalkRadiusMiles(talkRadiusMiles: Double) {
    putDouble(kSettingTalkRadiusMiles, talkRadiusMiles)
}
fun Settings.talkRadiusMiles(): Double {
    return getDouble(kSettingTalkRadiusMiles, 0.5)
}

fun Settings.isMarkersLastUpdatedLocationVisible(): Boolean {
    return getBoolean(kSettingIsMarkersLastUpdatedLocationVisible, false)
}
fun Settings.setIsMarkersLastUpdatedLocationVisible(isMarkersLastUpdatedLocationVisible: Boolean) {
    putBoolean(kSettingIsMarkersLastUpdatedLocationVisible, isMarkersLastUpdatedLocationVisible)
}

fun Settings.isRecentlySeenMarkersPanelVisible(): Boolean {
    return getBoolean(kSettingIsRecentlySeenMarkersPanelVisible, false)
}
fun Settings.setIsRecentlySeenMarkersPanelVisible(isRecentlySeenMarkersPanelVisible: Boolean) {
    putBoolean(kSettingIsRecentlySeenMarkersPanelVisible, isRecentlySeenMarkersPanelVisible)
}

fun Settings.isPermissionsGranted(): Boolean {
    return getBoolean(kSettingIsPermissionsGranted, false)
}
fun Settings.setIsPermissionsGranted(isPermissionsGranted: Boolean) {
    putBoolean(kSettingIsPermissionsGranted, isPermissionsGranted)
}
