package data

import data.loadMarkers.LoadMarkersResult
import kotlinx.atomicfu.atomic
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.atomicfu.update
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.InternalCoroutinesApi
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import presentation.maps.Marker
import presentation.maps.MarkerIdStr
import kotlin.time.Duration.Companion.milliseconds
import co.touchlab.kermit.Logger as Log

open class MarkersRepo(
    private val appSettings: AppSettings,
    val updateLoadMarkersResultFlow: MutableStateFlow<LoadMarkersResult> = MutableStateFlow(appSettings.loadMarkersResult),
) {
    private var inMemoryLoadMarkersResult = appSettings.loadMarkersResult
    private val ioCoroutineScope = CoroutineScope(Dispatchers.IO)
    private val synchronizedObject = SynchronizedObject()
    private val atomicInMemoryLoadMarkersResult = atomic(appSettings.loadMarkersResult)

    init {
       Log.d { "MarkersRepo: init, instance=$this" }
       updateLoadMarkersResult(appSettings.loadMarkersResult)
    }

    // Completely replaces the current MarkersResult with a new value
    private fun updateLoadMarkersResult(newLoadMarkersResult: LoadMarkersResult) {
        //Log.i("MarkersRepo: updateMarkersResult: newMarkersResult.size=${newMarkersResult.markerIdToMarker.size}")
        synchronized(synchronizedObject) {
            inMemoryLoadMarkersResult = newLoadMarkersResult

            ioCoroutineScope.launch {
                // debounce the update to improve performance
                delay(250.milliseconds) // debounce // 50ms is too fast, 150ms seems good

                appSettings.loadMarkersResult = newLoadMarkersResult // save to persistent storage
                updateLoadMarkersResultFlow.emit(newLoadMarkersResult)
            }
        }
        //inMemoryLoadMarkersResult = newLoadMarkersResult

    }

    fun markersResult() = inMemoryLoadMarkersResult // Uses the in-memory lookup

    fun addMarker(marker: Marker): LoadMarkersResult {
        this.marker(marker.id)?.run {
            // Log.w("MarkerRepo: addMarker: marker already exists, id: ${marker.id}, add ignored.")
            return inMemoryLoadMarkersResult
        }

        updateLoadMarkersResult(
            inMemoryLoadMarkersResult.copy(
                markerIdToMarkerMap =
                    inMemoryLoadMarkersResult.markerIdToMarkerMap + (marker.id to marker)
            )
        )

        return inMemoryLoadMarkersResult
    }

    fun removeMarker(id: MarkerIdStr): LoadMarkersResult {
        // Log.i("MarkerRepo: removeMarker: id=$id")
        this.marker(id) ?: run {
            return inMemoryLoadMarkersResult
        }

        updateLoadMarkersResult(
            inMemoryLoadMarkersResult.copy(
                markerIdToMarkerMap =
                    inMemoryLoadMarkersResult.markerIdToMarkerMap - id
                )
        )

        return inMemoryLoadMarkersResult
    }

    fun marker(id: MarkerIdStr): Marker? {
        return inMemoryLoadMarkersResult.markerIdToMarkerMap[id]
    }

    fun markers(): List<Marker> {
        return inMemoryLoadMarkersResult.markerIdToMarkerMap.values.toList()
    }

    fun clearAllMarkers(): LoadMarkersResult {
        // Clearing markers is immediate.  No need to debounce.
        inMemoryLoadMarkersResult = inMemoryLoadMarkersResult.copy(
            markerIdToMarkerMap = emptyMap(),
        )
        ioCoroutineScope.launch {
            appSettings.loadMarkersResult = inMemoryLoadMarkersResult
            updateLoadMarkersResultFlow.emit(inMemoryLoadMarkersResult)
        }

        return inMemoryLoadMarkersResult
    }

    // Note: Blows away all previous data.  Use with caution.
    fun replaceMarker(replacementMarker: Marker): LoadMarkersResult {
        // Log.i("MarkerRepo: updateAllDataForMarker: replacementMarker.id=${replacementMarker.id}")
        updateLoadMarkersResult(
            inMemoryLoadMarkersResult.copy(
                markerIdToMarkerMap =
                    inMemoryLoadMarkersResult.markerIdToMarkerMap +
                        (replacementMarker.id to replacementMarker)
            )
        )

        return inMemoryLoadMarkersResult
    }

    fun updateMarkerIsSeen(markerToUpdate: Marker, isSeen: Boolean): LoadMarkersResult {
        // Log.i("MarkerRepo: updateMarkerIsSeen: markerToUpdate.id=${markerToUpdate.id}, isSeen=$isSeen")
        val originalMarker =
            this.marker(markerToUpdate.id)
                ?: run {
                    Log.w("MarkerRepo: updateMarkerIsSeen: marker not found, id: ${markerToUpdate.id}")
                    return inMemoryLoadMarkersResult
                }

        updateLoadMarkersResult(
            inMemoryLoadMarkersResult.copy(
                markerIdToMarkerMap =
                    inMemoryLoadMarkersResult.markerIdToMarkerMap +
                        (originalMarker.id to originalMarker.copy(
                            isSeen = isSeen
                        ))
            )
        )

        return inMemoryLoadMarkersResult
    }

    fun updateMarkerIsSpoken(
        markerToUpdate: Marker? = null,
        id: MarkerIdStr? = null,
        isSpoken: Boolean
    ): LoadMarkersResult {
        val updateId =
            id ?: markerToUpdate?.id
            ?: run {
                Log.w("MarkerRepo: updateMarkerIsSpoken: marker id found")
                return inMemoryLoadMarkersResult
            }
        // Log.i("MarkerRepo: updateMarkerIsSpoken: markerToUpdate.id=$updateId, isSpoken=$isSpoken")
        val originalMarker =
            this.marker(updateId)
                ?: run {
                    Log.w("MarkerRepo: updateMarkerIsSpoken: marker not found, id: $updateId")
                    return inMemoryLoadMarkersResult
                }
        if(originalMarker.isSpoken == isSpoken) { // no change
            return inMemoryLoadMarkersResult
        }

        updateLoadMarkersResult(
            inMemoryLoadMarkersResult.copy(
                markerIdToMarkerMap =
                inMemoryLoadMarkersResult.markerIdToMarkerMap +
                        (originalMarker.id to originalMarker.copy(
                                isSpoken = isSpoken
                            )
                        )
            )
        )

        return inMemoryLoadMarkersResult
    }

    fun updateMarkerDetails(markerWithUpdatedDetails: Marker): LoadMarkersResult {
        val originalMarker = this.marker(markerWithUpdatedDetails.id)
                ?: run {
                    Log.w("MarkerRepo: updateMarkerDetails: marker not found, id: ${markerWithUpdatedDetails.id}")
                    return inMemoryLoadMarkersResult
                }

        updateLoadMarkersResult(
            inMemoryLoadMarkersResult.copy(
                markerIdToMarkerMap =
                    inMemoryLoadMarkersResult.markerIdToMarkerMap +
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

        return inMemoryLoadMarkersResult
    }

    fun upsertMarkerDetails(marker: Marker): LoadMarkersResult {
        marker(marker.id)?.let {
            updateMarkerDetails(marker)
        } ?: run {
            addMarker(marker)
        }

        return inMemoryLoadMarkersResult
    }

    fun updateMarkerBasicInfo(markerWithUpdatedBasicInfo: Marker): LoadMarkersResult {
        val originalMarker =
            marker(markerWithUpdatedBasicInfo.id)
                ?: run {
                    Log.w("MarkerRepo: updateMarkerBasicInfo: marker not found, id: ${markerWithUpdatedBasicInfo.id}")
                    return inMemoryLoadMarkersResult
                }

        // Check for changes - optimization
        if(originalMarker.position == markerWithUpdatedBasicInfo.position &&
            originalMarker.title == markerWithUpdatedBasicInfo.title &&
            originalMarker.subtitle == markerWithUpdatedBasicInfo.subtitle &&
            originalMarker.alpha == markerWithUpdatedBasicInfo.alpha
        ) { // no change
            return inMemoryLoadMarkersResult
        }

        // Log.d("🚹🚹🚹 ⎣ 🛜 MarkerRepo: updateMarkerBasicInfo: UPDATED markerWithUpdatedBasicInfo.id=${markerWithUpdatedBasicInfo.id}")
        updateLoadMarkersResult(
            inMemoryLoadMarkersResult.copy(
                markerIdToMarkerMap =
                inMemoryLoadMarkersResult.markerIdToMarkerMap +
                    (originalMarker.id to originalMarker.copy(
                        position = markerWithUpdatedBasicInfo.position,
                        title = markerWithUpdatedBasicInfo.title,
                        subtitle = markerWithUpdatedBasicInfo.subtitle,
                        alpha = markerWithUpdatedBasicInfo.alpha
                    ))
            )
        )

        return inMemoryLoadMarkersResult
    }

    fun upsertMarkerBasicInfo(marker: Marker): LoadMarkersResult {
        // Log.i("MarkerRepo: upsertMarkerBasicInfo: marker.id=${marker.id}")
        marker(marker.id)?.let {
            updateMarkerBasicInfo(marker)
        } ?: run {
            //val startTime = Clock.System.now()
            addMarker(marker)
            Log.d("🚹🚹🚹 MarkerRepo: upsertMarkerBasicInfo: ADDED marker.id=${marker.id}")
            //Log.d("🚹🚹🚹 ⎣ 🏁 MarkerRepo: upsertMarkerBasicInfo: added marker.id=${marker.id}, took ${Clock.System.now() - startTime}")
        }

        return inMemoryLoadMarkersResult
    }

    fun updateIsParseMarkersPageFinished(isFinished: Boolean): LoadMarkersResult {
        updateLoadMarkersResult(
            inMemoryLoadMarkersResult.copy(
                isParseMarkersPageFinished = isFinished
            )
        )

        return inMemoryLoadMarkersResult
    }
}
