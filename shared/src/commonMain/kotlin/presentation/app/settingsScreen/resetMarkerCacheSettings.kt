package presentation.app.settingsScreen

import androidx.compose.runtime.snapshots.SnapshotStateList
import data.AppSettings
import data.AppSettings.Companion.kMarkersLastUpdatedLocation
import data.AppSettings.Companion.kMarkersResult
import data.AppSettings.Companion.kRecentlySeenMarkersSet
import data.AppSettings.Companion.kUiRecentlySeenMarkersList
import data.MarkersRepo
import presentation.maps.Marker
import presentation.maps.RecentlySeenMarker

fun resetMarkerCacheSettings(
    settings: AppSettings,
    finalMarkers: SnapshotStateList<Marker>,
    recentlySeenMarkersSet: MutableSet<RecentlySeenMarker>,
    uiRecentlySeenMarkersList: SnapshotStateList<RecentlySeenMarker>,
    markersRepo: MarkersRepo,
) {
    // Reset the `seen markers` list, UI elements
    finalMarkers.clear()
    recentlySeenMarkersSet.clear()
    uiRecentlySeenMarkersList.clear()

    // Reset the settings cache of markers
    settings.clear(kMarkersResult)
    settings.clear(kMarkersLastUpdatedLocation)
    settings.clear(kRecentlySeenMarkersSet)
    settings.clear(kUiRecentlySeenMarkersList)
    markersRepo.clearAllMarkers()
}
