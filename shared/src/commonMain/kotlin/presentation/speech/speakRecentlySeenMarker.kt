package presentation.speech

import data.util.LoadingState
import data.MarkersRepo
import data.appSettings
import data.loadMarkerDetails.calculateMarkerDetailsPageUrl
import data.loadMarkerDetails.parseMarkerDetailsPageHtml
import data.loadMarkerDetails.sampleData.almadenVineyardsM2580MarkerDetailsHtml
import io.ktor.client.call.body
import io.ktor.client.request.get
import isTextToSpeechSpeaking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import data.network.httpClient
import kotlinx.coroutines.CancellationException
import presentation.maps.RecentlySeenMarker
import co.touchlab.kermit.Logger as Log

/**
 * Speaks the marker details, if the marker is not already spoken.
 * @return the marker that was spoken, or null if the marker was already spoken.
 */
fun speakRecentlySeenMarker(
    speakMarker: RecentlySeenMarker,
    isSpeakDetailsEnabled: Boolean = appSettings.isSpeakDetailsWhenUnseenMarkerFoundEnabled,
    markersRepo: MarkersRepo,
    coroutineScope: CoroutineScope,
    useFakeData: Boolean = false,
    onUpdateLoadingState: (LoadingState<String>) -> Unit = { },
    onSetUnspokenText: (String) -> Unit = { },
    onError: (String) -> Unit = { },
): RecentlySeenMarker {
    if (isTextToSpeechSpeaking()) {
        return appSettings.lastSpokenRecentlySeenMarker
    }

    val marker = markersRepo.marker(speakMarker.id)
        ?: throw Exception("Marker not found for id: ${speakMarker.id}")
    appSettings.lastSpokenRecentlySeenMarker = speakMarker

    // Update the 'isSpoken' flag
    markersRepo.updateMarkerIsSpoken(marker, isSpoken = true)

    if (isSpeakDetailsEnabled) {
        if (!marker.isDetailsLoaded) {
            coroutineScope.launch {
                // Load the marker details
                try {
                     if (!useFakeData) {
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
                            isSpeakDetailsEnabled,
                            onSetUnspokenText = onSetUnspokenText
                        )
                    } else {
                        // loadingState = fakeLoadingStateForMarkerDetailsPageHtml(mapMarker)  // for debugging - LEAVE FOR REFERENCE
                        val markerDetailsPageHtml = almadenVineyardsM2580MarkerDetailsHtml()
                        val (_, parsedDetailsMarker) =
                            parseMarkerDetailsPageHtml(markerDetailsPageHtml)

                        Pair(parsedDetailsMarker, null)
                    }
                } catch(e: CancellationException) {
                    throw e
                } catch (e: Exception) {
                    e.printStackTrace()
                    onUpdateLoadingState(LoadingState.Error(e.message ?: "Loading error"))
                    onError(
                        "Loading details error: " + (e.message ?: e.cause?.message
                        ?: "Loading error")
                    )
                    return@launch
                }
            }

            return appSettings.lastSpokenRecentlySeenMarker // early return due to async loading
        }

        // Already have the details, so just speak the marker.
        speakMarker(marker, isSpeakDetailsEnabled, onSetUnspokenText)
    } else {
        speakMarker(
            markersRepo.marker(speakMarker.id)
                ?: return appSettings.lastSpokenRecentlySeenMarker, // if marker is null, default to the last spoken marker.
            shouldSpeakDetails = false,
            onSetUnspokenText = onSetUnspokenText
        )
    }

    return speakMarker
}
