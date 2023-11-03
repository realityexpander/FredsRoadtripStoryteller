package presentation.speech

import data.util.LoadingState
import data.MarkersRepo
import data.appSettings
import data.loadMarkerDetails.calculateMarkerDetailsPageUrl
import data.loadMarkerDetails.parseMarkerDetailsPageHtml
import data.loadMarkerDetails.sampleData.almadenVineyardsM2580
import io.ktor.client.call.body
import io.ktor.client.request.get
import isTextToSpeechSpeaking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import data.network.httpClient
import presentation.maps.RecentlySeenMarker
import co.touchlab.kermit.Logger as Log

var lastSpeakMarker: RecentlySeenMarker? = null

fun speakRecentlySeenMarker(
    speakMarker: RecentlySeenMarker,
    shouldSpeakDetails: Boolean = appSettings.shouldSpeakDetailsWhenUnseenMarkerFound,
    coroutineScope: CoroutineScope,
    onError: (String) -> Unit = { },
    markersRepo: MarkersRepo,
    onUpdateLoadingState: (LoadingState<String>) -> Unit = { },
): RecentlySeenMarker? {
    if (isTextToSpeechSpeaking()) {
        Log.d("speakRecentlySeenMarker: isTextToSpeechSpeaking() is true, so returning early.")
        return lastSpeakMarker
    }

    val marker = markersRepo.marker(speakMarker.id)
        ?: throw Exception("Marker not found for id: ${speakMarker.id}")
    lastSpeakMarker = speakMarker

    // Update the 'isSpoken' flag
    markersRepo.updateMarkerIsSpoken(marker, isSpoken = true)

    if (shouldSpeakDetails) {
            if (!marker.isDetailsLoaded) {
                coroutineScope.launch {
                    // Load the marker details
                    try {
                        // if (!useFakeData) {
                        if (true) {
                            val markerDetailsPageUrl = marker.id.calculateMarkerDetailsPageUrl()
                            Log.d("loading network markerDetailsPageUrl = $markerDetailsPageUrl")

                            onUpdateLoadingState(LoadingState.Loading)
                            val response = httpClient.get(markerDetailsPageUrl)
                            val markerDetailsPageHtml = response.body<String>()

                            // parse the marker details page html into a Marker object
                            val (errorMessage, parsedDetailsMarker) =
                                parseMarkerDetailsPageHtml(markerDetailsPageHtml)
                            errorMessage?.run { throw Exception(errorMessage) }

                            // Update the marker details
                            parsedDetailsMarker ?: throw Exception("parsedDetailsMarker is null")
                                markersRepo.updateMarkerDetails(
                                    parsedDetailsMarker.copy(id=marker.id)
                                )
                            onUpdateLoadingState(LoadingState.Finished)

                            speakMarker(
                                parsedDetailsMarker,
                                shouldSpeakDetails
                            )
                        } else {
                            // loadingState = fakeLoadingStateForMarkerDetailsPageHtml(mapMarker)  // for debugging - LEAVE FOR REFERENCE
                            val markerDetailsPageHtml = almadenVineyardsM2580()
                            val (_, parsedDetailsMarker) =
                                parseMarkerDetailsPageHtml(markerDetailsPageHtml)
                            Pair(parsedDetailsMarker, null)
                        }
                    } catch (e: Exception) {
                        onUpdateLoadingState(LoadingState.Error(e.message ?: "Loading error"))
                        onError(
                            "Loading details error: " + (e.message ?: e.cause?.message
                            ?: "Loading error")
                        )
                        return@launch
                    }
                }

                return lastSpeakMarker // early return due to async loading
            }

            // Already have the details, so just speak the marker.
            speakMarker(marker, shouldSpeakDetails)
    } else {
        speakMarker(
            markersRepo.marker(speakMarker.id) ?: return lastSpeakMarker,
            shouldSpeakDetails = false
        )
    }

    return speakMarker
}
