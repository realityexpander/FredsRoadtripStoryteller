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
import com.mohamedrejeb.ksoup.entities.KsoupEntities
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlHandler
import com.mohamedrejeb.ksoup.html.parser.KsoupHtmlParser
import com.russhwolf.settings.Settings
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import sampleData.fullHtmlSamplePage1
import sampleData.fullHtmlSamplePage1a
import sampleData.fullHtmlSamplePage2
import sampleData.fullHtmlSamplePage3
import kotlin.math.*
import co.touchlab.kermit.Logger as Log

@Serializable
data class MarkerInfo(
    val id: String,
    val title: String = "",
    val description: String = "",
    val shortDescription: String = "",
    val lat: Double = 0.0,
    val long: Double = 0.0,
)

@Serializable
data class ParsedMarkersResult(
    val markerIdToRawMarkerInfoStrings: MutableMap<String, String> = mutableMapOf(),
    val markerInfos: Map<String, MarkerInfo> = mutableMapOf(),
    val rawMarkerCountFromFirstHtmlPage: Int = 0,
    val isFinished: Boolean = false,
)

const val kMarkerCacheMaxAgeSeconds = 60 * 60 * 24 * 30  // 30 days

// Strategy:
// - Check for cached data
//   - if not cached, load from network.
//   - Check for cached markers, check if user is still inside max re-load radius & cache expiry age.
//     - If not, load from network.
//     - If so, return the cached data.
// - Load from network
//   - Parse first page of HTML (to get the total number of entries & link to next page)
//   - Parse remaining pages of HTML
//   - Extract the MarkerInfo objects from the parsed HTML
//   - Save the MarkerInfo objects to cache
//
// Note: yes! this code is goofy looking... this is all to avoid using a viewModel and make
//       this work as a composable function. Experimenting with a pure compose architecture with
//       no android remnants.
@Composable
fun loadMarkers(
    settings: Settings,

    // Load Map Marker parameters
    myLocation: Location = Location(37.422160, -122.084270),
    maxReloadDistanceMiles: Int = 10,

    // Debugging parameters
    showLoadingState: Boolean = false,
    useFakeDataSetId: Int = 0,  // 0 = use real data, >1 = use fake data (1 = 3 pages around googleplex, 2 = 1 page around tepoztlan)
): ParsedMarkersResult {
    if (myLocation.latitude == 0.0 && myLocation.longitude == 0.0)
        return ParsedMarkersResult()

    val coroutineScope = rememberCoroutineScope()

//    var parsedMarkersResultState by remember { mutableStateOf(ParsedMarkersResult()) } // todo load the settings kCachedParsedMarkersResult instead of the parsedResult
    var parsedMarkersResultState by remember {
        mutableStateOf(
            json.decodeFromString<ParsedMarkersResult>(settings.getString(kCachedParsedMarkersResult, "{}"))
        ) } // todo load the settings kCachedParsedMarkersResult instead of the parsedResult - test
    var didCacheUpdateFromNetwork by remember { mutableStateOf(false) }
    var cacheResultState by remember(myLocation) {

        // Log.i("myLocation: $myLocation")

        // If currently loading/parsing markers, return the current parseResultState upon location change
        if(!parsedMarkersResultState.isFinished) return@remember mutableStateOf(parsedMarkersResultState)

        // Step 1 - Check for a cached result in the Settings
        if (settings.hasKey(kCachedParsedMarkersResult)) {
            val cachedMarkers =
                json.decodeFromString<ParsedMarkersResult>(
                    settings.getString(kCachedParsedMarkersResult, "")
                ).copy(isFinished = true)
            // Log.i("Found cached markers in Settings, count: ${cachedParsedMarkersResult.markerInfos.size}")
            parsedMarkersResultState = cachedMarkers.copy(isFinished = true)

            // Check if the cache is expired
            if(settings.hasKey(kCachedMarkersLastUpdatedEpochSeconds)) {
                val cacheLastUpdatedEpochSeconds =
                    settings.getLong(kCachedMarkersLastUpdatedEpochSeconds, 0)
                // Log.i("Days since last cache update: $(Clock.System.now().epochSeconds - cacheLastUpdatedEpochSeconds) / (60 * 60 * 24)")

                // if(true) { // test cache expiry
                if(Clock.System.now().epochSeconds > cacheLastUpdatedEpochSeconds + kMarkerCacheMaxAgeSeconds) {
                    Log.d("Cached markers are expired, attempting load from network..." )

                    // return current cached result, and also trigger network load, which will refresh the cache.
                    parsedMarkersResultState = cachedMarkers.copy(isFinished = false)
                }
            }

            // Check if the user is outside the reload radius
            if (settings.hasKey(kCachedMarkersLastLocationLatLong)) {
                val cachedMarkersLastLocationLatLong =
                    json.decodeFromString<Location>(
                        settings.getString(kCachedMarkersLastLocationLatLong, "{latitude:0.0, longitude:0.0}")
                    )
                //Log.i("cachedMarkersLastLocationLatLong: $cachedMarkersLastLocationLatLong")
                val userDistanceFromCachedLastLocationMiles = distanceBetween(
                    myLocation.latitude,
                    myLocation.longitude,
                    cachedMarkersLastLocationLatLong.latitude,
                    cachedMarkersLastLocationLatLong.longitude)

                Log.i("userDistanceFromCachedLastLocationMiles: $userDistanceFromCachedLastLocationMiles")
                if (userDistanceFromCachedLastLocationMiles > maxReloadDistanceMiles && parsedMarkersResultState.isFinished) {
                    Log.d("User is outside the max re-load radius, attempting load from network..." )

                    // return current cached result, and also trigger network load, which will refresh the cache.
                    parsedMarkersResultState = cachedMarkers.copy(isFinished = false)
                }
            }

            mutableStateOf(parsedMarkersResultState) // return the cached result
        } else {
            Log.d { "No cached markers found. Attempting load from network..." }
            mutableStateOf(ParsedMarkersResult())// return empty result, trigger network load
        }
    }

    var curMarkerHtmlPageNum by remember { mutableStateOf(1) }
    var assetUrl by remember { mutableStateOf<String?>(null) }
    var loadingState by remember(parsedMarkersResultState.isFinished, curMarkerHtmlPageNum) {
        // Step 2 - Load a page of marker HTML from the network
        if (!parsedMarkersResultState.isFinished) {
            Log.i("Loading page $curMarkerHtmlPageNum")

            if(useFakeDataSetId > 0) {
                // Step 3(test) - Simulate loading from network
                didCacheUpdateFromNetwork = true // simulate fake data being loaded from network
                mutableStateOf(generateSampleMarkerPageHtml(curMarkerHtmlPageNum, useFakeDataSetId)) // use fake data
            } else {
                // Step 3(real) - Start the load from network
                didCacheUpdateFromNetwork = true
                assetUrl = "https://www.hmdb.org/results.asp?Search=Coord" +
//                    "&Latitude=37.422160" +
//                    "&Longitude=-122.084270" +  // Sunnyvale, CA
//                    "&Miles=10" +
                        "&Latitude=" + myLocation.latitude +
                        "&Longitude=" + myLocation.longitude +
                        "&Miles=" + maxReloadDistanceMiles +
                        "&MilesType=1&HistMark=Y&WarMem=Y&FilterNOT=&FilterTown=&FilterCounty=&FilterState=&FilterCountry=&FilterCategory=0" +
                        "&Page=$curMarkerHtmlPageNum"

                mutableStateOf<LoadingState<String>>(LoadingState.Loading())
            }
        } else {
            // Step 4 - Finished loading pages, now Save result to cache
            cacheResultState = parsedMarkersResultState.copy(
                markerIdToRawMarkerInfoStrings = mutableMapOf(), // don't need the strings anymore, so remove it to save memory
            )

            // Save the cachedResultState to persistent storage (Settings) (if it was updated from the network)
            if (didCacheUpdateFromNetwork) {
                coroutineScope.launch {
                    settings.putString(
                        kCachedParsedMarkersResult,
                        json.encodeToString(ParsedMarkersResult.serializer(), cacheResultState)
                    )
                    settings.putLong(
                        kCachedMarkersLastUpdatedEpochSeconds,
                        Clock.System.now().epochSeconds
                    )
                    settings.putString(
                        kCachedMarkersLastLocationLatLong,
                        json.encodeToString(myLocation)
                    )
                    Log.i("Saved markers to cache, total count: ${cacheResultState.markerInfos.size}")
                }
            }

            // Indicate finished loading
            mutableStateOf<LoadingState<String>>(LoadingState.Idle)
        }
    }

    // Load the data from the network when `assetUrl` is updated
    LaunchedEffect(assetUrl) {
        // 3(real) - Perform the load from network
        assetUrl?.let { assetUrl ->
            loadingState = try {
                Log.d("Loading $assetUrl")
                val response = httpClient.get(assetUrl)
                val data: String = response.body()
                LoadingState.Loaded(data)
            } catch (e: Exception) {
                // Set "isFinished" to true here? // todo test this
                parsedMarkersResultState = parsedMarkersResultState.copy(isFinished = true)
                LoadingState.Error(e.cause?.message ?: "error")
            }
        }
    }

    // After the marker HTML data is loaded, parse it to extract the MarkerInfo's
    LaunchedEffect(loadingState) {
        if (loadingState !is LoadingState.Loaded<String>) // only run when the data loading is complete.
            return@LaunchedEffect

        withContext(Dispatchers.Default) {
            // Step 3.1 - Parse the HTML to extract the marker info
            val data = (loadingState as LoadingState.Loaded<String>).data
            if (data.isBlank()) {
                Log.w("Blank data for page $curMarkerHtmlPageNum, location: $myLocation")
                loadingState = LoadingState.Error("Blank data for page $curMarkerHtmlPageNum, location: $myLocation")
                return@withContext
            }
            Log.i("Parsing HTML...Current markers count: ${parsedMarkersResultState.markerInfos.size}")

            // Parse the raw HTML into a list of `markerToInfoStrings` and a list
            //   of `markerInfos` without lat/long values & descriptions.
            val parsedMarkersResult = parseHtml(data)
            if (parsedMarkersResult.rawMarkerCountFromFirstHtmlPage == 0) {
                Log.w("No entries found for page: $curMarkerHtmlPageNum, location: $myLocation")
                // set loading to finished
                // loadingState = LoadingState.Idle // needed?
                parsedMarkersResultState = parsedMarkersResultState.copy(isFinished = true)
                return@withContext
            }

            // Update the parseResultState with the new parsed data
            if (curMarkerHtmlPageNum == 1) { // preserves cached results
//                parseResultState = parseResult
//                println("Parsed result state: ${parseResultState.markerInfos.size}") // todo: remove this
                Log.i("parseResultState.markerInfos.size: ${parsedMarkersResultState.markerInfos.size}")
                parsedMarkersResultState = parsedMarkersResult.copy(
                    rawMarkerCountFromFirstHtmlPage = parsedMarkersResult.rawMarkerCountFromFirstHtmlPage,
//                    isFinished = parseResult.isFinished,
                    markerIdToRawMarkerInfoStrings = (parsedMarkersResultState.markerIdToRawMarkerInfoStrings + parsedMarkersResult.markerIdToRawMarkerInfoStrings).toMutableMap(),
                    markerInfos = (parsedMarkersResultState.markerInfos + parsedMarkersResult.markerInfos).toMap(),
                )
            } else {
                parsedMarkersResultState = parsedMarkersResultState.copy(
                    markerIdToRawMarkerInfoStrings = (parsedMarkersResultState.markerIdToRawMarkerInfoStrings + parsedMarkersResult.markerIdToRawMarkerInfoStrings).toMutableMap(),
                    markerInfos = (parsedMarkersResultState.markerInfos + parsedMarkersResult.markerInfos).toMap(),
                )
            }
            Log.i("Found Drivable Map Location (markerInfos) entries count: ${parsedMarkersResultState.markerInfos.size}, Parsed markerIdToRawMarkerInfoStrings count: ${parsedMarkersResultState.markerIdToRawMarkerInfoStrings.size}")

            // Load more pages, if needed.
            // - Marker list size comparison is based on the number of `markerIdToRawMarkerInfoStrings`, not the parsed
            //   `markerInfos` because some of the markers from the page may have been rejected, and we just want
            //   to know when the raw html is completely loaded, not how many markers were parsed.
            if (parsedMarkersResultState.markerIdToRawMarkerInfoStrings.size < parsedMarkersResultState.rawMarkerCountFromFirstHtmlPage) {
                Log.i("Loading next page..., markerIdToRawMarkerInfoStrings.size: ${parsedMarkersResultState.markerIdToRawMarkerInfoStrings.size}, rawMarkerCountFromFirstHtmlPage: ${parsedMarkersResultState.rawMarkerCountFromFirstHtmlPage}")
                //loadingState = LoadingState.Loading() // todo necessary?
                curMarkerHtmlPageNum++  // trigger the next page load
            } else {
                // Finish loading all pages
                parsedMarkersResultState = parsedMarkersResultState.copy(isFinished = true)

                // Save the parsed results to the cache
                cacheResultState = parsedMarkersResultState.copy()
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
            when (val state = loadingState) {
                is LoadingState.Loading -> {
                    Text("Fred's Talking Historical Markers")
                }

                is LoadingState.Loaded<String> -> {
                    Text(
                        fontSize = 18.sp,
                        text = "Loaded: ${parsedMarkersResultState.markerIdToRawMarkerInfoStrings.size} / ${parsedMarkersResultState.rawMarkerCountFromFirstHtmlPage} entries\n" +
                                "Parsed: ${parsedMarkersResultState.markerInfos.size}\n" +
                                "Data size: ${state.data.length} chars"
                    )
                }

                is LoadingState.Error -> {
                    Text("Error: ${state.message}")
                }

                else -> {
                    Text("Finished loading")
                }
            }
        }
    }

    return parsedMarkersResultState // todo: return the cachedResult instead of the parsedResult
}

// from https://dzone.com/articles/distance-calculation-using-3
fun distanceBetween(lat1: Double, lon1: Double, lat2: Double, lon2: Double, shouldUseKM: Boolean = true): Double {
    val theta = lon1 - lon2
    var dist: Double = (sin(deg2rad(lat1))
            * sin(deg2rad(lat2))
            + (cos(deg2rad(lat1))
            * cos(deg2rad(lat2))
            * cos(deg2rad(theta))))
    dist = acos(dist)
    dist = rad2deg(dist)
    dist *= 60 * 1.1515

    if(shouldUseKM)
        dist *= 1.609344

    return dist
}
private fun deg2rad(deg: Double): Double {
    return deg * PI / 180.0
}

private fun rad2deg(rad: Double): Double {
    return rad * 180.0 / PI
}

suspend fun parseHtml(htmlResponse: String): ParsedMarkersResult {
    if (htmlResponse.isBlank()) {
        Log.w { "htmlResponse is Blank" }
        return ParsedMarkersResult()
    }

    var isListItselfFound = false

    var isCapturingMarkerText = false
    var curCaptureMarkerId = ""
    var capturePhase = 0  // 1 == marker title, 2 == text

    val markerIdToRawMarkerInfoStringsMap = mutableMapOf<String, String>()
    var rawMarkerCountFromHtmlMultiPageDocument = 0
    var rawMarkerCountFoundInHtml = 0
    var totalMarkersAtLocation = 0

    val markerInfos = mutableMapOf<String, MarkerInfo>()

    // Create the scraper callbacks for the parser
    val handler = KsoupHtmlHandler
        .Builder()
        .onText { text ->
            if (isCapturingMarkerText) {
                val strippedBlankLines = text.trim()

                if (strippedBlankLines.isNotEmpty())
                    markerIdToRawMarkerInfoStringsMap[curCaptureMarkerId] =
                        (markerIdToRawMarkerInfoStringsMap[curCaptureMarkerId] ?: "") + KsoupEntities.decodeHtml(
                            strippedBlankLines
                        ) + "\n"
            }

            // First page only
            // Check for the entry count (239 entries matched your criteria. The first 100 are listed above.)
            if (text.contains("entries matched your criteria.", ignoreCase = true)) {
                rawMarkerCountFromHtmlMultiPageDocument = text.substringBefore("entries").trim().toIntOrNull() ?: 0
                // Log.i { "Entry count: $entryCount" }
            }
        }
        .onOpenTag { tagName, attributes, isSelfClosing ->

            // Find the "TheListItself" div, which contains the list of markers
            if (tagName == "div" && attributes["id"] == "TheListItself") {
                isListItselfFound = true
            }
            if (!isListItselfFound) return@onOpenTag

            // Found a marker
            if (tagName == "table") {
                if (attributes["id"]?.startsWith("M") == true) {
                    // Log.i { "Found a marker ${attributes["id"]}" }
                    curCaptureMarkerId = attributes["id"]!!
                    rawMarkerCountFoundInHtml++
                }
            }

            if (tagName == "td") {
                isCapturingMarkerText = true
            }

            if (tagName == "a" && isCapturingMarkerText) {
                if (attributes["href"]?.contains("https://www.google.com/maps/dir/?api=1&destination=") == true
                ) {
                    val lat = attributes["href"]
                        ?.substringAfter("destination=")
                        ?.substringBefore(",")
                        ?.toDoubleOrNull() ?: run {
                        Log.i { "Failed to parse lat value for latlong link, marker id: $curCaptureMarkerId" }
                        return@onOpenTag
                    }
                    val long = attributes["href"]
                        ?.substringAfter(",")
                        ?.substringBefore(" ")
                        ?.toDoubleOrNull() ?: run {
                        Log.i { "Failed to parse long value for latlong link, marker id: $curCaptureMarkerId" }
                        return@onOpenTag
                    }

                    // Log.i { "Found an a lat long link, Lat long: $lat, $long")  }
                    markerInfos[curCaptureMarkerId] = markerInfos[curCaptureMarkerId]?.copy(
                        lat = lat,
                        long = long
                    ) ?: MarkerInfo(
                        id = curCaptureMarkerId,
                        lat = lat,
                        long = long,
                    )
                }
            }
        }
        .onCloseTag { tagName, isSelfClosing ->
            if (tagName == "td" && isListItselfFound) {
                capturePhase++
                if (capturePhase == 2) {
                    capturePhase = 0
                    isCapturingMarkerText = false
                    // Log.i { "Captured marker: $curCaptureMarkerId")  }
                    // Log.i { "Captured text: ${markerToInfoStrings[curCaptureMarkerId]}")  }
                }
            }
        }
        .onAttribute { tagName, attributeName, attributeValue ->
            if (tagName == "id" && attributeName == "TheListItself") {
                isListItselfFound = true
            }

            // Found a marker
            if (tagName == "id" && isListItselfFound) {
                if (attributeName.startsWith("M")) {
                    attributeName.substring(1).toIntOrNull()?.let { markerId ->
                        // Log.i { "Found Marker id: $markerId")  }
                    }
                }
            }
        }
        .build()

    // Create parser & pass the HTML to it
    val ksoupHtmlParser = KsoupHtmlParser(handler = handler)
    ksoupHtmlParser.write(htmlResponse)
    ksoupHtmlParser.end()

    // Process the raw extracted strings into `MarkerInfo` objects
    markerIdToRawMarkerInfoStringsMap.forEach { (markerId, infoString) ->
        // break info string into lines
        val lines = infoString.split("\n")

        val shortLocation = lines[2]
        val title = lines[3]

        // val oneLineDescription = lines[5]

        // Collect lines between first and second dashes
        var indexOfFirstDashLine = 0
        var indexOfSecondDashLine = 0
        lines.forEachIndexed { i, line ->
            if (line == "—") {
                if (indexOfFirstDashLine == 0)  // first dash
                    indexOfFirstDashLine = i
                else if (indexOfSecondDashLine == 0)  // second dash
                    indexOfSecondDashLine = i
            }
        }
        val description = if(indexOfSecondDashLine > indexOfFirstDashLine) {
            lines.subList(indexOfFirstDashLine+1, indexOfSecondDashLine).joinToString("\n")
        } else {
            lines[5]  // default to one Line Description
        }

        val lat = markerInfos[markerId]?.lat ?: run {
            Log.i { "Failed to find lat value for marker id: $markerId" }
            return@forEach
        }
        val long = markerInfos[markerId]?.long ?: run {
            Log.i { "Failed to find long value for marker id: $markerId" }
            return@forEach
        }

        markerInfos[markerId] = MarkerInfo(
            id = markerId,
            title = title,
            description = description,
            shortDescription = shortLocation,
            lat = lat,
            long = long,
        )
    }

    if(rawMarkerCountFoundInHtml > 0 && rawMarkerCountFromHtmlMultiPageDocument == 0)
        totalMarkersAtLocation = rawMarkerCountFoundInHtml
    else
        totalMarkersAtLocation = rawMarkerCountFromHtmlMultiPageDocument

    return ParsedMarkersResult(
        markerIdToRawMarkerInfoStringsMap,
        markerInfos,
        totalMarkersAtLocation
    )
}

fun generateSampleMarkerPageHtml(pageNum: Int, useTestData: Int): LoadingState<String> {
    return LoadingState.Loaded(
        if(useTestData == 1) {
            when (pageNum) {
                1 -> fullHtmlSamplePage1()  // 3 pages based in Sunnyvale, CA
                2 -> fullHtmlSamplePage2()
                3 -> fullHtmlSamplePage3()
                else -> throw Exception("Invalid page number: $pageNum")
            }
        } else {
            when (pageNum) {
                1 -> fullHtmlSamplePage1a()  // 1 page only based in Tepoztlan, Mexico
                else -> throw Exception("Invalid page number: $pageNum")
            }
        }
    )
}
