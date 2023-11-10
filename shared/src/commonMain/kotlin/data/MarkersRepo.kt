package data

import data.loadMarkers.MarkersResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import presentation.maps.Marker
import presentation.maps.MarkerIdStr
import co.touchlab.kermit.Logger as Log

open class MarkersRepo(
    private val appSettings: AppSettings,
    val updateMarkersResultFlow: MutableStateFlow<MarkersResult> = MutableStateFlow(appSettings.markersResult),
) {
    private var inMemoryMarkersResult = appSettings.markersResult
    val coroutineScope = CoroutineScope(Dispatchers.IO)

    init {
       Log.d { "MarkersRepo: init, instance=$this" }
       updateMarkersResult(appSettings.markersResult)
    }

    // Completely replaces the current MarkersResult with a new value
    private fun updateMarkersResult(newMarkersResult: MarkersResult) {
        //Log.i("MarkersRepo: updateMarkersResult: newMarkersResult.size=${newMarkersResult.markerIdToMarker.size}")
        inMemoryMarkersResult = newMarkersResult

        // debounce the update to improve performance
        coroutineScope.launch {
            delay(100) // debounce
//            delay(250) // debounce
            appSettings.markersResult = newMarkersResult
            updateMarkersResultFlow.emit(newMarkersResult)
        }
    }

    // Pulls from the SSoT in the appSettings, slower than using inMemoryMarkersResult
    fun markersResult() = appSettings.markersResult

    fun addMarker(marker: Marker): MarkersResult {
        this.marker(marker.id)?.run {
            // Log.w("MarkerRepo: addMarker: marker already exists, id: ${marker.id}, add ignored.")
            return inMemoryMarkersResult
        }

        updateMarkersResult(
            inMemoryMarkersResult.copy(
                markerIdToMarker =
                    inMemoryMarkersResult.markerIdToMarker +
                        (marker.id to marker)
            )
        )

        return inMemoryMarkersResult
    }

    fun removeMarker(id: MarkerIdStr): MarkersResult {
        // Log.i("MarkerRepo: removeMarker: id=$id")
        this.marker(id) ?: run {
            updateMarkersResult(appSettings.markersResult) // trigger update to flow anyway
            return inMemoryMarkersResult
        }

        updateMarkersResult(
            inMemoryMarkersResult.copy(
                markerIdToMarker =
                    inMemoryMarkersResult.markerIdToMarker - id
                )
        )

        return inMemoryMarkersResult
    }

    fun marker(id: MarkerIdStr): Marker? {
        return inMemoryMarkersResult.markerIdToMarker[id] // todo use inMemoryMarkersResult for all other methods
    }

    fun markers(): List<Marker> {
        return inMemoryMarkersResult.markerIdToMarker.values.toList()
    }

    fun clearAllMarkers(): MarkersResult {
        updateMarkersResult(
            inMemoryMarkersResult.copy(
                markerIdToMarker = emptyMap()
            )
        )

        return inMemoryMarkersResult
    }

    // Note: Blows away all previous data.  Use with caution.
    fun replaceMarker(replacementMarker: Marker): MarkersResult {
        // Log.i("MarkerRepo: updateAllDataForMarker: replacementMarker.id=${replacementMarker.id}")
        updateMarkersResult(
            inMemoryMarkersResult.copy(
                markerIdToMarker =
                    inMemoryMarkersResult.markerIdToMarker +
                        (replacementMarker.id to replacementMarker)
            )
        )

        return inMemoryMarkersResult
    }

    fun updateMarkerIsSeen(markerToUpdate: Marker, isSeen: Boolean): MarkersResult {
        // Log.i("MarkerRepo: updateMarkerIsSeen: markerToUpdate.id=${markerToUpdate.id}, isSeen=$isSeen")
        val originalMarker =
            this.marker(markerToUpdate.id)
                ?: run {
                    Log.w("MarkerRepo: updateMarkerIsSeen: marker not found, id: ${markerToUpdate.id}")
                    return inMemoryMarkersResult
                }
        if(originalMarker.isSeen == isSeen) { // no change
            updateMarkersResult(appSettings.markersResult)
            return inMemoryMarkersResult
        }

        updateMarkersResult(
            inMemoryMarkersResult.copy(
                markerIdToMarker =
                    inMemoryMarkersResult.markerIdToMarker +
                        (originalMarker.id to originalMarker.copy(
                            isSeen = isSeen
                        ))
            )
        )

        return inMemoryMarkersResult
    }

    fun updateMarkerIsSpoken(
        markerToUpdate: Marker? = null,
        id: MarkerIdStr? = null,
        isSpoken: Boolean
    ): MarkersResult {
        val updateId =
            id ?: markerToUpdate?.id
            ?: run {
                Log.w("MarkerRepo: updateMarkerIsSpoken: marker id found")
                return inMemoryMarkersResult
            }
        // Log.i("MarkerRepo: updateMarkerIsSpoken: markerToUpdate.id=$updateId, isSpoken=$isSpoken")
        val originalMarker =
            this.marker(updateId)
                ?: run {
                    Log.w("MarkerRepo: updateMarkerIsSpoken: marker not found, id: $updateId")
                    return inMemoryMarkersResult
                }
        if(originalMarker.isSpoken == isSpoken) { // no change
            return inMemoryMarkersResult
        }

        updateMarkersResult(
            inMemoryMarkersResult.copy(
                markerIdToMarker =
                inMemoryMarkersResult.markerIdToMarker +
                        (originalMarker.id to originalMarker.copy(
                                isSpoken = isSpoken
                            )
                        )
            )
        )

        return inMemoryMarkersResult
    }

    fun updateMarkerDetails(markerWithUpdatedDetails: Marker): MarkersResult {
        val originalMarker = this.marker(markerWithUpdatedDetails.id)
                ?: run {
                    Log.w("MarkerRepo: updateMarkerDetails: marker not found, id: ${markerWithUpdatedDetails.id}")
                    return inMemoryMarkersResult
                }

        updateMarkersResult(
            inMemoryMarkersResult.copy(
                markerIdToMarker =
                    inMemoryMarkersResult.markerIdToMarker +
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

        return inMemoryMarkersResult
    }

    fun upsertMarkerDetails(marker: Marker): MarkersResult {
        marker(marker.id)?.let {
            updateMarkerDetails(marker)
        } ?: run {
            addMarker(marker)
        }

        return inMemoryMarkersResult
    }

    fun updateMarkerBasicInfo(markerWithUpdatedBasicInfo: Marker): MarkersResult {
        val originalMarker =
            marker(markerWithUpdatedBasicInfo.id)
                ?: run {
                    Log.w("MarkerRepo: updateMarkerBasicInfo: marker not found, id: ${markerWithUpdatedBasicInfo.id}")
                    return inMemoryMarkersResult
                }

        // Check for changes - optimization
        if(originalMarker.position == markerWithUpdatedBasicInfo.position &&
            originalMarker.title == markerWithUpdatedBasicInfo.title &&
            originalMarker.subtitle == markerWithUpdatedBasicInfo.subtitle &&
            originalMarker.alpha == markerWithUpdatedBasicInfo.alpha
        ) { // no change
            return inMemoryMarkersResult
        }
        println("🚹🚹🚹 🛜 MarkerRepo: updateMarkerBasicInfo: markerWithUpdatedBasicInfo.id=${markerWithUpdatedBasicInfo.id}")

        updateMarkersResult(
            inMemoryMarkersResult.copy(
                markerIdToMarker =
                    inMemoryMarkersResult.markerIdToMarker +
                        (originalMarker.id to originalMarker.copy(
                            position = markerWithUpdatedBasicInfo.position,
                            title = markerWithUpdatedBasicInfo.title,
                            subtitle = markerWithUpdatedBasicInfo.subtitle,
                            alpha = markerWithUpdatedBasicInfo.alpha
                        ))
            )
        )

        return inMemoryMarkersResult
    }

    fun upsertMarkerBasicInfo(marker: Marker): MarkersResult {
        // Log.i("MarkerRepo: upsertMarkerBasicInfo: marker.id=${marker.id}")
        marker(marker.id)?.let {
            updateMarkerBasicInfo(marker)
        } ?: run {
            addMarker(marker)
            println("🚹🚹🚹 MarkerRepo: upsertMarkerBasicInfo: added marker.id=${marker.id}")
        }

        return inMemoryMarkersResult
    }

    fun updateIsParseMarkersPageFinished(isFinished: Boolean): MarkersResult {
        updateMarkersResult(
            inMemoryMarkersResult.copy(
                isParseMarkersPageFinished = isFinished
            )
        )

        return inMemoryMarkersResult
    }
}
