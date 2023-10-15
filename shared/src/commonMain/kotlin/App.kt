import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.russhwolf.settings.Settings
import com.russhwolf.settings.contains
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import loadMarkers.MarkersResult
import loadMarkers.loadMarkers
import co.touchlab.kermit.Logger as Log

val json = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}


const val kMaxReloadDistanceMiles = 2
const val kMaxMarkerCacheAgeSeconds = 60 * 60 * 24 * 30  // 30 days

// Settings Keys
const val kCachedParsedMarkersResult = "cachedMarkersResult"
const val kCachedMarkersLastUpdatedEpochSeconds = "cachedMarkersLastUpdatedEpochSeconds"
const val kCachedMarkersLastLocationLatLong = "cachedMarkersLastLocationLatLong"
const val kLastKnownUserLocation = "LastKnownUserLocation"

@Composable
fun App() {

    MaterialTheme {
        val coroutineScope = rememberCoroutineScope()

        var greetingText by remember { mutableStateOf("Hello, World!") }
        var showImage by remember { mutableStateOf(false) }

        val settings = remember {
            Settings().apply {
//                clear()  // Force cache refresh // todo test location cache from start.
                // Log.setMinSeverity(Severity.Warn)

                // Show current settings
                Log.d { "keys from settings: $keys" }
                Log.d("Settings: cachedMarkersResult markerInfos.size= " +
                        json.decodeFromString<MarkersResult>(getString(kCachedParsedMarkersResult, "{}")).markerInfos.size.toString())
                Log.d("Settings: cachedMarkersLastUpdatedEpochSeconds= " +
                        getLong(kCachedMarkersLastUpdatedEpochSeconds, 0L).toString())
                Log.d("Settings: cachedMarkersLastLocationLatLong= " +
                        getString(kCachedMarkersLastLocationLatLong, "{latitude:0.0, longitude:0.0}"))
                Log.d("Settings: LastKnownUserLocation= " +
                        getString(kLastKnownUserLocation, "{latitude:0.0, longitude:0.0}"))
            }
        }
        val locationService by remember { mutableStateOf(LocationService()) }
        var userLocation: Location by remember {
            mutableStateOf(
                // Load last known location
                if(settings.contains(kLastKnownUserLocation)) {
                    val latLong = json.decodeFromString<Location>(
                        settings.getString(kLastKnownUserLocation,
                            "{latitude:0.0, longitude:0.0}")
                    )
                    Location(latLong.latitude, latLong.longitude)
                } else {
                    // Default to UNKNOWN? // todo - use a default location
                    Location(0.0, 0.0)
                }
            )
        }
        val markersLoadResult = loadMarkers(
            settings,
            userLocation = userLocation,
            maxReloadDistanceMiles = kMaxReloadDistanceMiles,
            showLoadingState = false,
            useFakeDataSetId = 0  // 0 = real data, 1 = Googleplex, 2 = Tepoztlan,
        )
        val cachedMapMarkers = remember { // prevents flicker when loading new markers
            mutableStateListOf<MapMarker>()
        }
        var shouldUpdateMapMarkers by remember {
            mutableStateOf(true)
        }
        val mapMarkers = remember(markersLoadResult.isFinished) {

            if (!markersLoadResult.isFinished) { // While loading new markers, use the cached markers to prevent flicker
                return@remember cachedMapMarkers
            }

            if (false) {
//                Log.d { "marker count = ${markersData.value.markerInfos.size}")
//                mutableStateListOf(
////                    MapMarker(
////                        key = "marker4",
////                        position = LatLong(
////                            37.422160,
////                            -122.084270
////                        ),
////                        title = "Googleplex"
////                    ),
////                    MapMarker(
////                        key = "marker2",
////                        position = LatLong(
////                            37.400550,
////                            -122.108651
////                        ),
////                        title = "Facebook HQ"
////                    ),
////                    MapMarker(
////                        key = "marker3",
////                        position = LatLong(
////                            37.432160,
////                            -122.086270
////                        ),
////                        title = "Center"
////                    ),
////                    MapMarker(
////                        key = "marker1",
////                        position = LatLong(
////                            myLocation.latitude,
////                            myLocation.longitude
////                        ),
////                        title = "Another"
////                    ),
//                    MapMarker(
//                        key =
//                        //                    markersData.value.markerInfos.entries.firstOrNull()?.key ?:
//                        "temp",
//                        position = LatLong(
//                            markersData.value.markerInfos.entries.firstOrNull()?.value?.lat ?: 0.0,
//                            markersData.value.markerInfos.entries.firstOrNull()?.value?.long ?: 0.0
//                        ),
//                        title = "Another"
//                    )
//
//                )
//            } else {
//                listOf()
//            }
            }

            // Log.d("Updating markers, markersData.markerInfos.size: ${markersLoadResult.markerInfos.size}")
            val markers =
                markersLoadResult.markerInfos.map { marker ->
                    MapMarker(
                        key = marker.key,
                        position = LatLong(
                            marker.value.lat,
                            marker.value.long
                        ),
                        title = marker.value.title,
                        alpha = 1.0f
                    )
                }

            mutableStateListOf<MapMarker>().also { snapShot ->
                // Log.d("pre-snapshot markersData.markerInfos.size: ${markersLoadResult.markerInfos.size}")
                snapShot.clear()
                snapShot.addAll(markers)
                cachedMapMarkers.clear()
                cachedMapMarkers.addAll(markers)

                coroutineScope.launch {
                    shouldUpdateMapMarkers = true
                }

                Log.d { "Final map-applied marker count = ${snapShot.size}" }
            }
        }
        val mapBounds by remember(mapMarkers) {
            mutableStateOf(
                mapMarkers.map {
                    it.position
                }.toList()
            )
        }

        // Update user location
        LaunchedEffect(Unit) {

            if (false) {
                // uses a marker to show current location
//                try {
//                    locationService.getCurrentLocation().let {
//                        myLocation = it
//
//                        // Update the marker position
//                        markers = markers.map { marker ->
//                            if (marker.key == "marker4") {
//                                Log.d { "marker4, myLocation = ${myLocation.latitude}, ${myLocation.longitude}" }
//                                MapMarker(
//                                    position = LatLong(
//                                        myLocation.latitude,
//                                        myLocation.longitude
//                                    ),
//                                    key = marker.key,
//                                    title = marker.title
//                                )
//                            } else {
//                                marker
//                            }
//                        }.toMutableList()
//                    }
//                } catch (e: Exception) {
//                    Log.d { "Error: ${e.message}" }
//                }
            }

            // Set the last known location to the current location
            locationService.currentLocation { location ->
//                val locationTemp = Location(
////                    37.422160,
////                    -122.084270 // googleplex
//                    18.976794,
//                    -99.095387 // Tepoztlan
//                )
//                myLocation = locationTemp ?: run { // use defined location
                userLocation = location ?: run { // use live location
                    Log.w { "Error: Unable to get current location" }
                    return@run userLocation // just return the most recent location
                }
            }

            snapshotFlow { userLocation }
                .collect { location ->
//                    Log.d { "location = ${location.latitude}, ${location.longitude}" }

                    // location track marker
//                    markers.clear()
//                    markers.add(
//                        MapMarker(
//                            key = "marker4",
//                            position = LatLong(
//                                location.latitude,
//                                location.longitude
//                            ),
//                            title = "Another"
//                        )
//                    )
                }

//            coroutineScope.launch {
//                // Update the marker position
//                markers = markers.toMutableList().map { marker ->
//                    if (marker.key == "marker4") {
//                        Log.d { "marker4, myLocation = ${myLocation.latitude}, ${myLocation.longitude}" }
//                        MapMarker(
//                            position = LatLong(
//                                myLocation.latitude,
//                                myLocation.longitude
//                            ),
//                            key = marker.key,
//                            title = marker.title
//                        )
//                    } else {
//                        marker
//                    }
//                }
//            }
//
//            // Get heading updates
//            locationService.currentHeading { heading ->
//                heading?.let {
//                    Log.d { "heading = ${it.trueHeading}, ${it.magneticHeading}" }
//                }
//            }
        }

        var isFirstUpdate by remember { mutableStateOf(true) }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

//            Text("Location: ${myLocation.latitude}, ${myLocation.longitude}")
//            Text(json.decodeFromString<Location>(settings.getString(kLastKnownUserLocation, "{latitude:0.0, longitude:0.0}")).toString() )

            if(markersLoadResult.isFinished || !isFirstUpdate) {
                GoogleMaps(
                    modifier = Modifier.fillMaxSize(),
                    markers = mapMarkers.ifEmpty { null },
                    shouldUpdateMapMarkers = shouldUpdateMapMarkers,
                    cameraPosition = remember { CameraPosition(
                        target = LatLong(
                            37.422160,
                            -122.084270  // googleplex
//                            myLocation.latitude, // only sets the initial position, not tracked. Use `myLocation` for tracking.
//                            myLocation.longitude // Todo test that this is coming from the settings intially.
                        ),
                        zoom = 12f  // note: forced zoom level
                    )},
//                    cameraLocationLatLong = remember(myLocation) { LatLong(
////                        37.422160,
////                        -122.084270  // googleplex
//                        myLocation.latitude,
//                        myLocation.longitude
//                    )},
//                    cameraLocationBounds = remember {
//                        CameraLocationBounds(
//                            coordinates = mapBounds,
//                            padding = 80
//                        )
//                    },
                    myLocation = LatLong(
                        userLocation.latitude,
                        userLocation.longitude
                    ),
                )

                isFirstUpdate = false
                shouldUpdateMapMarkers = false
            }
        }
    }
}

expect fun getPlatformName(): String



//    MaterialTheme {
//        var greetingText by remember { mutableStateOf("Hello, World!") }
//        var showImage by remember { mutableStateOf(false) }
//        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
//            Button(onClick = {
//                greetingText = "Hello, ${getPlatformName()}"
//                showImage = !showImage
//            }) {
//                Text(greetingText)
//            }
//            AnimatedVisibility(showImage) {
//                Image(
//                    painterResource("compose-multiplatform.xml"),
//                    null
//                )
//            }
//        }
//    }
