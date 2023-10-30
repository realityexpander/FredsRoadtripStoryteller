package data.loadMarkerDetails

import maps.Marker
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import data.LoadingState
import data.loadMarkerDetails.sampleData.almadenVineyardsM2580
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.yield
import network.httpClient
import data.loadMarkers.kBaseHmdbDotOrgUrl
import kMaxMarkerCacheAgeSeconds
import kotlinx.datetime.Clock
import co.touchlab.kermit.Logger as Log

const val kUseFakeData = false

@Composable
fun loadMarkerDetails(marker: Marker, useFakeData: Boolean = false): LoadingState<Marker> {

    var loadingState by remember(marker) {
        if(isMarkerDetailsAlreadyLoadedAndNotExpired(marker)) {
            Log.d("in loadMapMarkerDetails(${marker.id}), isDescriptionLoaded is true & not expired, returning Loaded(mapMarker)")

            return@remember mutableStateOf<LoadingState<Marker>>(LoadingState.Loading)
        }

        mutableStateOf<LoadingState<Marker>>(LoadingState.Loading)
    }

    fun String.calculateMarkerDetailsPageUrl(): String {
        val markerKeyNumber = this.substringAfter ("M").toInt()
        return kBaseHmdbDotOrgUrl + "m.asp?m=$markerKeyNumber"
    }
    // load markerDetailsPageUrl from markerId
    val markerDetailsPageUrl by remember(marker.id) {
        mutableStateOf(marker.id.calculateMarkerDetailsPageUrl())
    }

    LaunchedEffect(markerDetailsPageUrl) {
        if(isMarkerDetailsAlreadyLoadedAndNotExpired(marker)) {
            loadingState = LoadingState.Loaded(marker)
            return@LaunchedEffect
        }

        if(markerDetailsPageUrl.isBlank()) {
            loadingState = LoadingState.Error("MarkerDetailsPageUrl is empty")
            return@LaunchedEffect
        }

        Log.d("in loadMapMarkerDetails(), loading from network: $markerDetailsPageUrl")
        loadingState = LoadingState.Loading
        yield() // Allow UI to render LoadingState.Loading state

        try {
            if (!useFakeData) {
                val response = httpClient.get(markerDetailsPageUrl)
                val markerInfoPageHtml = response.body<String>()

                // parse the page html into a MarkerInfo object
                val parsedMarkerResult = parseMarkerDetailsPageHtml(markerInfoPageHtml)
                parsedMarkerResult.second ?: throw Exception(parsedMarkerResult.first)

                // update the passed-in marker with the parsed info and return it
                val parsedMarkerDetails = parsedMarkerResult.second!!
                loadingState = LoadingState.Loaded(
                    marker.copy(
                        location = marker.location,
                        id = marker.id,
                        title = marker.title,
                        subtitle = marker.subtitle,
                        isDetailsLoaded = true,
                        inscription = parsedMarkerDetails.inscription,
                        englishInscription = parsedMarkerDetails.englishInscription,
                        spanishInscription = parsedMarkerDetails.spanishInscription,
                        erected = parsedMarkerDetails.erected,
                        mainPhotoUrl = parsedMarkerDetails.mainPhotoUrl,
                        markerPhotos = parsedMarkerDetails.markerPhotos,
                        photoCaptions = parsedMarkerDetails.photoCaptions,
                        photoAttributions = parsedMarkerDetails.photoAttributions,
                        credits = parsedMarkerDetails.credits
                    )
                )
            } else {
                 // loadingState = fakeLoadingStateForMarkerDetailsPageHtml(mapMarker)  // for debugging - LEAVE FOR REFERENCE

                val markerDetailsPageHtml = almadenVineyardsM2580()
                val result = parseMarkerDetailsPageHtml(markerDetailsPageHtml)
                loadingState = LoadingState.Loaded(result.second!!)
            }
        } catch (e: Exception) {
            loadingState = LoadingState.Error(e.message ?: e.cause?.message ?: "Loading error")
        }
    }

    return loadingState
}

private fun isMarkerDetailsAlreadyLoadedAndNotExpired(markerToUpdate: Marker) =
    markerToUpdate.isDetailsLoaded &&
        markerToUpdate.lastUpdatedDetailsEpochSeconds + kMaxMarkerCacheAgeSeconds < Clock.System.now().epochSeconds


