package data.loadMarkerDetails

import maps.MapMarker
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
fun loadMapMarkerDetails(mapMarker: MapMarker, useFakeData: Boolean = false): LoadingState<MapMarker> {

    var loadingState by remember(mapMarker) {
        if(isMarkerDetailsAlreadyLoadedAndNotExpired(mapMarker)) {
            Log.d("in loadMapMarkerDetails(${mapMarker.id}), isDescriptionLoaded is true & not expired, returning Loaded(mapMarker)")

            return@remember mutableStateOf<LoadingState<MapMarker>>(LoadingState.Loading)
        }

        mutableStateOf<LoadingState<MapMarker>>(LoadingState.Loading)
    }

    fun String.calculateMarkerDetailsPageUrl(): String {
        val markerKeyNumber = this.substringAfter ("M").toInt()
        return kBaseHmdbDotOrgUrl + "m.asp?m=$markerKeyNumber"
    }
    // load markerDetailsPageUrl from markerId
    val markerDetailsPageUrl by remember(mapMarker.id) {
        mutableStateOf(mapMarker.id.calculateMarkerDetailsPageUrl())
    }

    LaunchedEffect(markerDetailsPageUrl) {
        if(isMarkerDetailsAlreadyLoadedAndNotExpired(mapMarker)) {
            loadingState = LoadingState.Loaded(mapMarker)
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
                    mapMarker.copy(
                        location = mapMarker.location,
                        id = mapMarker.id,
                        title = mapMarker.title,
                        subtitle = mapMarker.subtitle,
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

private fun isMarkerDetailsAlreadyLoadedAndNotExpired(mapMarkerToUpdate: MapMarker) =
    mapMarkerToUpdate.isDetailsLoaded &&
        mapMarkerToUpdate.lastUpdatedEpochSeconds + kMaxMarkerCacheAgeSeconds < Clock.System.now().epochSeconds


