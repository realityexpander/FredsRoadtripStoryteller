package data

import data.loadMarkers.MarkersResult
import kotlinx.coroutines.flow.MutableStateFlow
import maps.Marker
import maps.MarkerIdStr
import co.touchlab.kermit.Logger as Log

open class MarkerRepo(
    private val appSettings: AppSettings,
    val markerRepoUpdateFlow: MutableStateFlow<MarkersResult> = MutableStateFlow(appSettings.markersResult)
) {
    fun addMarker(marker: Marker): MarkersResult {
        // todo should update if already exists?
        if(appSettings.markersResult.markerIdToMarker.containsKey(marker.id)) {
            // Log.w("MarkerRepo: updateMarkerDetails: marker already exists, id: ${marker.id}")
            return appSettings.markersResult
        }

        appSettings.markersResult = appSettings.markersResult.copy(
            markerIdToMarker =
                appSettings.markersResult.markerIdToMarker +
                    (marker.id to marker)
        )

        markerRepoUpdateFlow.tryEmit(appSettings.markersResult)
        return appSettings.markersResult
    }

    fun removeMarker(id: MarkerIdStr): MarkersResult {
        if(!appSettings.markersResult.markerIdToMarker.containsKey(id)) {
            return appSettings.markersResult
        }

        appSettings.markersResult = appSettings.markersResult.copy(
            markerIdToMarker =
                appSettings.markersResult.markerIdToMarker - id
        )

        markerRepoUpdateFlow.tryEmit(appSettings.markersResult)
        return appSettings.markersResult
    }

    fun marker(id: MarkerIdStr): Marker? {
        return appSettings.markersResult.markerIdToMarker[id]
    }

    fun markers(): List<Marker> {
        return appSettings.markersResult.markerIdToMarker.values.toList()
    }

    fun clearAllMarkers(): MarkersResult {
        appSettings.markersResult = appSettings.markersResult.copy(
            markerIdToMarker = emptyMap()
        )

        markerRepoUpdateFlow.tryEmit(appSettings.markersResult)
        return appSettings.markersResult
    }

    // Note: Blows away all old data.  Use with caution.
    fun updateMarker(marker: Marker): MarkersResult {
        appSettings.markersResult = appSettings.markersResult.copy(
            markerIdToMarker =
                appSettings.markersResult.markerIdToMarker +
                    (marker.id to marker)
        )

        markerRepoUpdateFlow.tryEmit(appSettings.markersResult)
        return appSettings.markersResult
    }

    fun updateMarkerIsSeen(markerToUpdate: Marker, isSeen: Boolean): MarkersResult {
        val originalMarker =
            appSettings.markersResult.markerIdToMarker[markerToUpdate.id]
                ?: run {
                    Log.w("MarkerRepo: updateMarkerIsSeen: marker not found, id: ${markerToUpdate.id}")
                    return appSettings.markersResult
                }
        if(originalMarker.isSeen) {
            return appSettings.markersResult
        }

        appSettings.markersResult = appSettings.markersResult.copy(
            markerIdToMarker =
                appSettings.markersResult.markerIdToMarker +
                    (originalMarker.id to originalMarker.copy(
                        isSeen = isSeen
                    ))
        )

        markerRepoUpdateFlow.tryEmit(appSettings.markersResult)
        return appSettings.markersResult
    }

    fun updateMarkerDetails(markerWithUpdatedDetails: Marker): MarkersResult {
        // todo - should add marker if not found?
        val originalMarker =
            appSettings.markersResult.markerIdToMarker[markerWithUpdatedDetails.id]
                ?: run {
                    Log.w("MarkerRepo: updateMarkerDetails: marker not found, id: ${markerWithUpdatedDetails.id}")
                    return appSettings.markersResult
                }

        appSettings.markersResult = appSettings.markersResult.copy(
            markerIdToMarker =
            appSettings.markersResult.markerIdToMarker +
                    (originalMarker.id to originalMarker.copy(
                        isDetailsLoaded = true, // force to indicate details have been loaded
                        markerDetailsPageUrl = markerWithUpdatedDetails.markerDetailsPageUrl,
                        mainPhotoUrl = markerWithUpdatedDetails.mainPhotoUrl,
                        markerPhotos = markerWithUpdatedDetails.markerPhotos,
                        photoCaptions = markerWithUpdatedDetails.photoCaptions,
                        photoAttributions = markerWithUpdatedDetails.photoAttributions,
                        inscription = markerWithUpdatedDetails.inscription,
                        englishInscription = markerWithUpdatedDetails.englishInscription,
                        spanishInscription = markerWithUpdatedDetails.spanishInscription,
                        erected = markerWithUpdatedDetails.erected,
                        credits = markerWithUpdatedDetails.credits,
                        location = markerWithUpdatedDetails.location,
                        markerPhotos2 = markerWithUpdatedDetails.markerPhotos2,
                        lastUpdatedDetailsEpochSeconds = markerWithUpdatedDetails.lastUpdatedDetailsEpochSeconds
                    ))

        )

        markerRepoUpdateFlow.tryEmit(appSettings.markersResult)
        return appSettings.markersResult
    }

    fun updateMarkerBasicInfo(markerWithBasicInfo: Marker): MarkersResult {
        // todo - should add marker if not found?
        val originalMarker =
            appSettings.markersResult.markerIdToMarker[markerWithBasicInfo.id]
                ?: run {
                    Log.w("MarkerRepo: updateMarkerBasicInfo: marker not found, id: ${markerWithBasicInfo.id}")
                    return appSettings.markersResult
                }

        appSettings.markersResult = appSettings.markersResult.copy(
            markerIdToMarker =
                appSettings.markersResult.markerIdToMarker +
                    (originalMarker.id to originalMarker.copy(
                        position = markerWithBasicInfo.position,
                        title = markerWithBasicInfo.title,
                        subtitle = markerWithBasicInfo.subtitle,
                        alpha = markerWithBasicInfo.alpha
                    ))
        )

        markerRepoUpdateFlow.tryEmit(appSettings.markersResult)
        return appSettings.markersResult
    }

}
