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

const val kUseFakeData = false

@Composable
fun loadMapMarkerDetails(mapMarker: MapMarker, useFakeData: Boolean = false): LoadingState<MapMarker> {
    var loadingState by remember {
        mutableStateOf<LoadingState<MapMarker>>(LoadingState.Loading)
    }

    fun String.calculateMarkerInfoPageUrl(): String {
        val markerKeyNumber = this.substringAfter ("M").toInt()
        return kBaseHmdbDotOrgUrl + "m.asp?m=$markerKeyNumber"
    }
    val markerInfoPageUrl by remember(mapMarker.key) {
        mutableStateOf(mapMarker.key.calculateMarkerInfoPageUrl())
    }

    LaunchedEffect(markerInfoPageUrl) {
        println("loadMapMarkerInfo $markerInfoPageUrl")
        if(markerInfoPageUrl == "") {
            //loadingState = LoadingState.Error("MarkerInfoPageUrl is empty")
            return@LaunchedEffect
        }

        loadingState = LoadingState.Loading
        yield() // Allow UI to render LoadingState.Loading state

        try {
            if (!useFakeData) {
                val response = httpClient.get(markerInfoPageUrl)
                val markerInfoPageHtml = response.body<String>()

                // parse the page html into a MarkerInfo object
                val parsedMarkerResult = parseMarkerInfoPageHtml(markerInfoPageHtml)
                parsedMarkerResult.second ?: throw Exception(parsedMarkerResult.first)

                // update the passed-in marker with the parsed info and return it
                val parsedMarkerInfo = parsedMarkerResult.second!!
                println("parsedMarkerInfo: $parsedMarkerInfo")

                loadingState = LoadingState.Loaded(
                    mapMarker.copy(
                        location = mapMarker.location,
                        key = mapMarker.key,
                        title = mapMarker.title,
                        subtitle = mapMarker.subtitle,
                        isDescriptionLoaded = true,
                        inscription = parsedMarkerInfo.inscription,
                        englishInscription = parsedMarkerInfo.englishInscription,
                        spanishInscription = parsedMarkerInfo.spanishInscription,
                        erected = parsedMarkerInfo.erected,
                        mainPhotoUrl = parsedMarkerInfo.mainPhotoUrl,
                        markerPhotos = parsedMarkerInfo.markerPhotos,
                        photoCaptions = parsedMarkerInfo.photoCaptions,
                        photoAttributions = parsedMarkerInfo.photoAttributions,
                        credits = parsedMarkerInfo.credits
                    )
                )
            } else {
                // loadingState = fakeLoadingStateForParseMarkerInfoPageHtml(mapMarker)

                val markerInfoPageHtml = almadenVineyardsM2580()
                val result = parseMarkerInfoPageHtml(markerInfoPageHtml)
                loadingState = LoadingState.Loaded(result.second!!)
            }
        } catch (e: Exception) {
            loadingState = LoadingState.Error(e.message ?: e.cause?.message ?: "Loading error")
        }
    }

    return loadingState
}

