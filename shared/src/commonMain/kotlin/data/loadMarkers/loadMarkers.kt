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
import data.util.LoadingState
import data.MarkersRepo
import data.loadMarkers.sampleData.kUseRealNetwork
import data.loadMarkers.sampleData.simpleMarkersPageHtml
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.datetime.Clock
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import presentation.maps.Marker
import data.network.httpClient
import presentation.maps.Location
import presentation.maps.MarkerIdStr
import co.touchlab.kermit.Logger as Log

@Serializable
data class MarkersResult(
    val markerIdToRawMarkerDetailStrings: MutableMap<MarkerIdStr, String> = mutableMapOf(),
    val markerIdToMarker: Map<MarkerIdStr, Marker> = mutableMapOf(),
    val rawMarkerCountFromFirstPageHtmlOfMultiPageResult: Int = 0,
    val isParseMarkersPageFinished: Boolean = false,

    @Contextual
    val loadingState: LoadingState<String> = LoadingState.Finished,
)

/**
 Loads marker basic info from the markers index page html.
 - Can process multiple pages, so there is a state machine to track the current processing page.

 Strategy:
 ```
     1 - Check for cached data in persistent storage (Settings)
       - if not cached, attempt load from network (go to step 2)
       - 1.1 - Check for cached markers & load them if they exist.
       - 1.2 - Check if user is still inside max re-load radius & cache has not expired.
         - 1.2a - If so, return the cached data.
         - 1.2b - If not, attempt load markers from network for this location.
     2 - Initiate Load from network (or fake data)
     3 - Perform Load & Error checking
     4 - Parse the HTML for Basic Info (Marker details are loaded separately, and merged into the same object)
       - 4.1 - Parse the raw page HTML into a list of `MapMarker` objects & metadata about the scraped data
       - 4.2 - Load more pages, if needed (goto step 2)
     5 - Save the MapMarker objects to cache & settings (if they were updated from the network)
     6 - PROCESS COMPLETE
 ```
 Note: This code is goofy looking & hops around a lot... this is because to I'm avoid using
       a ViewModel as this works as a pure composable function. And its all reactive.
       Plus side - there is no ping-pong between files. Its all here.
       I'm Experimenting with a pure compose architecture with no android idiom remnants.
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
): MarkersResult {
    val coroutineScope = rememberCoroutineScope()

    // If the user location is not set, return an empty result
    if (userLocation.latitude == 0.0 && userLocation.longitude == 0.0)
        return MarkersResult()

    var markersLoadingState: LoadingState<String> by remember { mutableStateOf(LoadingState.Loading) }
    LaunchedEffect(markersLoadingState.hashCode()) {
        onUpdateLoadingState(markersLoadingState)
    }

    // Holds the current processing state of the parsed markers
    var processingHtmlPageNum by remember { mutableStateOf(1) }
    var processingMarkersResultState by remember(markersRepo.markers().size) { // checks if repo is cleared or not set yet.
        if(markersRepo.markers().isNotEmpty()) {
            return@remember mutableStateOf(markersRepo.markersResult().copy()) // todo test - does this need to return a copy?
        }

        processingHtmlPageNum = 1
        // return empty result, trigger network load
        return@remember mutableStateOf(
            MarkersResult(
            isParseMarkersPageFinished = false,
            loadingState = LoadingState.Loading,
        ))
    }

    // Keeps internal final state of the MarkersResult load & parse process
    var finalMarkersResultState by remember(userLocation) {
        // Guards
        // - If processing markers (after network load) then return the LAST SAVED result from the MarkersRepo
        if(!processingMarkersResultState.isParseMarkersPageFinished) return@remember mutableStateOf(markersRepo.markersResult())
        // - If processing Markers IS finished and loadingState IS NOT Finished, then return the current processing result.
        if(markersLoadingState !is LoadingState.Finished) {
            Log.w("in loadMarkers: remember-cachedMarkersResultState: markersLoadingState is NOT Finished, even though isParseMarkersPageFinished==true, returning current result, this should not be possible.")
            return@remember mutableStateOf(processingMarkersResultState)
        }

        // Step 1 - Check for a cached result in the Settings
        if (markersRepo.markers().isNotEmpty()) {
            val oldProcessingMarkerResult = processingMarkersResultState

            // Step 1.2 - Check if the user is outside the markers last update radius
            if (appSettings.hasKey(kMarkersLastUpdatedLocation)) {
                val markersLastUpdatedLocation = appSettings.markersLastUpdatedLocation
                val userDistanceFromLastUpdatedLocationMiles =
                    distanceBetween(
                        userLocation.latitude,
                        userLocation.longitude,
                        markersLastUpdatedLocation.latitude,
                        markersLastUpdatedLocation.longitude
                    ) * 1.35 // fudge factor to account for the fact that the user location may have
                             // moved since the last update.

                if (userDistanceFromLastUpdatedLocationMiles > maxReloadDistanceMiles
                    && processingMarkersResultState.isParseMarkersPageFinished
                ) {
                    Log.d("in loadMarkers(): User is outside the max re-load radius, attempting load from network..." )
                    processingMarkersResultState = oldProcessingMarkerResult.copy(
                        isParseMarkersPageFinished = false,
                        loadingState = LoadingState.Loading // trigger loading state // todo remove
                    )
                    processingHtmlPageNum = 1 // reset the page number to 1
                }
            }

            return@remember mutableStateOf(processingMarkersResultState) // return the cached result
        }

        Log.d("in loadMarkers(): No cached markers found. Attempting load from network...")
        processingHtmlPageNum = 1 // reset the page number to 1
        processingMarkersResultState = processingMarkersResultState.copy(
            isParseMarkersPageFinished = false,
            loadingState = LoadingState.Loading
        )
        return@remember mutableStateOf(processingMarkersResultState)// trigger network load
    }

    var markerHtmlPageUrl by remember { mutableStateOf<String?>(null) }
    var shouldUpdateCache by remember { mutableStateOf(false) }
    var networkLoadingState by remember(
            processingMarkersResultState.isParseMarkersPageFinished,
            processingHtmlPageNum
        ) {
        // Guard
        if(processingHtmlPageNum == 0) return@remember mutableStateOf<LoadingState<String>>(
            LoadingState.Finished)

        // Step 2 - Initiate Load a page of raw marker HTML from the network
        if (!processingMarkersResultState.isParseMarkersPageFinished) {
            Log.d("Loading page number $processingHtmlPageNum")
            markersLoadingState = LoadingState.Loading
            shouldUpdateCache = true

            // Define URL to load from network
            markerHtmlPageUrl = "https://www.hmdb.org/results.asp?Search=Coord" +
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

            return@remember mutableStateOf<LoadingState<String>>(LoadingState.Loading)// triggers network load
        }

        // Step 5 - Finished loading pages, now Save result to internal cache
        finalMarkersResultState = processingMarkersResultState.copy(
            markerIdToRawMarkerDetailStrings = mutableMapOf(), // Clear the strings (they are no longer needed)
        )

        // Save the cachedResultState to persistent storage (Settings) (if it was updated from the network)
        if (shouldUpdateCache) {
            coroutineScope.launch {
                appSettings.markersLastUpdateEpochSeconds = Clock.System.now().epochSeconds // todo - move this to individual markerDetails
                appSettings.markersLastUpdatedLocation =
                    userLocation.also { onUpdateMarkersLastUpdatedLocation(userLocation) }

                // Update the markers in the repo
                finalMarkersResultState.markerIdToMarker.forEach {
                    markersRepo.addMarker(it.value)
                }

                Log.d("Saved markers to Settings, total count: ${finalMarkersResultState.markerIdToMarker.size}")
            }
        }

        // 6 PROCESS COMPLETE
        Log.d("Finished loading all pages, total markers= ${finalMarkersResultState.markerIdToMarker.size}")
        markersLoadingState = LoadingState.Finished
        processingHtmlPageNum = 0
        processingMarkersResultState = processingMarkersResultState.copy(
            loadingState = LoadingState.Finished,
            isParseMarkersPageFinished = true
        )

        return@remember mutableStateOf<LoadingState<String>>(LoadingState.Finished)
    }

    // Load the data from the network when `markerHtmlPageUrl` is changed
    LaunchedEffect(markerHtmlPageUrl) {

        // Step 3 - Perform the load from network (or fake data)
        markerHtmlPageUrl?.let { assetUrl ->
            networkLoadingState = LoadingState.Loading // leave for debugging
            processingMarkersResultState = processingMarkersResultState.copy(
                loadingState = LoadingState.Loading
            )
            yield() // allow the UI to update before loading the network data

            networkLoadingState = try {
                Log.d("Loading... $assetUrl")

                // Step 3 - Load the raw HTML from the network (or fake data)
                val rawHtmlString =
                    if(useFakeDataSetId == kUseRealNetwork) {
                        val response = httpClient.get(assetUrl)  // network load
                        val rawHtml: String = response.body()
                        // Log.d("Loaded page successfully, data length: ${rawHtml.length}, coordinates: ${userLocation.latitude}, ${userLocation.longitude}")
                        rawHtml
                    } else {
                        // use FAKE loading from fakeDataSet
                        simpleMarkersPageHtml(processingHtmlPageNum, useFakeDataSetId)
                    }

                // Step 4 - Parse the HTML to extract the marker info
                withContext(Dispatchers.Default) {
                    // Log.d("Before parsing raw HTML, markerInfos.size: ${markersResultState.markerInfos.size}, markerIdToRawMarkerInfoStrings.size: ${markersResultState.markerIdToRawMarkerInfoStrings.size}")

                    // Guard against blank data
                    if (rawHtmlString.isBlank()) {
                        Log.w("Blank data for page $processingHtmlPageNum, location: $userLocation")
                        networkLoadingState =
                            LoadingState.Error("Blank data for page $processingHtmlPageNum, location: $userLocation") // leave for debugging
                        processingMarkersResultState = processingMarkersResultState.copy(
                            isParseMarkersPageFinished = true,
                            loadingState = LoadingState.Error("Blank data for page $processingHtmlPageNum, location: $userLocation")
                        )
                        return@withContext
                    }

                    // 4.1 - Parse the raw page HTML into a list of `MarkerInfo` objects & metadata about the scraped data
                    // Log.d("Parsing HTML.. Current markers in cache (before parsing) count: ${parsedMarkersResultState.markerInfos.size}")
                    val parsedMarkersResult = parseMarkersPageHtml(rawHtmlString)

                    // Check for zero `raw html` marker entries
                    if (parsedMarkersResult.rawMarkerCountFromFirstPageHtmlOfMultiPageResult == 0) {
                        Log.w("No raw html marker entries found for page: $processingHtmlPageNum, location: $userLocation")
                        processingMarkersResultState =
                            processingMarkersResultState.copy(isParseMarkersPageFinished = true)
                        return@withContext
                    }

                    // Merge the new parsed marker data with the previous marker data.
                    // Note: needed to preserve previous results of "loadMarkerDetails" and "isSeen"
                    processingMarkersResultState = processingMarkersResultState.copy(
                        markerIdToRawMarkerDetailStrings = (
                            processingMarkersResultState.markerIdToRawMarkerDetailStrings +
                                parsedMarkersResult.markerIdToRawMarkerDetailStrings
                            ).toMutableMap(),
                        markerIdToMarker = (
                            processingMarkersResultState.markerIdToMarker +
                                // Merge the new parsed marker basic info with the current marker
                                parsedMarkersResult.markerIdToMarker.map { parsedBasicInfoMarker ->
                                    val preserveMarker = processingMarkersResultState.markerIdToMarker[parsedBasicInfoMarker.key]
                                    val mergedMarker = preserveMarker?.copy(
                                        position = parsedBasicInfoMarker.value.position,
                                        title = parsedBasicInfoMarker.value.title,
                                        subtitle = parsedBasicInfoMarker.value.subtitle,
                                        alpha = parsedBasicInfoMarker.value.alpha,
                                    ) ?: parsedBasicInfoMarker.value // if there is no preserveMarker, use the parsedMarkerBasicInfo as the value

                                    parsedBasicInfoMarker.key to mergedMarker
                                }
                            ).toMap(),
                    )
                    if (processingHtmlPageNum == 1) {
                        processingMarkersResultState = processingMarkersResultState.copy(
                            rawMarkerCountFromFirstPageHtmlOfMultiPageResult =
                                parsedMarkersResult.rawMarkerCountFromFirstPageHtmlOfMultiPageResult
                        )
                    }
                    Log.d("Total parsed markers from basic info index page, markerInfos.size: ${processingMarkersResultState.markerIdToMarker.size}, Parsed markerIdToRawMarkerInfoStrings count: ${processingMarkersResultState.markerIdToRawMarkerDetailStrings.size}")

                    // 4.2 - Load more pages, if needed.
                    // - Marker list size comparison is based on the number of `markerIdToRawMarkerInfoStrings`, not the parsed
                    //   `markerInfos` because some of the markers from the page may have been rejected, and we just want
                    //   to know when the raw html is completely loaded, not how many markers were parsed.
                    if (processingMarkersResultState.markerIdToRawMarkerDetailStrings.size
                        < processingMarkersResultState.rawMarkerCountFromFirstPageHtmlOfMultiPageResult
                    ) {
                        Log.d("Loading next page..., markerIdToRawMarkerDetailStrings.size: ${processingMarkersResultState.markerIdToRawMarkerDetailStrings.size}, rawMarkerCountFromFirstHtmlPage: ${processingMarkersResultState.rawMarkerCountFromFirstPageHtmlOfMultiPageResult}")
                        processingHtmlPageNum++  // trigger the next page load
                    } else {
                        //Log.d("Finished processing all pages, total markers: ${markersResultState.markerInfos.size}")
                        processingMarkersResultState = processingMarkersResultState.copy(
                            isParseMarkersPageFinished = true,
                            loadingState = LoadingState.Finished
                        )

                        // Save the final parsed results to the internal cachedMarkersResultState.
                        finalMarkersResultState = processingMarkersResultState.copy()
                    }
                }

                LoadingState.Finished  // loading state of the network load
            } catch (e: Exception) {
                shouldUpdateCache = false
                processingMarkersResultState = processingMarkersResultState.copy(
                    isParseMarkersPageFinished = true,
                    loadingState = LoadingState.Error(e.cause?.message ?: "Loading error - ${e.message}")
                )
                Log.w("Failed to load page: $processingHtmlPageNum, assetUrl: $assetUrl, error: ${e.cause?.message}")
                LoadingState.Error(e.cause?.message ?: "error")  // leave for debugging
            }
        }
    }

    // Displays the loading state
    if (showLoadingState) {
        Box(
            modifier = androidx.compose.ui.Modifier
                .shadow(4.dp, shape = RoundedCornerShape(4.dp))
                .padding(4.dp)
                .fillMaxSize(),
            Alignment.Center
        ) {
            when (val state = networkLoadingState) {
                is LoadingState.Loading -> {
                    Text("Fred's Talking Historical Markers")
                }

                is LoadingState.Loaded<String> -> {
                    Text(
                        fontSize = 18.sp,
                        text = "Loaded: ${processingMarkersResultState.markerIdToRawMarkerDetailStrings.size} / ${processingMarkersResultState.rawMarkerCountFromFirstPageHtmlOfMultiPageResult} entries\n" +
                                "Parsed: ${processingMarkersResultState.markerIdToMarker.size}\n" +
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

    return processingMarkersResultState
}

