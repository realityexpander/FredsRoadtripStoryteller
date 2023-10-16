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
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import loadMarkers.dump
import loadMarkers.lastKnownUserLocation
import loadMarkers.loadMarkers
import loadMarkers.sampleData.kSunnyvaleFakeDataset
import loadMarkers.sampleData.kUseRealNetwork
import loadMarkers.setLastKnownUserLocation
import co.touchlab.kermit.Logger as Log

val json = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}

const val kMaxReloadDistanceMiles = 2
const val kMaxMarkerCacheAgeSeconds = 60 * 60 * 24 * 30  // 30 days

@Composable
fun App() {

    MaterialTheme {
        val coroutineScope = rememberCoroutineScope()

        val settings = remember {
            Settings().apply {
//                clear()  // Force cache refresh
                // Log.setMinSeverity(Severity.Warn)
                dump()
            }
        }
        val locationService by remember { mutableStateOf(LocationService()) }
        var userLocation: Location by remember {
            mutableStateOf(settings.lastKnownUserLocation())
        }
        val markersLoadResult = loadMarkers(
            settings,
            userLocation = userLocation,
            maxReloadDistanceMiles = kMaxReloadDistanceMiles,
            showLoadingState = false,
            useFakeDataSetId =
                kUseRealNetwork,
//                kSunnyvaleFakeDataset,
            //    kTepoztlanFakeDataset,
            //    kSingleItemPageFakeDataset
        )
        val cachedMapMarkers = remember { mutableStateListOf<MapMarker>() } // prevents flicker when loading new markers
        var shouldUpdateMapMarkers by remember { mutableStateOf(true) }
        val mapMarkers = remember(markersLoadResult.isMarkerPageParseFinished) {

            if (!markersLoadResult.isMarkerPageParseFinished) { // While loading new markers, use the cached markers to prevent flicker
                return@remember cachedMapMarkers
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
        // LEAVE FOR REFERENCE
        //val mapBounds by remember(mapMarkers) {
        //    mutableStateOf(
        //        mapMarkers.map {
        //            it.position
        //        }.toList()
        //    )
        //}

        // Update user location
        LaunchedEffect(Unit) {

            // Set the last known location to the current location
            locationService.currentLocation { location ->
                //    val locationTemp = Location(
                //        37.422160,
                //        -122.084270 // googleplex
                //        // 18.976794,
                //        // -99.095387 // Tepoztlan
                //    )
                //    myLocation = locationTemp ?: run { // use defined location above
                userLocation = location ?: run { // use live location
                    Log.w { "Error: Unable to get current location" }
                    return@run userLocation // just return the most recent location
                }
            }

            snapshotFlow { userLocation }
                .collect { location ->
                    // Log.d { "location = ${location.latitude}, ${location.longitude}" }
                    settings.setLastKnownUserLocation(location)
                }

            // LEAVE FOR REFERENCE
            //    // Get heading updates
            //    locationService.currentHeading { heading ->
            //        heading?.let {
            //            Log.d { "heading = ${it.trueHeading}, ${it.magneticHeading}" }
            //        }
            //    }
        }

        var isFirstUpdate by remember { mutableStateOf(true) }
        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

            if(markersLoadResult.isMarkerPageParseFinished || !isFirstUpdate) {
                GoogleMaps(
                    modifier = Modifier.fillMaxSize(),
                    markers = mapMarkers.ifEmpty { null },
                    shouldUpdateMapMarkers = shouldUpdateMapMarkers,
                    initialCameraPosition = remember { CameraPosition(
                        target = LatLong(
//                            37.422160,
//                            -122.084270  // googleplex
//                            userLocation.latitude, // only sets the initial position, not tracked. Use `userLocation` for tracking.
//                            userLocation.longitude
                            settings.lastKnownUserLocation().latitude,
                            settings.lastKnownUserLocation().longitude
                        ),
                        zoom = 12f  // note: forced zoom level
                    )},
//                    cameraLocationLatLong = remember(myLocation) { LatLong( // track camera to location
////                        37.422160,
////                        -122.084270  // googleplex
//                        userLocation.latitude,
//                        userLocation.longitude
//                    )},
//                    cameraLocationBounds = remember {  // Center around bound of markers
//                        CameraLocationBounds(
//                            coordinates = mapBounds,
//                            padding = 80  // in pixels
//                        )
//                    },
                    userLocation = LatLong( // passed to map to track location
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

// LEAVE FOR REFERENCE
//                // uses a marker to show current location
////                try {
////                    locationService.getCurrentLocation().let {
////                        myLocation = it
////
////                        // Update the marker position
////                        markers = markers.map { marker ->
////                            if (marker.key == "marker4") {
////                                Log.d { "marker4, myLocation = ${myLocation.latitude}, ${myLocation.longitude}" }
////                                MapMarker(
////                                    position = LatLong(
////                                        myLocation.latitude,
////                                        myLocation.longitude
////                                    ),
////                                    key = marker.key,
////                                    title = marker.title
////                                )
////                            } else {
////                                marker
////                            }
////                        }.toMutableList()
////                    }
////                } catch (e: Exception) {
////                    Log.d { "Error: ${e.message}" }
////                }
