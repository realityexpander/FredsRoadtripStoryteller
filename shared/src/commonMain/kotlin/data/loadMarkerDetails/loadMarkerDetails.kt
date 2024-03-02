package data.loadMarkerDetails

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import data.loadMarkerDetails.sampleData.almadenVineyardsM2580MarkerDetailsHtml
import data.loadMarkers.kBaseHmdbDotOrgUrl
import data.network.httpClient
import data.util.LoadingState
import io.ktor.client.call.body
import io.ktor.client.request.get
import kMaxMarkerDetailsAgeSeconds
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.yield
import kotlinx.datetime.Clock
import presentation.maps.Marker
import co.touchlab.kermit.Logger as Log

const val kUseFakeData = false

fun String.calculateMarkerDetailsPageUrl(): String {
    val markerKeyNumber = this.substringAfter ("M").toInt()
    return kBaseHmdbDotOrgUrl + "m.asp?m=$markerKeyNumber"
}

@Composable
fun loadMarkerDetails(
    marker: Marker,
    useFakeData: Boolean = false,
    onUpdateMarkerDetails: (marker: Marker) -> Unit
): LoadingState<Marker> {
    var loadingState: LoadingState<Marker> by remember(marker) {
        if(isMarkerDetailsLoadedAndNotExpired(marker)) {
            // Log.d("in loadMarkerDetails(${marker.id}), isDetailsLoaded is true & not expired, returning Loaded(marker)")
            return@remember mutableStateOf(LoadingState.Loaded(marker))
        }

        mutableStateOf(LoadingState.Loading)
    }

    // load `markerDetailsPageUrl` from `markerId`
    val markerDetailsPageUrl by remember(marker.id) {
        mutableStateOf(marker.id.calculateMarkerDetailsPageUrl())
    }

    LaunchedEffect(markerDetailsPageUrl) {
        if(isMarkerDetailsLoadedAndNotExpired(marker)) {
            loadingState = LoadingState.Loaded(marker)
            return@LaunchedEffect
        }
        if(markerDetailsPageUrl.isBlank()) {
            loadingState = LoadingState.Error("MarkerDetailsPageUrl is empty")
            return@LaunchedEffect
        }

        Log.d("in loadMarkerDetails(${marker.id}), loading from network: $markerDetailsPageUrl")
        loadingState = LoadingState.Loading
        yield() // Allow UI to render LoadingState.Loading state

        try {
            if (!useFakeData) {
                val response = httpClient.get(markerDetailsPageUrl)
                val markerDetailsPageHtml = response.body<String>()

                // parse the page html into a MarkerInfo object
                val (errorMessageStr, parsedDetailsMarker) =
                    parseMarkerDetailsPageHtml(markerDetailsPageHtml)
                errorMessageStr?.run { throw Exception(errorMessageStr) }
                parsedDetailsMarker ?: throw Exception("parsedDetailsMarker is null, even though errorMessageStr is null")

                // update the passed-in marker with the parsed details and return it
                loadingState = LoadingState.Loaded(
                    parsedDetailsMarker.copy(
                        position = marker.position,
                        id = marker.id,
                        title = marker.title,
                        subtitle = marker.subtitle,
                        alpha = marker.alpha,
                        isSeen = marker.isSeen,
                        isDetailsLoaded = true,
                    )
                )

                // Update the marker in the repository.
                onUpdateMarkerDetails(parsedDetailsMarker)
            } else {
                 // loadingState = fakeLoadingStateForMarkerDetailsPageHtml(mapMarker)  // for debugging - LEAVE FOR REFERENCE

                val markerDetailsPageHtml = almadenVineyardsM2580MarkerDetailsHtml()
                val result = parseMarkerDetailsPageHtml(markerDetailsPageHtml)
                loadingState = LoadingState.Loaded(result.second!!)
            }
        } catch(e: CancellationException) {
            throw e
        } catch (e: Exception) {
            e.printStackTrace()
            loadingState = LoadingState.Error(e.message ?: e.cause?.message ?: "Unknown Loading error for marker ${marker.id}")
        }
    }

    return loadingState
}

private fun isMarkerDetailsLoadedAndNotExpired(marker: Marker) =
    marker.isDetailsLoaded
        && marker.lastUpdatedDetailsEpochSeconds +
            kMaxMarkerDetailsAgeSeconds < Clock.System.now().epochSeconds


