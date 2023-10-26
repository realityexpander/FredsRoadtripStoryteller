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
import data.cachedMarkersLastUpdatedLocation
import data.cachedMarkersResult
import com.russhwolf.settings.Settings
import data.LoadingState
import data.cachedMarkersLastUpdateEpochSeconds
import network.httpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import data.kCachedMarkersLastLoadLocationSetting
import data.kcachedMarkersLastUpdateEpochSecondsSetting
import data.kCachedMarkersResultSetting
import kMaxMarkerCacheAgeSeconds
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.datetime.Clock
import kotlinx.serialization.Contextual
import kotlinx.serialization.Serializable
import data.loadMarkers.sampleData.simpleMarkersPageHtml
import data.loadMarkers.sampleData.kUseRealNetwork
import data.setCachedMarkersLastUpdatedLocation
import data.setCachedMarkersLastUpdateEpochSeconds
import data.setCachedMarkersResult
import maps.MapMarker
import maps.MarkerIdStr
import co.touchlab.kermit.Logger as Log

@Serializable
data class MarkersResult(
    val markerIdToRawMarkerDetailStrings: MutableMap<MarkerIdStr, String> = mutableMapOf(),
    val markerIdToMapMarker: Map<MarkerIdStr, MapMarker> = mutableMapOf(),
    val rawMarkerCountFromFirstPageHtmlOfMultiPageResult: Int = 0,
    val isParseMarkersPageFinished: Boolean = false,

    @Contextual
    val loadingState: LoadingState<String> = LoadingState.Idle,
)

