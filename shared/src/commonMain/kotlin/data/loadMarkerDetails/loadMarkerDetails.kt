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
fun loadMapMarkerDetails(mapMarkerToUpdate: MapMarker, useFakeData: Boolean = false): LoadingState<MapMarker> {

    var loadingState by remember(mapMarkerToUpdate) {
        if(isMarkerIsAlreadyCachedAndNotExpired(mapMarkerToUpdate)) {
            // Log.d("mapMarker.isDescriptionLoaded is true, returning Loaded(mapMarker)")
            LoadingState.Loaded(mapMarkerToUpdate)
        }

        mutableStateOf<LoadingState<MapMarker>>(LoadingState.Loading)
    }

    fun String.calculateMarkerDetailsPageUrl(): String {
        val markerKeyNumber = this.substringAfter ("M").toInt()
        return kBaseHmdbDotOrgUrl + "m.asp?m=$markerKeyNumber"
    }
    // load markerDetailsPageUrl from markerId
    val markerDetailsPageUrl by remember(mapMarkerToUpdate.id) {
        mutableStateOf(mapMarkerToUpdate.id.calculateMarkerDetailsPageUrl())
    }

    LaunchedEffect(markerDetailsPageUrl) {
        if(isMarkerIsAlreadyCachedAndNotExpired(mapMarkerToUpdate)) {
            loadingState = LoadingState.Loaded(mapMarkerToUpdate)
            return@LaunchedEffect
        }

        if(markerDetailsPageUrl == "") {
            loadingState = LoadingState.Error("MarkerDetailsPageUrl is empty")
            return@LaunchedEffect
        }

        Log.d("loading loadMapMarkerDetails from network: $markerDetailsPageUrl")
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
                    mapMarkerToUpdate.copy(
                        location = mapMarkerToUpdate.location,
                        id = mapMarkerToUpdate.id,
                        title = mapMarkerToUpdate.title,
                        subtitle = mapMarkerToUpdate.subtitle,
                        isDescriptionLoaded = true,
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
                // loadingState = fakeLoadingStateForParseMarkerInfoPageHtml(mapMarker)  // LEAVE FOR REFERENCE

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

private fun isMarkerIsAlreadyCachedAndNotExpired(mapMarkerToUpdate: MapMarker) =
    mapMarkerToUpdate.isDescriptionLoaded &&
        mapMarkerToUpdate.lastUpdatedEpochSeconds + kMaxMarkerCacheAgeSeconds < Clock.System.now().epochSeconds


