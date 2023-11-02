package presentation.speech

import data.MarkersRepo
import data.appSettings
import data.loadMarkerDetails.calculateMarkerDetailsPageUrl
import data.loadMarkerDetails.parseMarkerDetailsPageHtml
import data.loadMarkerDetails.sampleData.almadenVineyardsM2580
import data.loadMarkers.MarkersResult
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import maps.RecentlySeenMarker
import network.httpClient
import speakTextToSpeech
import co.touchlab.kermit.Logger as Log

fun speakRecentlySeenMarker(
    recentlySeenMarker: RecentlySeenMarker,
    includeDetails: Boolean = false,
    coroutineScope: CoroutineScope,
    onError: (String) -> Unit = { },
    onUpdateMarkersResult: (MarkersResult) -> Unit = {}, // if details are loaded, update the master markers list.
    markersRepo: MarkersRepo
) {
   if(includeDetails) {
       val marker = markersRepo.marker(recentlySeenMarker.id)

       marker?.let {
           if(!marker.isDetailsLoaded) {
               coroutineScope.launch {
                   // Load the marker details
                   try {
                       // if (!useFakeData) {
                       if (true) {
                           val markerDetailsPageUrl = marker.id.calculateMarkerDetailsPageUrl()
                           Log.d("loading network markerDetailsPageUrl = $markerDetailsPageUrl")
                           val response = httpClient.get(markerDetailsPageUrl)
                           val markerDetailsPageHtml = response.body<String>()

                           // parse the marker details page html into a Marker object
                           val (errorMessage, parsedDetailsMarker) =
                               parseMarkerDetailsPageHtml(markerDetailsPageHtml)
                           errorMessage?.run { throw Exception(errorMessage) }

                           // Update the marker details
                           parsedDetailsMarker ?: throw Exception("parsedDetailsMarker is null")
                           val result = markersRepo.updateMarkerDetails(parsedDetailsMarker)
                           onUpdateMarkersResult(result)

                           speakMarker(
                               parsedDetailsMarker,
                               appSettings.shouldSpeakDetailsWhenUnseenMarkerFound
                           )
                       } else {
                           // loadingState = fakeLoadingStateForMarkerDetailsPageHtml(mapMarker)  // for debugging - LEAVE FOR REFERENCE
                           val markerDetailsPageHtml = almadenVineyardsM2580()
                           val (_, parsedDetailsMarker) =
                               parseMarkerDetailsPageHtml(markerDetailsPageHtml)
                           Pair(parsedDetailsMarker, null)
                       }
                   } catch (e: Exception) {
                       onError("Loading details error: " + (e.message ?: e.cause?.message ?: "Loading error"))
                       return@launch
                   }
               }

               return@let // early return due to async loading
           }

           // Already have the details, so just speak the marker.
           speakMarker(
               marker,
               appSettings.shouldSpeakDetailsWhenUnseenMarkerFound
           )
       }
   } else {
       speakTextToSpeech(recentlySeenMarker.title)
   }
}