// Strategy:
// 1 - Check for cached data
//   - if not cached, attempt load from network (go to step 2)
//   1.1 - Check for cached markers & load them if they exist.
//   1.2 - Check if user is still inside max re-load radius & cache has not expired.
//     - If so, return the cached data.
//     - If not, attempt load markers from network for this location.
// 2 - Initiate Load from network
// 3 - Perform Load & Error checking
// 4 - Parse the HTML
//   4.1 - Parse the raw page HTML into a list of `MapMarker` objects & metadata about the scraped data
//   4.2 - Load more pages, if needed (goto step 2)
// 5 - Save the MapMarker objects to cache & settings (if they were updated from the network)
// 6 - PROCESS COMPLETE
//
// Note: yes! this code is goofy looking & hops around a lot... this is because to I'm avoid using
//       a ViewModel as this works as a pure composable function. There is no ping-pong between files. Its all here.
//       I'm Experimenting with a pure compose architecture with no android idiom remnants.
@Composable
fun loadMarkers(
    settings: Settings,
    // todo expose Loading state params to caller

    // Map Marker loading parameters
    userLocation: Location = Location(37.422160, -122.084270),
    maxReloadDistanceMiles: Int = 10,
    onSetCachedMarkersLastUpdatedLocation: (Location) -> Unit = {},

    // Debugging parameters
    showLoadingState: Boolean = false,
    useFakeDataSetId: Int = 0,  // 0 = use real data, >1 = use fake data (1 = 3 pages around googleplex, 2 = 1 page around tepoztlan)

): MarkersResult {
    // If the user location is not set, return an empty result
    if (userLocation.latitude == 0.0 && userLocation.longitude == 0.0)
        return MarkersResult()

    val coroutineScope = rememberCoroutineScope()

    // Holds the current processing state of the parsed markers
    var markersResultState by remember {
        mutableStateOf(settings.cachedMarkersResult()) // Load the cached result from persistent storage (Settings) as the initial state
    }

    // Overall state of the load & parse
    var markersLoadingState: LoadingState<String> by remember { mutableStateOf(LoadingState.Idle) }
    var curHtmlPageNum by remember { mutableStateOf(0) }
    var cachedMarkersResultState by remember(userLocation) {
        // Guard - If currently loading/parsing markers, return the current parseResultState upon location change
        if(!markersResultState.isParseMarkersPageFinished) return@remember mutableStateOf(markersResultState)
        if(markersLoadingState !is LoadingState.Idle) return@remember mutableStateOf(markersResultState)

        // Step 1 - Check for a cached result in the Settings
        if (settings.cachedMarkersResult().markerIdToMapMarker.isNotEmpty()) {
            val cachedMarkersResult =
                settings.cachedMarkersResult()
                    .copy(isParseMarkersPageFinished = true) // ensure the cached result is marked as finished

            // Log.d("Found cached markers in Settings, count: ${cachedMarkersResult.markerInfos.size}")
            markersResultState = cachedMarkersResult.copy(isParseMarkersPageFinished = true)

            // Step 1.1 - Check if the cache is expired // todo replace this with a per-marker expiry
            if(settings.hasKey(kcachedMarkersLastUpdateEpochSecondsSetting)) {
                val cacheLastUpdatedEpochSeconds = settings.cachedMarkersLastUpdateEpochSeconds()
                // Log.d("Days since last cache update: $(Clock.System.now().epochSeconds - cacheLastUpdatedEpochSeconds) / (60 * 60 * 24)")

                // if(true) { // test cache expiry
                if(Clock.System.now().epochSeconds > cacheLastUpdatedEpochSeconds + kMaxMarkerCacheAgeSeconds) {
                    Log.d("Cached markers are expired, dumping entire cache, attempting load from network..." ) // todo make this more sophisticated

                    // return current cached result, and also trigger network load, which will refresh the cache.
                    markersResultState = MarkersResult(isParseMarkersPageFinished = false)

                    // Clear the cache in the settings
                    coroutineScope.launch {
                        settings.remove(kCachedMarkersResultSetting)
                        // Update the cache expiry time
                        settings.putLong(
                            kcachedMarkersLastUpdateEpochSecondsSetting,
                            Clock.System.now().epochSeconds
                        )
                        Log.d("Cache expired, cleared markers from Settings")
                    }
                }
            }

            // Step 1.2 - Check if the user is outside the reload radius
            if (settings.hasKey(kCachedMarkersLastLoadLocationSetting)) {
                val cachedMarkersLastLoadLocation = settings.cachedMarkersLastUpdatedLocation()
                val userDistanceFromCachedLastLocationMiles = distanceBetween(
                    userLocation.latitude,
                    userLocation.longitude,
                    cachedMarkersLastLoadLocation.latitude,
                    cachedMarkersLastLoadLocation.longitude
                )

                if (userDistanceFromCachedLastLocationMiles > maxReloadDistanceMiles &&
                    markersResultState.isParseMarkersPageFinished
                ) {
                    // return current cached result, and also trigger network load, which will refresh the cache.
                    // Log.d("User is outside the max re-load radius, attempting load from network..." )
                    markersResultState = cachedMarkersResult.copy(isParseMarkersPageFinished = false)
                    curHtmlPageNum = 1 // reset the page number to 1
                }
            }

            mutableStateOf(markersResultState) // return the cached result
        } else {
            Log.d { "No cached markers found. Attempting load from network..." }
            markersResultState = markersResultState.copy(
                isParseMarkersPageFinished = false,
                loadingState = LoadingState.Loading
            )
            curHtmlPageNum = 1
            mutableStateOf(MarkersResult())// return empty result, trigger network load
        }
    }

    var markerHtmlPageUrl by remember { mutableStateOf<String?>(null) }
    var shouldUpdateCache by remember { mutableStateOf(false) }
    var networkLoadingState by remember(markersResultState.isParseMarkersPageFinished, curHtmlPageNum) {
        // Guard
        if(curHtmlPageNum == 0) return@remember mutableStateOf<LoadingState<String>>(LoadingState.Idle)

        // Step 2 - Initiate Load a page of raw marker HTML from the network
        if (!markersResultState.isParseMarkersPageFinished) {
            Log.d("Loading page number $curHtmlPageNum")
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
                "&Page=$curHtmlPageNum"

            return@remember mutableStateOf<LoadingState<String>>(LoadingState.Loading)// triggers network load
        }

        // Step 5 - Finished loading pages, now Save result to internal cache
        cachedMarkersResultState = markersResultState.copy(
            markerIdToRawMarkerDetailStrings = mutableMapOf(), // Clear the strings (they are no longer needed)
        )

        // Save the cachedResultState to persistent storage (Settings) (if it was updated from the network)
        if (shouldUpdateCache) {
            println("Saving markers to Settings, total count: ${cachedMarkersResultState.markerIdToMapMarker.size}, setCachedMarkersLastUpdatedLocation: $userLocation")
            coroutineScope.launch {
                settings.setCachedMarkersResult(cachedMarkersResultState)
                settings.setCachedMarkersLastUpdateEpochSeconds(Clock.System.now().epochSeconds)

                settings.setCachedMarkersLastUpdatedLocation(userLocation).also {
                    onSetCachedMarkersLastUpdatedLocation(userLocation)
                }
                // Log.d("Saved markers to Settings, total count: ${cachedMarkersResultState.markerIdToMapMarker.size}")
            }
        }

        // 6 PROCESS COMPLETE
        Log.d("Finished loading all pages, total markers:${cachedMarkersResultState.markerIdToMapMarker.size}")
        markersLoadingState = LoadingState.Idle
        curHtmlPageNum = 0
        mutableStateOf<LoadingState<String>>(LoadingState.Idle)

    }

    // Load the data from the network when `markerHtmlPageUrl` is changed
    LaunchedEffect(markerHtmlPageUrl) {

        // Step 3 (real network) - Perform the load from network
        markerHtmlPageUrl?.let { assetUrl ->
            networkLoadingState = LoadingState.Loading // leave for debugging
            markersResultState = markersResultState.copy(
                loadingState = LoadingState.Loading
            )
            yield() // allow the UI to update before loading the network data // todo necessary? test UI

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
                        simpleMarkersPageHtml(curHtmlPageNum, useFakeDataSetId)
                    }

                // Step 4 - Parse the HTML to extract the marker info
                withContext(Dispatchers.Default) {
                    // Log.d("Before parsing raw HTML, markerInfos.size: ${markersResultState.markerInfos.size}, markerIdToRawMarkerInfoStrings.size: ${markersResultState.markerIdToRawMarkerInfoStrings.size}")

                    // Guard against blank data
                    if (rawHtmlString.isBlank()) {
                        Log.w("Blank data for page $curHtmlPageNum, location: $userLocation")
                        networkLoadingState =
                            LoadingState.Error("Blank data for page $curHtmlPageNum, location: $userLocation") // leave for debugging
                        markersResultState = markersResultState.copy(
                            isParseMarkersPageFinished = true,
                            loadingState = LoadingState.Error("Blank data for page $curHtmlPageNum, location: $userLocation")
                        )
                        return@withContext
                    }

                    // 4.1 - Parse the raw page HTML into a list of `MarkerInfo` objects & metadata about the scraped data
                    // Log.d("Parsing HTML.. Current markers in cache (before parsing) count: ${parsedMarkersResultState.markerInfos.size}")
                    val parsedMarkersResult = parseMarkersPageHtml(rawHtmlString)

                    // Check for zero `raw html` marker entries
                    if (parsedMarkersResult.rawMarkerCountFromFirstPageHtmlOfMultiPageResult == 0) {
                        Log.w("No raw html marker entries found for page: $curHtmlPageNum, location: $userLocation")
                        markersResultState = markersResultState.copy(isParseMarkersPageFinished = true)
                        return@withContext
                    }

                    // Update the marker result state with the new parsed data
                    // Note: preserves cached results
                    markersResultState = markersResultState.copy(
                        markerIdToRawMarkerDetailStrings = (
                            markersResultState.markerIdToRawMarkerDetailStrings +
                                parsedMarkersResult.markerIdToRawMarkerDetailStrings
                            ).toMutableMap(),
                        markerIdToMapMarker = (
                            markersResultState.markerIdToMapMarker +
                                parsedMarkersResult.markerIdToMapMarker
                            ).toMap(),
                    )
                    if (curHtmlPageNum == 1) {
                        markersResultState = markersResultState.copy(
                            rawMarkerCountFromFirstPageHtmlOfMultiPageResult =
                                parsedMarkersResult.rawMarkerCountFromFirstPageHtmlOfMultiPageResult
                        )
                    }
                    Log.d("Total drivable markerInfos.size after parse: ${markersResultState.markerIdToMapMarker.size}, Parsed markerIdToRawMarkerInfoStrings count: ${markersResultState.markerIdToRawMarkerDetailStrings.size}")

                    // 4.2 - Load more pages, if needed.
                    // - Marker list size comparison is based on the number of `markerIdToRawMarkerInfoStrings`, not the parsed
                    //   `markerInfos` because some of the markers from the page may have been rejected, and we just want
                    //   to know when the raw html is completely loaded, not how many markers were parsed.
                    if (markersResultState.markerIdToRawMarkerDetailStrings.size < markersResultState.rawMarkerCountFromFirstPageHtmlOfMultiPageResult) {
                        Log.d("Loading next page..., markerIdToRawMarkerDetailStrings.size: ${markersResultState.markerIdToRawMarkerDetailStrings.size}, rawMarkerCountFromFirstHtmlPage: ${markersResultState.rawMarkerCountFromFirstPageHtmlOfMultiPageResult}")
                        curHtmlPageNum++  // trigger the next page load
                    } else {
                        //Log.d("Finished processing all pages, total markers: ${markersResultState.markerInfos.size}")
                        markersResultState = markersResultState.copy(
                            isParseMarkersPageFinished = true,
                            loadingState = LoadingState.Idle
                        )

                        // Save the final parsed results to the internal cachedMarkersResultState.
                        cachedMarkersResultState = markersResultState.copy()
                    }
                }

                LoadingState.Idle
            } catch (e: Exception) {
                shouldUpdateCache = false
                markersResultState = markersResultState.copy(
                    isParseMarkersPageFinished = true,
                    loadingState = LoadingState.Error(
                        e.cause?.message ?: "Loading error - ${e.message}"
                    )
                )
                Log.w("Failed to load page: $curHtmlPageNum, assetUrl: $assetUrl, error: ${e.cause?.message}")

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
                                "Parsed: ${markersResultState.markerIdToMapMarker.size}\n" +
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

