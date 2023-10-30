package data.loadMarkers

import Location
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
import data.LoadingState
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
import maps.Marker
import maps.MarkerIdStr
import network.httpClient
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
 Loads marker info from the markers page html.
 - Can process multiple pages, so there is a state machine to track the current processing page.


 Strategy:
 ```
     1 - Check for cached data
       - if not cached, attempt load from network (go to step 2)
       - 1.1 - Check for cached markers & load them if they exist.
       - 1.2 - Check if user is still inside max re-load radius & cache has not expired.
         - 1.2a - If so, return the cached data.
         - 1.2b - If not, attempt load markers from network for this location.
     2 - Initiate Load from network
     3 - Perform Load & Error checking
     4 - Parse the HTML
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
    settings: AppSettings,

    // Map Marker loading parameters
    userLocation: Location = Location(37.422160, -122.084270),
    maxReloadDistanceMiles: Int = 10,
    onSetMarkersLastUpdatedLocation: (Location) -> Unit = {},
    onUpdateLoadingState: (LoadingState<String>) -> Unit = {},

    // Debugging/Testing parameters
    showLoadingState: Boolean = false, // 0 = use real data, >1 = use fake data (1 = 3 pages around googleplex, 2 = 1 page around tepoztlan)
    useFakeDataSetId: Int = 0,
): MarkersResult {

    // If the user location is not set, return an empty result
    if (userLocation.latitude == 0.0 && userLocation.longitude == 0.0)
        return MarkersResult()

    val coroutineScope = rememberCoroutineScope()
    var markersLoadingState: LoadingState<String> by remember { mutableStateOf(LoadingState.Loading) }
    LaunchedEffect(markersLoadingState.hashCode()) {
        onUpdateLoadingState(markersLoadingState)
    }

    // Holds the current processing state of the parsed markers
    var curProcessingHtmlPageNum by remember { mutableStateOf(1) }
    var markersResultState by remember(settings.markersResult.markerIdToMarker.size) { // checks if settings is cleared or not set yet.
        if(settings.markersResult.markerIdToMarker.isNotEmpty()) {
            return@remember mutableStateOf(settings.markersResult) // return the cached result
        }

        // Log.d { "in loadMarkers(): markersResultState: No cached markers found. Attempting load from network..." }
        curProcessingHtmlPageNum = 1
        mutableStateOf(
            MarkersResult(
            loadingState = LoadingState.Loading,
            isParseMarkersPageFinished = false,
        ))// return empty result, trigger network load
    }

    // Keeps internal final state of the MarkersResult load & parse process
    var cachedMarkersResultState by remember(userLocation) {
        // Guards - If currently parsing markers (after network load)
        //          return the PREVIOUS result until its finished.
        if(!markersResultState.isParseMarkersPageFinished) return@remember mutableStateOf(markersResultState)
        //        - If parsing is finished and loading is NOT complete (idle), return the current result. (should never happen)
        if(markersLoadingState !is LoadingState.Finished) {
            Log.w("in loadMarkers: remember-cachedMarkersResultState: markersLoadingState is NOT Finished, even though isParseMarkersPageFinished==true, returning current result")
            return@remember mutableStateOf(markersResultState)
        }

        // Step 1 - Check for a cached result in the Settings
        if (settings.markersResult.markerIdToMarker.isNotEmpty()) {
            val cachedMarkersResult =
                settings.markersResult
                    .copy(isParseMarkersPageFinished = true) // ensure the cached result is marked as finished

            // Log.d("Found cached markers in Settings, count: ${cachedMarkersResult.markerInfos.size}")
            markersResultState = cachedMarkersResult.copy(isParseMarkersPageFinished = true)

//            // Step 1.1 - Check if the cache is expired // todo replace this with a per-marker details loaded expiry
//            // todo make a function
//            if(settings.hasKey(kSettingMarkersLastUpdateEpochSeconds)) {
//                val cacheLastUpdatedEpochSeconds = settings.markersLastUpdateEpochSeconds()
//                // Log.d("Days since last cache update: $(Clock.System.now().epochSeconds - cacheLastUpdatedEpochSeconds) / (60 * 60 * 24)")
//
//                // if(true) { // test cache expiry
//                if(Clock.System.now().epochSeconds > cacheLastUpdatedEpochSeconds + kMaxMarkerCacheAgeSeconds) {
//                    Log.d("Cached markers are expired, dumping entire cache, attempting load from network..." ) // todo make this more sophisticated
//
//                    // return current cached result, and also trigger network load, which will refresh the cache.
//                    markersResultState = MarkersResult(isParseMarkersPageFinished = false)
//
//                    // Clear the cache in the settings
//                    coroutineScope.launch {
//                        settings.remove(kSettingMarkersResult)
//                        // Update the cache expiry time
//                        settings.putLong(
//                            kSettingMarkersLastUpdateEpochSeconds,
//                            Clock.System.now().epochSeconds
//                        )
//                        Log.d("Cache expired, cleared markers from Settings")
//                    }
//                }
//            }

            // Step 1.2 - Check if the user is outside the markers last update radius
            if (settings.hasKey(kMarkersLastUpdatedLocation)) {
                val markersLastUpdatedLocation = settings.markersLastUpdatedLocation
                val userDistanceFromLastUpdatedLocationMiles =
                    distanceBetween(
                        userLocation.latitude,
                        userLocation.longitude,
                        markersLastUpdatedLocation.latitude,
                        markersLastUpdatedLocation.longitude
                    ) * 1.35 // fudge factor to account for the fact that the user location may have
                             // moved since the last update.

                if (userDistanceFromLastUpdatedLocationMiles > maxReloadDistanceMiles &&
                    markersResultState.isParseMarkersPageFinished
                ) {
                    // Log.d("User is outside the max re-load radius, attempting load from network..." )
                    markersResultState = cachedMarkersResult.copy(isParseMarkersPageFinished = false)
                    curProcessingHtmlPageNum = 1 // reset the page number to 1
                }
            }

            return@remember mutableStateOf(markersResultState) // return the cached result
        }

        Log.d { "No cached markers found. Attempting load from network..." }
        markersResultState = markersResultState.copy(
            isParseMarkersPageFinished = false,
            loadingState = LoadingState.Loading
        )
        curProcessingHtmlPageNum = 1
        mutableStateOf(MarkersResult())// return empty result, trigger network load
    }

    var markerHtmlPageUrl by remember { mutableStateOf<String?>(null) }
    var shouldUpdateCache by remember { mutableStateOf(false) }
    var networkLoadingState by remember(markersResultState.isParseMarkersPageFinished, curProcessingHtmlPageNum) {
        // Guard
        if(curProcessingHtmlPageNum == 0) return@remember mutableStateOf<LoadingState<String>>(LoadingState.Finished)

        // Step 2 - Initiate Load a page of raw marker HTML from the network
        if (!markersResultState.isParseMarkersPageFinished) {
            Log.d("Loading page number $curProcessingHtmlPageNum")
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
                "&Page=$curProcessingHtmlPageNum"

            return@remember mutableStateOf<LoadingState<String>>(LoadingState.Loading)// triggers network load
        }

        // Step 5 - Finished loading pages, now Save result to internal cache
        cachedMarkersResultState = markersResultState.copy(
            markerIdToRawMarkerDetailStrings = mutableMapOf(), // Clear the strings (they are no longer needed)
        )

        // Save the cachedResultState to persistent storage (Settings) (if it was updated from the network)
        if (shouldUpdateCache) {
            println("Saving markers to Settings, total count: ${cachedMarkersResultState.markerIdToMarker.size}")
            coroutineScope.launch {
                settings.markersResult = cachedMarkersResultState
                settings.markersLastUpdateEpochSeconds = Clock.System.now().epochSeconds
                settings.markersLastUpdatedLocation = userLocation
                    .also {
                        onSetMarkersLastUpdatedLocation(userLocation)
                    }
                // Log.d("Saved markers to Settings, total count: ${cachedMarkersResultState.markerIdToMapMarker.size}")
            }
        }

        // 6 PROCESS COMPLETE
        Log.d("Finished loading all pages, total markers:${cachedMarkersResultState.markerIdToMarker.size}")
        markersLoadingState = LoadingState.Finished
        curProcessingHtmlPageNum = 0
        mutableStateOf<LoadingState<String>>(LoadingState.Finished)
    }

    // Load the data from the network when `markerHtmlPageUrl` is changed
    LaunchedEffect(markerHtmlPageUrl) {

        // Step 3 - Perform the load from network (or fake data)
        markerHtmlPageUrl?.let { assetUrl ->
            networkLoadingState = LoadingState.Loading // leave for debugging
            markersResultState = markersResultState.copy(
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
                        simpleMarkersPageHtml(curProcessingHtmlPageNum, useFakeDataSetId)
                    }

                // Step 4 - Parse the HTML to extract the marker info
                withContext(Dispatchers.Default) {
                    // Log.d("Before parsing raw HTML, markerInfos.size: ${markersResultState.markerInfos.size}, markerIdToRawMarkerInfoStrings.size: ${markersResultState.markerIdToRawMarkerInfoStrings.size}")

                    // Guard against blank data
                    if (rawHtmlString.isBlank()) {
                        Log.w("Blank data for page $curProcessingHtmlPageNum, location: $userLocation")
                        networkLoadingState =
                            LoadingState.Error("Blank data for page $curProcessingHtmlPageNum, location: $userLocation") // leave for debugging
                        markersResultState = markersResultState.copy(
                            isParseMarkersPageFinished = true,
                            loadingState = LoadingState.Error("Blank data for page $curProcessingHtmlPageNum, location: $userLocation")
                        )
                        return@withContext
                    }

                    // 4.1 - Parse the raw page HTML into a list of `MarkerInfo` objects & metadata about the scraped data
                    // Log.d("Parsing HTML.. Current markers in cache (before parsing) count: ${parsedMarkersResultState.markerInfos.size}")
                    val parsedMarkersResult = parseMarkersPageHtml(rawHtmlString)

                    // Check for zero `raw html` marker entries
                    if (parsedMarkersResult.rawMarkerCountFromFirstPageHtmlOfMultiPageResult == 0) {
                        Log.w("No raw html marker entries found for page: $curProcessingHtmlPageNum, location: $userLocation")
                        markersResultState = markersResultState.copy(isParseMarkersPageFinished = true)
                        return@withContext
                    }

                    // Merge the new parsed marker data with the previous marker data.
                    // Note: needed to preserve previous results of "loadMarkerDetails" and "isSeen"
                    markersResultState = markersResultState.copy(
                        markerIdToRawMarkerDetailStrings = (
                            markersResultState.markerIdToRawMarkerDetailStrings +
                                parsedMarkersResult.markerIdToRawMarkerDetailStrings
                            ).toMutableMap(),
                        markerIdToMarker = (
                            markersResultState.markerIdToMarker +
                                // Merge the new parsed marker basic info with the current markers.
                                parsedMarkersResult.markerIdToMarker.map { parsedBasicInfoMarker ->
                                    val preserveMarker = markersResultState.markerIdToMarker[parsedBasicInfoMarker.key]
                                    val isPreserveMarkerDetailsLoaded = preserveMarker?.isDetailsLoaded ?: false

                                    // Start with parsed marker (basic info) as base for merge
                                    var mergedMarker = parsedBasicInfoMarker.value

                                    // TODO Make this a REPO function
                                    // Preserve the `MarkerDetails`, and update the basic info from the parsedMarker
                                    if(isPreserveMarkerDetailsLoaded) {
                                        // Use the `withDataToBePreservedMarker` as base
                                        // and copy in the parsedMarker data
                                        mergedMarker = preserveMarker?.copy(
                                            inscription = parsedBasicInfoMarker.value.inscription,
                                            position = parsedBasicInfoMarker.value.position,
                                            title = parsedBasicInfoMarker.value.title,
                                            alpha = parsedBasicInfoMarker.value.alpha,
                                            subtitle = parsedBasicInfoMarker.value.subtitle,
                                        ) ?: throw IllegalStateException("markerBeforeUpdateWithDetailsAndIsSeen is null, but isDetailsLoaded=true")
                                    }

                                    // Preserve the `isSeen` state
                                    mergedMarker = preserveMarker?.copy(
                                        isSeen = preserveMarker.isSeen,
                                    ) ?: mergedMarker

                                    parsedBasicInfoMarker.key to mergedMarker
                                }
                            ).toMap(),
                    )
                    if (curProcessingHtmlPageNum == 1) {
                        markersResultState = markersResultState.copy(
                            rawMarkerCountFromFirstPageHtmlOfMultiPageResult =
                                parsedMarkersResult.rawMarkerCountFromFirstPageHtmlOfMultiPageResult
                        )
                    }
                    Log.d("Total drivable markerInfos.size after parse: ${markersResultState.markerIdToMarker.size}, Parsed markerIdToRawMarkerInfoStrings count: ${markersResultState.markerIdToRawMarkerDetailStrings.size}")

                    // 4.2 - Load more pages, if needed.
                    // - Marker list size comparison is based on the number of `markerIdToRawMarkerInfoStrings`, not the parsed
                    //   `markerInfos` because some of the markers from the page may have been rejected, and we just want
                    //   to know when the raw html is completely loaded, not how many markers were parsed.
                    if (markersResultState.markerIdToRawMarkerDetailStrings.size
                        < markersResultState.rawMarkerCountFromFirstPageHtmlOfMultiPageResult
                    ) {
                        Log.d("Loading next page..., markerIdToRawMarkerDetailStrings.size: ${markersResultState.markerIdToRawMarkerDetailStrings.size}, rawMarkerCountFromFirstHtmlPage: ${markersResultState.rawMarkerCountFromFirstPageHtmlOfMultiPageResult}")
                        curProcessingHtmlPageNum++  // trigger the next page load
                    } else {
                        //Log.d("Finished processing all pages, total markers: ${markersResultState.markerInfos.size}")
                        markersResultState = markersResultState.copy(
                            isParseMarkersPageFinished = true,
                            loadingState = LoadingState.Finished
                        )

                        // Save the final parsed results to the internal cachedMarkersResultState.
                        cachedMarkersResultState = markersResultState.copy()
                    }
                }

                LoadingState.Finished
            } catch (e: Exception) {
                shouldUpdateCache = false
                markersResultState = markersResultState.copy(
                    isParseMarkersPageFinished = true,
                    loadingState = LoadingState.Error(
                        e.cause?.message ?: "Loading error - ${e.message}"
                    )
                )
                Log.w("Failed to load page: $curProcessingHtmlPageNum, assetUrl: $assetUrl, error: ${e.cause?.message}")

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
                        text = "Loaded: ${markersResultState.markerIdToRawMarkerDetailStrings.size} / ${markersResultState.rawMarkerCountFromFirstPageHtmlOfMultiPageResult} entries\n" +
                                "Parsed: ${markersResultState.markerIdToMarker.size}\n" +
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

    return markersResultState
}

