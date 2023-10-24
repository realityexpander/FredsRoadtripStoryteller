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
import cachedMarkersLastUpdatedLocation
import cachedMarkersResult
import com.russhwolf.settings.Settings
import data.LoadingState
import network.httpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kCachedMarkersLastLoadLocationSetting
import kCachedMarkersLastUpdatedEpochSecondsSetting
import kCachedMarkersResultSetting
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
import setCachedMarkersLastUpdatedLocation
import setCachedMarkersLastUpdatedEpochSeconds
import setCachedMarkersResult
import co.touchlab.kermit.Logger as Log

@Serializable
data class MarkerInfo(
    val id: String,
    val title: String = "",
    val shortDescription: String = "",
    val lat: Double = 0.0,
    val long: Double = 0.0,
    val infoPageUrl: String = "",
    val imageUrl: String = "",
    val lastUpdatedEpochSeconds: Long = 0, // for cache expiry // todo add to cache update code
)

@Serializable
data class MarkersResult(
    val markerIdToRawMarkerInfoStrings: MutableMap<String, String> = mutableMapOf(),
    val markerInfos: Map<String, MarkerInfo> = mutableMapOf(),
    val rawMarkerCountFromFirstPageHtmlOfMultiPageResult: Int = 0,
    val isMarkerPageParseFinished: Boolean = false,

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
//   4.1 - Parse the raw page HTML into a list of `MarkerInfo` objects & metadata about the scraped data
//   4.2 - Load more pages, if needed (goto step 2)
// 5 - Save the MarkerInfo objects to cache & settings (if they were updated from the network)
// 6 - PROCESS COMPLETE
//
// Note: yes! this code is goofy looking & hops around a lot... this is because to I'm avoid using
//       a viewModel as this works as a pure composable function.
//       I'm Experimenting with a pure compose architecture with no android idiom remnants.
@Composable
fun loadMarkers(
    settings: Settings,
    // todo expose Loading state params to caller

    // Map Marker loading parameters
    userLocation: Location = Location(37.422160, -122.084270),
    maxReloadDistanceMiles: Int = 10,

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
        // Load the cached result from persistent storage (Settings) as the initial state
        mutableStateOf(settings.cachedMarkersResult())
    }

    var markersLoadingState: LoadingState<String> by remember { mutableStateOf(LoadingState.Idle) }
    var cachedMarkersResultState by remember(userLocation) {
         // Log.d("userLocation update, currentlyLoadingState: $markersLoadingState, location: $userLocation")

        // Guard - If currently loading/parsing markers, return the current parseResultState upon location change
        if(!markersResultState.isMarkerPageParseFinished) return@remember mutableStateOf(markersResultState)
        if(markersLoadingState !is LoadingState.Idle) return@remember mutableStateOf(markersResultState)

        // Step 1 - Check for a cached result in the Settings
        if (settings.cachedMarkersResult().markerInfos.isNotEmpty()) {
            val cachedMarkers = settings.cachedMarkersResult()
                    .copy(isMarkerPageParseFinished = true) // ensure the cached result is marked as finished // todo needed?

            // Log.d("Found cached markers in Settings, count: ${cachedMarkersResult.markerInfos.size}")
            markersResultState = cachedMarkers.copy(isMarkerPageParseFinished = true)

            // Step 1.1 - Check if the cache is expired // todo replace this with a per-marker expiry
            if(settings.hasKey(kCachedMarkersLastUpdatedEpochSecondsSetting)) {
                val cacheLastUpdatedEpochSeconds =
                    settings.getLong(kCachedMarkersLastUpdatedEpochSecondsSetting, 0)
                // Log.d("Days since last cache update: $(Clock.System.now().epochSeconds - cacheLastUpdatedEpochSeconds) / (60 * 60 * 24)")

                // if(true) { // test cache expiry
                if(Clock.System.now().epochSeconds > cacheLastUpdatedEpochSeconds + kMaxMarkerCacheAgeSeconds) {
                    Log.d("Cached markers are expired, dumping entire cache, attempting load from network..." ) // todo make this more sophisticated

                    // return current cached result, and also trigger network load, which will refresh the cache.
                    markersResultState = MarkersResult(isMarkerPageParseFinished = false)

                    // Clear the cache in the settings
                    coroutineScope.launch {
                        settings.remove(kCachedMarkersResultSetting)
                        // Update the cache expiry time
                        settings.putLong(
                            kCachedMarkersLastUpdatedEpochSecondsSetting,
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

                // Log.d("userDistanceFromCachedLastLocationMiles: $userDistanceFromCachedLastLocationMiles")
                if (userDistanceFromCachedLastLocationMiles > maxReloadDistanceMiles && markersResultState.isMarkerPageParseFinished) {
                    Log.d("User is outside the max re-load radius, attempting load from network..." )

                    // return current cached result, and also trigger network load, which will refresh the cache.
                    markersResultState = cachedMarkers.copy(isMarkerPageParseFinished = false)
                }
            }

            mutableStateOf(markersResultState) // return the cached result
        } else {
            Log.d { "No cached markers found. Attempting load from network..." }
            mutableStateOf(MarkersResult())// return empty result, trigger network load
        }
    }

    var markerHtmlPageUrl by remember { mutableStateOf<String?>(null) }
    var curHtmlPageNum by remember { mutableStateOf(1) }
    var shouldUpdateCache by remember { mutableStateOf(false) }
    var networkLoadingState by remember(markersResultState.isMarkerPageParseFinished, curHtmlPageNum) {
        // Step 2 - Initiate Load a page of raw marker HTML from the network
        if (!markersResultState.isMarkerPageParseFinished) {
            // Log.d("Loading page $curHtmlPageNum")
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

        // Step 5 - Finished loading pages, now Save result to cache
        cachedMarkersResultState = markersResultState.copy(
            markerIdToRawMarkerInfoStrings = mutableMapOf(), // Clear the strings (they are no longer needed)
        )

        // Save the cachedResultState to persistent storage (Settings) (if it was updated from the network)
        if (shouldUpdateCache) {
            coroutineScope.launch {
                settings.setCachedMarkersResult(cachedMarkersResultState)
                settings.setCachedMarkersLastUpdatedEpochSeconds(Clock.System.now().epochSeconds)
                settings.setCachedMarkersLastUpdatedLocation(userLocation)
                // Log.d("Saved markers to Settings, total count: ${cachedMarkersResultState.markerInfos.size}")
            }
        }

        // 6 PROCESS COMPLETE
        // Log.d("Finished loading all pages, total markers: ${parsedMarkersResultState.markerInfos.size}")
        markersLoadingState = LoadingState.Idle
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
                            isMarkerPageParseFinished = true,
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
                        markersResultState = markersResultState.copy(isMarkerPageParseFinished = true)
                        return@withContext
                    }

                    // Update the marker result state with the new parsed data
                    // Note: preserves cached results
                    markersResultState = markersResultState.copy(
                        markerIdToRawMarkerInfoStrings = (
                            markersResultState.markerIdToRawMarkerInfoStrings +
                                parsedMarkersResult.markerIdToRawMarkerInfoStrings
                            ).toMutableMap(),
                        markerInfos = (
                            markersResultState.markerInfos +
                                parsedMarkersResult.markerInfos
                            ).toMap(),
                    )
                    if (curHtmlPageNum == 1) {
                        markersResultState = markersResultState.copy(
                            rawMarkerCountFromFirstPageHtmlOfMultiPageResult =
                                parsedMarkersResult.rawMarkerCountFromFirstPageHtmlOfMultiPageResult
                        )
                    }
                    Log.d("Total drivable markerInfos.size after parse: ${markersResultState.markerInfos.size}, Parsed markerIdToRawMarkerInfoStrings count: ${markersResultState.markerIdToRawMarkerInfoStrings.size}")

                    // 4.2 - Load more pages, if needed.
                    // - Marker list size comparison is based on the number of `markerIdToRawMarkerInfoStrings`, not the parsed
                    //   `markerInfos` because some of the markers from the page may have been rejected, and we just want
                    //   to know when the raw html is completely loaded, not how many markers were parsed.
                    if (markersResultState.markerIdToRawMarkerInfoStrings.size < markersResultState.rawMarkerCountFromFirstPageHtmlOfMultiPageResult) {
                        Log.d("Loading next page..., markerIdToRawMarkerInfoStrings.size: ${markersResultState.markerIdToRawMarkerInfoStrings.size}, rawMarkerCountFromFirstHtmlPage: ${markersResultState.rawMarkerCountFromFirstPageHtmlOfMultiPageResult}")
                        curHtmlPageNum++  // trigger the next page load
                    } else {
                        //Log.d("Finished processing all pages, total markers: ${markersResultState.markerInfos.size}")
                        markersResultState = markersResultState.copy(
                            isMarkerPageParseFinished = true,
                            loadingState = LoadingState.Idle
                        )

                        // Save the final parsed results to the cache.
                        cachedMarkersResultState = markersResultState.copy()
                    }
                }

                LoadingState.Idle
            } catch (e: Exception) {
                shouldUpdateCache = false
                markersResultState = markersResultState.copy(
                    isMarkerPageParseFinished = true,
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
                        text = "Loaded: ${markersResultState.markerIdToRawMarkerInfoStrings.size} / ${markersResultState.rawMarkerCountFromFirstPageHtmlOfMultiPageResult} entries\n" +
                                "Parsed: ${markersResultState.markerInfos.size}\n" +
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

