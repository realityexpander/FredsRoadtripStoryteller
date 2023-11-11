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
import kAppName
import data.AppSettings
import data.AppSettings.Companion.kMarkersLastUpdatedLocation
import data.MarkersRepo
import data.loadMarkers.sampleData.kUseRealNetwork
import data.loadMarkers.sampleData.simpleMarkersPageHtml
import data.network.httpClient
import data.util.LoadingState
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.async
import kotlinx.coroutines.withContext
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
       - 1.1 - Check for cached markers, if outside the max reload radius:
         - 1.1a - If has cached markers and inside radius, return the cached data.
         - 1.1b - If no cached markers or outside radius, attempt load markers from network for this location.
     2 - Initiate Load from network. (or fake data.)
     3 - Perform Load & Error checking.
     4 - Parse the Markers Index Page HTML for marker `basic info`.
         (Note: Marker details are loaded separately, and merged into the same Marker object.)
       - 4.1 - Parse the raw page HTML into a list of `Marker` objects & metadata about the scraped data
       - 4.2 - Load more pages, if needed (goto step 2)
     5 - Save the `Marker` objects to internal cached results & to settings, if needed.
     6 - PROCESS COMPLETE.
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
    coroutineScope: CoroutineScope = rememberCoroutineScope()
): LoadMarkersResult {

    // If the user location is not set, return an empty result
    if (userLocation.latitude == 0.0 && userLocation.longitude == 0.0)
        return markersRepo.markersResult()

    var markersLoadingState: LoadingState<String> by remember { mutableStateOf(LoadingState.Loading) }
    LaunchedEffect(markersLoadingState.hashCode()) {
        onUpdateLoadingState(markersLoadingState)
    }

    // Holds the current processing state of the parsed markers
    var processingHtmlPageNum by remember { mutableStateOf(1) }
    var processingLoadMarkersResultState by remember(markersRepo.markers().size) { // checks if repo is cleared or not set yet.
        if(markersRepo.markers().isNotEmpty()) {
            return@remember mutableStateOf(markersRepo.markersResult()) // todo test - does this need to return a copy?
        }

        processingHtmlPageNum = 1
        // return empty result, trigger network load
        return@remember mutableStateOf(
            LoadMarkersResult(
            isParseMarkersPageFinished = false,
            loadingState = LoadingState.Loading,
        ))
    }

    // Keeps internal final state of the MarkersResult load & parse process
    var finalLoadMarkersResultState by remember(userLocation) {
        // Guards
        // - If processing markers (after network load) then return the LAST SAVED result from the MarkersRepo
        if(!processingLoadMarkersResultState.isParseMarkersPageFinished) return@remember mutableStateOf(markersRepo.markersResult())
        // - If processing Markers IS finished and loadingState IS NOT Finished, then return the current processing result.
        if(markersLoadingState !is LoadingState.Finished) {
            Log.w("✳️ in loadMarkers: finalLoadMarkersResultState.remember(userLocat): markersLoadingState is NOT Finished, even though isParseMarkersPageFinished==true, returning current result, this should not be possible.")
            return@remember mutableStateOf(processingLoadMarkersResultState)
        }

        // Step 1 - Check for a cached result in the Settings
        if (markersRepo.markers().isNotEmpty()) {
            val oldProcessingMarkerResult = processingLoadMarkersResultState

            // Step 1.1 - Check if the user is outside the markers last update radius
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
                    && processingLoadMarkersResultState.isParseMarkersPageFinished
                ) {
                    Log.d("✳️ Step 1.1 - in loadMarkers(): User is outside the max re-load radius, attempting load from network..." )
                    processingLoadMarkersResultState = oldProcessingMarkerResult.copy(
                        isParseMarkersPageFinished = false,
                        loadingState = LoadingState.Loading // trigger loading state // todo remove
                    )
                    processingHtmlPageNum = 1 // reset the page number to 1
                }
            }

            return@remember mutableStateOf(processingLoadMarkersResultState) // return the cached result
        }

        Log.d("in loadMarkers(): No cached markers found. Attempting load from network...")
        processingHtmlPageNum = 1 // reset the page number to 1
        processingLoadMarkersResultState = processingLoadMarkersResultState.copy(
            isParseMarkersPageFinished = false,
            loadingState = LoadingState.Loading
        )
        return@remember mutableStateOf(processingLoadMarkersResultState)// trigger network load
    }

    var markerHtmlPageUrl by remember { mutableStateOf<String?>(null) }
    var didUpdateMarkers by remember { mutableStateOf(false) }
    var networkLoadingState by remember(
            processingLoadMarkersResultState.isParseMarkersPageFinished,
            processingHtmlPageNum
        ) {
        // Guard
        if(processingHtmlPageNum == 0) return@remember mutableStateOf<LoadingState<String>>(
            LoadingState.Finished)

        // Step 2 - Initiate Load a page of raw marker HTML from the network
        if (!processingLoadMarkersResultState.isParseMarkersPageFinished) {
            Log.d("✳️ Step 2 - Loading page number $processingHtmlPageNum")
            markersLoadingState = LoadingState.Loading
            didUpdateMarkers = true

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
        finalLoadMarkersResultState = processingLoadMarkersResultState.copy(
            markerIdToRawMarkerDetailStrings = mutableMapOf(), // Clear the strings (they are no longer needed)
        )

        // Update the last updated location
        if(didUpdateMarkers) {
            appSettings.markersLastUpdatedLocation =
                userLocation.also {
                    onUpdateMarkersLastUpdatedLocation(userLocation)
                }
        }

        // 6 PROCESS COMPLETE
        Log.d("✳️ Step 6 - Finished parsing & loading all pages, total markers= ${finalLoadMarkersResultState.markerIdToMarker.size}")
        markersLoadingState = LoadingState.Finished
        processingHtmlPageNum = 0
        processingLoadMarkersResultState = processingLoadMarkersResultState.copy(
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
            processingLoadMarkersResultState = processingLoadMarkersResultState.copy(
                loadingState = LoadingState.Loading
            )
            yield() // allow the UI to update before loading the network data

            networkLoadingState = try {
                Log.d("✳️⬇️ Step 3 - Loading... $assetUrl")

                // Step 3 - Load the raw HTML from the network (or fake data)
                val rawHtmlString =
                    if(useFakeDataSetId == kUseRealNetwork) {
                        coroutineScope.async(Dispatchers.IO) {
                            val response = httpClient.get(assetUrl)  // network load
                            val rawHtml: String = response.body()
                            // Log.d("✳️⬆️ Step 3 - Loaded page successfully, data length: ${rawHtml.length}, coordinates: ${userLocation.latitude}, ${userLocation.longitude}")
                            rawHtml
                        }.await()
                    } else {
                        // use FAKE loading from fakeDataSet
                        // Log.d("✳️⬆️ Step 3 - Using fake data, page: $processingHtmlPageNum, useFakeDataSetId: $useFakeDataSetId")
                        simpleMarkersPageHtml(processingHtmlPageNum, useFakeDataSetId)
                    }

                // Step 4 - Parse the HTML to extract the marker info
                withContext(Dispatchers.IO) {
                    yield()
                    // Log.d("✳️ Step 4 - Before parsing raw HTML, markerInfos.size: ${markersResultState.markerInfos.size}, markerIdToRawMarkerInfoStrings.size: ${markersResultState.markerIdToRawMarkerInfoStrings.size}")

                    // Guard against blank data
                    if (rawHtmlString.isBlank()) {
                        Log.w("✳️ Step 4 - Blank data for page $processingHtmlPageNum, location: $userLocation")
                        networkLoadingState =
                            LoadingState.Error("Blank data for page $processingHtmlPageNum, location: $userLocation") // leave for debugging
                        processingLoadMarkersResultState = processingLoadMarkersResultState.copy(
                            isParseMarkersPageFinished = true,
                            loadingState = LoadingState.Error("Blank data for page $processingHtmlPageNum, location: $userLocation")
                        )
                        return@withContext
                    }

                    // 4.1 - Parse the raw page HTML into a list of `MarkerInfo` objects & metadata about the scraped data
                    // Log.d("Parsing HTML.. Current markers in cache (before parsing) count: ${parsedMarkersResultState.markerInfos.size}")
                    val parseStartTime = Clock.System.now()
                    val parsedMarkersResult = parseMarkersPageHtml(rawHtmlString)
                    Log.d("✳️🛑 Step 4.1 - Parsed HTML in ${Clock.System.now() - parseStartTime}")
                    yield()

                    // Check for zero `raw html` marker entries
                    if (parsedMarkersResult.rawMarkerCountFromFirstPageHtmlOfMultiPageResult == 0) {
                        Log.w("✳️ Step 4.1 - No raw html marker entries found for page: $processingHtmlPageNum, location: $userLocation")
                        processingLoadMarkersResultState =
                            processingLoadMarkersResultState.copy(isParseMarkersPageFinished = true)
                        return@withContext
                    }
                    // Merge the new parsed marker data with the previous marker data.
                    // Note: needed to preserve previous results of "loadMarkerDetails" and "isSeen"
                    processingLoadMarkersResultState = processingLoadMarkersResultState.copy(
                        markerIdToRawMarkerDetailStrings = (
                            processingLoadMarkersResultState.markerIdToRawMarkerDetailStrings +
                                parsedMarkersResult.markerIdToRawMarkerDetailStrings
                            ).toMutableMap(),
                        markerIdToMarker = (
                            processingLoadMarkersResultState.markerIdToMarker +
                                // Merge the new parsed marker basic info with the current marker
                                parsedMarkersResult.markerIdToMarker.map { parsedBasicInfoMarker ->
                                    val preserveMarker =
                                        processingLoadMarkersResultState.markerIdToMarker[parsedBasicInfoMarker.key]
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
                        processingLoadMarkersResultState = processingLoadMarkersResultState.copy(
                            rawMarkerCountFromFirstPageHtmlOfMultiPageResult =
                                parsedMarkersResult.rawMarkerCountFromFirstPageHtmlOfMultiPageResult
                        )
                    }
                    Log.d("✳️ Step 4.1 - Total parsed markers from basic info index page, markerInfos.size: ${processingLoadMarkersResultState.markerIdToMarker.size}, Parsed markerIdToRawMarkerInfoStrings count: ${processingLoadMarkersResultState.markerIdToRawMarkerDetailStrings.size}")

                    // 4.2 - Load more pages, if needed.
                    // - Marker list size comparison is based on the number of `markerIdToRawMarkerInfoStrings`, not the parsed
                    //   `markerInfos` because some of the markers from the page may have been rejected, and we just want
                    //   to know when the raw html is completely loaded, not how many markers were parsed.
                    if (processingLoadMarkersResultState.markerIdToRawMarkerDetailStrings.size
                        < processingLoadMarkersResultState.rawMarkerCountFromFirstPageHtmlOfMultiPageResult
                    ) {
                        Log.d("✳️ Step 4.2 - Loading next page..., markerIdToRawMarkerDetailStrings.size: ${processingLoadMarkersResultState.markerIdToRawMarkerDetailStrings.size}, rawMarkerCountFromFirstHtmlPage: ${processingLoadMarkersResultState.rawMarkerCountFromFirstPageHtmlOfMultiPageResult}")
                        processingHtmlPageNum++  // trigger the next page load
                    } else {
                        //Log.d("Finished processing all pages, total markers: ${markersResultState.markerInfos.size}")
                        processingLoadMarkersResultState = processingLoadMarkersResultState.copy(
                            isParseMarkersPageFinished = true,
                            loadingState = LoadingState.Finished
                        )

                        // Save the final parsed results to the internal cachedMarkersResultState.
                        finalLoadMarkersResultState = processingLoadMarkersResultState.copy()
                    }
                }

                LoadingState.Finished  // loading state of the network load
            } catch (e: Exception) {
                didUpdateMarkers = false
                processingLoadMarkersResultState = processingLoadMarkersResultState.copy(
                    isParseMarkersPageFinished = true,
                    loadingState = LoadingState.Error(e.cause?.message ?: "Loading error - ${e.message}")
                )
                Log.w("✳️ Step 3 - Failed to load page: $processingHtmlPageNum, assetUrl: $assetUrl, error: ${e.cause?.message}")
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
                    Text(kAppName)
                }

                is LoadingState.Loaded<String> -> {
                    Text(
                        fontSize = 18.sp,
                        text = "Loaded: ${processingLoadMarkersResultState.markerIdToRawMarkerDetailStrings.size} / ${processingLoadMarkersResultState.rawMarkerCountFromFirstPageHtmlOfMultiPageResult} entries\n" +
                                "Parsed: ${processingLoadMarkersResultState.markerIdToMarker.size}\n" +
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

    return processingLoadMarkersResultState
}

