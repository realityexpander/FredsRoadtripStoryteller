package data.loadMarkers

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import data.AppSettings
import data.AppSettings.Companion.kMarkersLastUpdatedLocation
import data.MarkersRepo
import data.loadMarkers.sampleData.generateTestPageHtml
import data.loadMarkers.sampleData.kUseRealNetwork
import data.network.httpClient
import data.util.LoadingState
import data.util.toInstant
import io.ktor.client.call.body
import io.ktor.client.request.get
import kAppNameStr
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlinx.datetime.Clock
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import presentation.maps.Location
import presentation.maps.Marker
import presentation.maps.MarkerIdStr
import co.touchlab.kermit.Logger as Log

@Serializable
data class LoadMarkersResult(
    val markerIdToRawMarkerDetailStrings: MutableMap<MarkerIdStr, String> = mutableMapOf(),
    val markerIdToMarkerMap: Map<MarkerIdStr, Marker> = mutableMapOf(),
    val rawMarkerCountFromFirstPageHtmlOfMultiPageResult: Int = 0,
    var isParseMarkersPageFinished: Boolean = false,

    @Contextual
    val loadingState: LoadingState<String> = LoadingState.Finished,
)

/**
 Loads marker basic info from the markers index page html.
 - Can process multiple pages, so there is a state machine to track the current processing page.

 Strategy:
 ```
      1. Check for cached markers
      2. Check if user location is outside reload max radius
      3. Load markers from network (or fake data)
         - Load multiple pages if needed
      4. Parse markers - Parse the raw page HTML into a list of `Marker` objects & metadata about the scraped data
      5. Save markers to repo
      6. Save "last updated at" location
 ```
**/
@Composable
fun loadMarkers(
    appSettings: AppSettings,
    markersRepo: MarkersRepo,

    // Map Marker loading parameters
    userLocation: Location = Location(37.422160, -122.084270),
    maxReloadDistanceMiles: Int = 10,
    onUpdateMarkersLastUpdatedLocation: (Location) -> Unit = {},
    onUpdateLoadingState: (LoadingState<String>) -> Unit = {},

    // Debugging/Testing parameters
    showLoadingState: Boolean = false, // 0 = use real data, >1 = use fake data (1 = 3 pages around googleplex, 2 = 1 page around tepoztlan)
    useFakeDataSetId: Int = 0,
    coroutineScope: CoroutineScope = rememberCoroutineScope { Dispatchers.IO }
) {
    var parsedMarkersResult by remember { mutableStateOf(LoadMarkersResult()) }
    var isLoadMarkersCurrentlyProcessing by remember { mutableStateOf(false) }
    var debugLoadingState by remember { mutableStateOf<LoadingState<String>>(LoadingState.Loading) }

    fun updateLoadingState(newState: LoadingState<String>) {
        debugLoadingState = newState
        onUpdateLoadingState(newState)
    }

    LaunchedEffect(userLocation) {

        // Guard
        if(isLoadMarkersCurrentlyProcessing) return@LaunchedEffect

        // 1. Check for cached markers
        if(markersRepo.markers().isEmpty()
            ||
            // 2. Check for cached markers within max reload radius
            (markersRepo.markers().isNotEmpty()
                && isLocationOutsideReloadRadius(appSettings, userLocation, maxReloadDistanceMiles)
            )
        ) {
            isLoadMarkersCurrentlyProcessing = true

            coroutineScope.launch(Dispatchers.IO) {
                var isProcessingPagesFinished = false
                var processingHtmlPageNum = 1
                updateLoadingState(LoadingState.Loading)

                do {
                    val markerHtmlPageUrl = "https://www.hmdb.org/results.asp?Search=Coord" +
                            // "&Latitude=37.422160" +
                            // "&Longitude=-122.084270" +  // Sunnyvale, CA
                            // "&Latitude=37.2391" +
                            // "&Longitude=-121.8947" + // Almaden Vineyards, CA  - Single Item Page
                            // "&Miles=10" +
                            "&Latitude=" + userLocation.latitude +
                            "&Longitude=" + userLocation.longitude +
                            "&Miles=" + maxReloadDistanceMiles +
                            "&MilesType=1&HistMark=Y&WarMem=Y&FilterNOT=&FilterTown=&FilterCounty=&FilterState=&FilterCountry=&FilterCategory=0" +
                            "&Page=$processingHtmlPageNum"

                    yield() // allow the UI to update before loading the network data
                    try {
                        // 3. Load markers from network (or fake data)
                        val rawHtml =
                            if(useFakeDataSetId == kUseRealNetwork) {
                                Log.d("📍⬇️ Loading... $markerHtmlPageUrl")
                                val response = httpClient.get(markerHtmlPageUrl)  // network load

                                Log.d("📍⬆️ Loaded page successfully, time to load: ${Clock.System.now().minus(response.requestTime.toInstant())}")
                                response.body()
                            } else {
                                // use FAKE loading from fakeDataSet
                                // Log.d("📍⬆️ Step 3a - Using fake data, page: $processingHtmlPageNum, useFakeDataSetId: $useFakeDataSetId")
                                generateTestPageHtml(processingHtmlPageNum, useFakeDataSetId)
                            }
                        println("📍⬆️ Loaded page successfully, data length: ${rawHtml.length}")
                        updateLoadingState(LoadingState.Loaded(rawHtml))

                        // 4. Parse markers
                        parsedMarkersResult = parseMarkersPageHtml(rawHtml)
                        yield() // allow the UI to update before loading the network data
                        Log.d("📍⬆️ Parsed HTML, markerInfos.size: ${parsedMarkersResult.markerIdToMarkerMap.size}, Parsed markerIdToRawMarkerInfoStrings count: ${parsedMarkersResult.markerIdToRawMarkerDetailStrings.size}")

                        // 5. Save markers to repo
                        parsedMarkersResult.markerIdToMarkerMap.values.forEach { marker ->
                            markersRepo.addMarker(marker)
                        }
                        //Log.d("📍⬆️ Saved markers to repo, total markers: ${markersRepo.markers().size}")

                        // Check for more pages
                        if (parsedMarkersResult.rawMarkerCountFromFirstPageHtmlOfMultiPageResult
                            > parsedMarkersResult.markerIdToMarkerMap.size
                        ) {
                            processingHtmlPageNum++
                            updateLoadingState(LoadingState.Loading)
                        } else {
                            isProcessingPagesFinished = true
                        }

                        yield() // allow the debug UI to update before loading the network data
                    } catch (e: Exception) {
                        if (e is CancellationException) throw e // todo find better way from Tasky - isCancelled()?

                        e.printStackTrace()
                        updateLoadingState(
                            LoadingState.Error(
                                e.cause?.message ?: "Loading error - ${e.message}"
                            )
                        )
                        isLoadMarkersCurrentlyProcessing = false
                        return@launch
                    }
                } while (!isProcessingPagesFinished)
                updateLoadingState(LoadingState.Finished)
                yield()

                // 6. Update "last updated at" location
                coroutineScope.launch {
                    appSettings.markersLastUpdatedLocation =
                        userLocation.also {
                            onUpdateMarkersLastUpdatedLocation(userLocation)
                        }
                }

                isLoadMarkersCurrentlyProcessing = false
            }
        }
    }

    // Displays the loading state - for debugging
    if (showLoadingState) {
        Box(
            modifier = androidx.compose.ui.Modifier
                .shadow(4.dp, shape = RoundedCornerShape(4.dp))
                .padding(4.dp)
                .fillMaxSize(),
            Alignment.Center
        ) {
            when (val state = debugLoadingState) {
                is LoadingState.Loading -> {
                    Text("$kAppNameStr Loading...")
                }

                is LoadingState.Loaded<String> -> {
                    Text(
                        fontSize = 18.sp,
                        text = "Loaded: ${parsedMarkersResult.markerIdToRawMarkerDetailStrings.size} / ${parsedMarkersResult.rawMarkerCountFromFirstPageHtmlOfMultiPageResult} entries\n" +
                                "Parsed: ${parsedMarkersResult.markerIdToMarkerMap.size}\n" +
                                "Data size: ${state.data.length} chars"
                    )
                }

                is LoadingState.Error -> {
                    Text("Error: ${state.errorMessage}")
                }

                else -> {
                    Text("Finished loading")
                }
            }
        }
    }
}

private fun isLocationOutsideReloadRadius(
    appSettings: AppSettings,
    location: Location,
    maxReloadDistanceMiles: Int
): Boolean {
    // Step 1.1 - Check if the user is outside the markers last update radius
    if (appSettings.hasKey(kMarkersLastUpdatedLocation)) {
        val markersLastUpdatedLocation = appSettings.markersLastUpdatedLocation
        val userDistanceFromLastUpdatedLocationMiles =
            distanceBetweenInMiles(
                location.latitude,
                location.longitude,
                markersLastUpdatedLocation.latitude,
                markersLastUpdatedLocation.longitude
            ) * .90 // fudge factor to account for the fact that the user location may have
                    // moved since the last update.

        if (userDistanceFromLastUpdatedLocationMiles > maxReloadDistanceMiles) {
            return true
        }
    }

    return false
}
















































