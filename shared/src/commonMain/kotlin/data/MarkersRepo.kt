package data

import data.loadMarkers.LoadMarkersResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import presentation.maps.Marker
import presentation.maps.MarkerIdStr
import co.touchlab.kermit.Logger as Log

open class MarkersRepo(
    private val appSettings: AppSettings,
) {
    private val _markersResult2Flow: MutableStateFlow<LoadMarkersResult> =
        MutableStateFlow(appSettings.loadMarkersResult)
    val markersResultFlow =
        _markersResult2Flow.asStateFlow()

    private val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
       Log.d { "MarkersRepo: init, instance=$this" }
       updateLoadMarkersResult(appSettings.loadMarkersResult)
    }

    // Completely replaces the current MarkersResult with a new value
    private fun updateLoadMarkersResult(newLoadMarkersResult: LoadMarkersResult) {
        _markersResult2Flow.update {
            newLoadMarkersResult
        }.also {
            coroutineScope.launch {
                delay(100) // debounce the save to persistent storage
                appSettings.loadMarkersResult = newLoadMarkersResult
            }
        }
    }

    fun markersResultFlow() = markersResultFlow
    fun markersResult() = markersResultFlow.value

    fun marker(id: MarkerIdStr): Marker? {
        return markersResultFlow.value.markerIdToMarkerMap[id]
    }

    fun markers(): List<Marker> {
        return markersResultFlow.value.markerIdToMarkerMap.values.toList()
    }

    fun addMarker(marker: Marker) {
        // Log.i("MarkerRepo: addMarker: marker.id=${marker.id}")
        this.marker(marker.id)?.run {
            Log.w("MarkerRepo: addMarker: marker already exists, id: ${marker.id}, add ignored.")
            return
        }

        updateLoadMarkersResult(
            markersResultFlow.value.copy(
                markerIdToMarkerMap =
                    markersResultFlow.value.markerIdToMarkerMap + (marker.id to marker)
            )
        )
    }

    fun removeMarker(id: MarkerIdStr) {
        // Log.i("MarkerRepo: removeMarker: id=$id")
        this.marker(id) ?: run {
            Log.w("MarkerRepo: removeMarker: marker not found, id: $id")
            return
        }

        updateLoadMarkersResult(
            markersResultFlow.value.copy(
                markerIdToMarkerMap =
                    markersResultFlow.value.markerIdToMarkerMap - id
            )
        )
    }

    fun clearAllMarkers() {
        // Log.i("MarkerRepo: clearAllMarkers: inMemoryLoadMarkersResult.markerIdToMarkerMap.size=${markersResultFlow.value.markerIdToMarkerMap.size}")
        updateLoadMarkersResult(
            markersResultFlow.value.copy(
                markerIdToMarkerMap = emptyMap()
            )
        )
    }

    // Note: Blows away all previous data.  Use with caution.
    fun replaceMarker(replacementMarker: Marker) {
        // Log.i("MarkerRepo: replaceMarker: replacementMarker.id=${replacementMarker.id}")
        updateLoadMarkersResult(
            markersResultFlow.value.copy(
                markerIdToMarkerMap =
                    markersResultFlow.value.markerIdToMarkerMap +
                        (replacementMarker.id to replacementMarker)
            )
        )
    }

    fun updateMarkerIsSeen(markerToUpdate: Marker, isSeen: Boolean) {
        Log.i("MarkerRepo: updateMarkerIsSeen: markerToUpdate.id=${markerToUpdate.id}, isSeen=$isSeen")
        this.marker(markerToUpdate.id)?.let { originalMarker ->
            if(originalMarker.isSeen == isSeen) return@let // no change

            updateLoadMarkersResult(
                markersResultFlow.value.copy(
                    markerIdToMarkerMap =
                        markersResultFlow.value.markerIdToMarkerMap +
                            (originalMarker.id to originalMarker.copy(
                                isSeen = isSeen
                            ))
                )
            )
        } ?: run {
            Log.w("MarkerRepo: updateMarkerIsSeen: marker not found, id: ${markerToUpdate.id}")
            return
        }
    }

    fun updateMarkerIsAnnounced(markerToUpdate: Marker, isAnnounced: Boolean) {
        // Log.i("MarkerRepo: updateMarkerIsAnnounced: markerToUpdate.id=${markerToUpdate.id}, isAnnounced=$isAnnounced")
        this.marker(markerToUpdate.id)?.let { originalMarker ->
            if(originalMarker.isAnnounced == isAnnounced) return@let // no change

            updateLoadMarkersResult(
                markersResultFlow.value.copy(
                    markerIdToMarkerMap =
                        markersResultFlow.value.markerIdToMarkerMap +
                            (originalMarker.id to originalMarker.copy(
                                isAnnounced = isAnnounced
                            ))
                )
            )
        } ?: run {
            Log.w("MarkerRepo: updateMarkerIsAnnounced: marker not found, id: ${markerToUpdate.id}")
            return
        }
    }

    fun updateMarkerIsSpoken(
        markerToUpdate: Marker? = null, // if null, use id
        id: MarkerIdStr? = null,
        isSpoken: Boolean
    ) {
        val updateId =
            id ?: markerToUpdate?.id
                ?: run {
                    Log.w("MarkerRepo: updateMarkerIsSpoken: marker id found")
                    return
                }
        // Log.i("MarkerRepo: updateMarkerIsSpoken: markerToUpdate.id=$updateId, isSpoken=$isSpoken")

        this.marker(updateId)?.let { originalMarker ->
            if(originalMarker.isSpoken == isSpoken) return@let // no change

            updateLoadMarkersResult(
                markersResultFlow.value.copy(
                    markerIdToMarkerMap =
                        markersResultFlow.value.markerIdToMarkerMap +
                            (originalMarker.id to originalMarker.copy(
                                isSpoken = isSpoken
                            ))
                )
            )
        } ?: run {
            Log.w("MarkerRepo: updateMarkerIsSpoken: marker not found, id: $updateId")
            return
        }
    }

    fun updateMarkerDetails(markerWithUpdatedDetails: Marker) {
        val originalMarker = this.marker(markerWithUpdatedDetails.id)
            ?: run {
                Log.w("MarkerRepo: updateMarkerDetails: marker not found, id: ${markerWithUpdatedDetails.id}")
                return
            }

        updateLoadMarkersResult(
            markersResultFlow.value.copy(
                markerIdToMarkerMap =
                    markersResultFlow.value.markerIdToMarkerMap +
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
        )
    }

    fun upsertMarkerDetails(marker: Marker) {
        marker(marker.id)?.let {
            updateMarkerDetails(marker)
        } ?: run {
            addMarker(marker)
        }
    }

    fun updateMarkerBasicInfo(markerWithUpdatedBasicInfo: Marker) {
        val originalMarker =
            marker(markerWithUpdatedBasicInfo.id)
                ?: run {
                    Log.w("MarkerRepo: updateMarkerBasicInfo: marker not found, id: ${markerWithUpdatedBasicInfo.id}")
                    return
                }

        // Check for changes (optimization)
        if(originalMarker.position == markerWithUpdatedBasicInfo.position &&
            originalMarker.title == markerWithUpdatedBasicInfo.title &&
            originalMarker.subtitle == markerWithUpdatedBasicInfo.subtitle &&
            originalMarker.alpha == markerWithUpdatedBasicInfo.alpha
        ) {
            return // no change
        }

        // Log.d("🚹🚹🚹 ⎣ 🛜 MarkerRepo: updateMarkerBasicInfo: UPDATED markerWithUpdatedBasicInfo.id=${markerWithUpdatedBasicInfo.id}")
        updateLoadMarkersResult(
            markersResultFlow.value.copy(
                markerIdToMarkerMap =
                    markersResultFlow.value.markerIdToMarkerMap +
                        (originalMarker.id to originalMarker.copy(
                            position = markerWithUpdatedBasicInfo.position,
                            title = markerWithUpdatedBasicInfo.title,
                            subtitle = markerWithUpdatedBasicInfo.subtitle,
                            alpha = markerWithUpdatedBasicInfo.alpha
                        ))
            )
        )

    }

    fun upsertMarkerBasicInfo(marker: Marker) {
        // Log.i("MarkerRepo: upsertMarkerBasicInfo: marker.id=${marker.id}")
        marker(marker.id)?.let {
            updateMarkerBasicInfo(marker)
        } ?: run {
            //val startTime = Clock.System.now()
            addMarker(marker)
            //Log.d("🚹🚹🚹 ⎣ 🏁 MarkerRepo: upsertMarkerBasicInfo: added marker.id=${marker.id}, took ${Clock.System.now() - startTime}")
        }
    }

    fun updateIsParseMarkersPageFinished(isFinished: Boolean) {
        updateLoadMarkersResult(
            markersResultFlow.value.copy(
                isParseMarkersPageFinished = isFinished
            )
        )
    }
}
