package presentation.app.settingsScreen

import data.AppSettings
import data.AppSettings.Companion.kMarkersLastUpdatedLocation
import data.AppSettings.Companion.kMarkersResult
import data.AppSettings.Companion.kRecentlySeenMarkersSet
import data.AppSettings.Companion.kUiRecentlySeenMarkersList
import data.MarkersRepo
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import presentation.maps.Marker
import presentation.maps.RecentlySeenMarker

fun resetMarkerCacheSettings(
    settings: AppSettings,
//    finalMarkers: SnapshotStateList<Marker>,
    finalMarkers: MutableStateFlow<List<Marker>>,
//    recentlySeenMarkersSet: MutableSet<RecentlySeenMarker>,
    recentlySeenMarkersSet: MutableStateFlow<Set<RecentlySeenMarker>>,
//    uiRecentlySeenMarkersList: List<RecentlySeenMarker>,
    uiRecentlySeenMarkersFlow: MutableStateFlow<List<RecentlySeenMarker>>,
    markersRepo: MarkersRepo,
) {
    // Reset the `seen markers` list, UI elements
//    finalMarkers.clear()
    finalMarkers.update { emptyList() }
//    recentlySeenMarkersSet.clear()
    recentlySeenMarkersSet.update { emptySet() }
    uiRecentlySeenMarkersFlow.update { emptyList() }

    println("RESET uiRecentlySeenMarkersList = ${uiRecentlySeenMarkersFlow.value.map {
        it.id
    }}")

    // Reset the settings cache of markers
    settings.clear(kMarkersResult)
    settings.clear(kMarkersLastUpdatedLocation)
    settings.clear(kRecentlySeenMarkersSet)
    settings.clear(kUiRecentlySeenMarkersList)
    markersRepo.clearAllMarkers()
}
