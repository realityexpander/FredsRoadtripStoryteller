import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
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
import kotlinx.serialization.json.Json
import co.touchlab.kermit.Logger as Log

val json = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}


const val kMaxReloadDistanceMiles = 2

@Composable
fun App() {

    MaterialTheme {
        val coroutineScope = rememberCoroutineScope()

        var greetingText by remember { mutableStateOf("Hello, World!") }
        var showImage by remember { mutableStateOf(false) }

        val settings = remember {
            Settings().apply {
                clear()  // Force cache refresh // todo test location cache from start.
//                Log.setMinSeverity(Severity.Warn)
                Log.d { "keys from settings: $keys" }

                // Show current settings
                // Log.d(getString("cachedMarkers", "[]"))
                Log.d(getLong("cachedMarkersLastUpdatedEpochSeconds", 0L).toString())
                Log.d(getString("cachedMarkersLastLocationLatLong", "{latitude:0.0, longitude:0.0}"))
            }
        }

        val locationService by remember { mutableStateOf(LocationService()) }
        var myLocation: Location by remember {
            mutableStateOf(
                // Load last known location
                if(settings.contains("LastKnownUserLocation")) {
                    val latLong = json.decodeFromString<Location>(
                        settings.getString("LastKnownUserLocation",
                            "{latitude:0.0, longitude:0.0}")
                    )
                    Location(latLong.latitude, latLong.longitude)
                } else {
                    // Default to UNKNOWN? // todo - use a default location
                    Location(0.0, 0.0)
                }
            )
        }


        val markersData = loadMarkers(
            settings,
            myLocation = myLocation,
            maxReloadDistanceMiles = kMaxReloadDistanceMiles,
            showLoadingState = true,
            useFakeDataSetId = 0  // 0 = real data, 1 = Googleplex, 2 = Tepoztlan,
        )

//        // Load markers
//        val markersData = remember(myLocation) {
//            mutableStateOf(
//                loadMarkers(
//                    settings,
//                    myLocationLatLong = LatLong(myLocation.latitude, myLocation.longitude),
//                    maxReloadDistanceMiles = kMaxReloadDistanceMiles,
//                    showLoadingState = true,
//                    useFakeDataSetId = 1  // 0 = real data, 1 = Googleplex, 2 = Tepoztlan,
//                )
//            )
//        }

        var markerLoad = remember {
            derivedStateOf { markersData.isFinished }
        }
        val markers = remember(markersData.isFinished) {

            if (!markersData.isFinished) {
                return@remember listOf()
            }

            if (false) {
//                Log.i { "marker count = ${markersData.value.markerInfos.size}")
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

            val markers =
                markersData.markerInfos.map { marker ->
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
                snapShot.clear()
                snapShot.addAll(markers)
                Log.i { "marker count = ${snapShot.size}" }
            }
        }
        val mapBounds by remember(markers) {
            mutableStateOf(
                markers.map {
                    it.position
                }.toList()
            )
        }

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
//                                Log.i { "marker4, myLocation = ${myLocation.latitude}, ${myLocation.longitude}" }
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
//                    Log.i { "Error: ${e.message}" }
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
                myLocation = location ?: run { // use live location
                    Log.i { "Error: Unable to get current location" }
                    return@run myLocation // just return the most recent location
                }

//                // Update the last known location & load new markers if needed
//                coroutineScope.launch {
//                    settings["LastKnownUserLocation"] =
//                        json.encodeToString(LatLongSerializer, LatLong(myLocation.latitude, myLocation.longitude))
//
//                    // check if outside the MaxReloadDistanceMiles to load new markers
//                    val cachedMarkersLastLocationLatLong =
//                        json.decodeFromString<Location>(
//                            settings.getString("cachedMarkersLastLocationLatLong", "{lat:0.0, long:0.0}")
//                        )
//                    println("cachedMarkersLastLocationLatLong: $cachedMarkersLastLocationLatLong")
//                    val distance = distanceBetween(
//                        myLocation.latitude,
//                        myLocation.longitude,
//                        cachedMarkersLastLocationLatLong.latitude,
//                        cachedMarkersLastLocationLatLong.longitude
//                    )
//                    println("distanceBetween myLocation and cachedMarkersLastLocationLatLong: $distance")
//                    if(distance > kMaxReloadDistanceMiles) {
//                        println("distance > MaxReloadDistanceMiles")
//                        settings["cachedMarkersLastLocationLatLong"] =
//                            json.encodeToString(Location(myLocation.latitude, myLocation.longitude))
//                        settings["cachedMarkersLastUpdatedEpochSeconds"] = Clock.System.now().epochSeconds
//
//                        markersData = markersData.copy(isFinished = false) // trigger reload
//                    }
//
//                }
            }

            snapshotFlow { myLocation }
                .collect { location ->
//                    Log.i { "location = ${location.latitude}, ${location.longitude}" }

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

//                    markers = markers.map { marker ->
//                        if (marker.key == "marker4") {
//                            Log.i { "marker4, myLocation = ${myLocation.latitude}, ${myLocation.longitude}" }
//                            MapMarker(
//                                position = LatLong(
//                                    myLocation.latitude,
//                                    myLocation.longitude
//                                ),
//                                key = marker.key,
//                                title = marker.title
//                            )
//                        } else {
//                            marker
//                        }
//                    }.toMutableList()
                }

//            coroutineScope.launch {
//                // Update the marker position
//                markers = markers.toMutableList().map { marker ->
//                    if (marker.key == "marker4") {
//                        Log.i { "marker4, myLocation = ${myLocation.latitude}, ${myLocation.longitude}" }
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
//                    Log.i { "heading = ${it.trueHeading}, ${it.magneticHeading}" }
//                }
//            }
        }

//        LaunchedEffect(myLocation) {
//            // Update the marker position
//            markers = markers.toMutableList().map { marker ->
//                if (marker.key == "marker4") {
//                    Log.i { "marker4, myLocation = ${myLocation.latitude}, ${myLocation.longitude}" }
//                    MapMarker(
//                        position = LatLong(
//                            myLocation.latitude,
//                            myLocation.longitude
//                        ),
//                        key = marker.key,
//                        title = marker.title
//                    )
//                } else {
//                    marker
//                }
//            }
//        }

        Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {

//            Text("Location: ${myLocation.latitude}, ${myLocation.longitude}")
//            Text(json.decodeFromString<Location>(settings.getString("LastKnownUserLocation", "{latitude:0.0, longitude:0.0}")).toString() )

            if (markersData.isFinished) {
                GoogleMaps(
                    modifier = Modifier.fillMaxSize(),
                    markers = markers,
                    cameraPosition = remember { CameraPosition(
                        target = LatLong(
                            37.422160,
                            -122.084270  // googleplex
//                            myLocation.latitude, // only sets the initial position, not tracked. Use `myLocation` for tracking.
//                            myLocation.longitude
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
                        myLocation.latitude,
                        myLocation.longitude
                    ),
                )
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
