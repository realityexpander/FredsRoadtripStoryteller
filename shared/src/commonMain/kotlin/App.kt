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
import org.jetbrains.compose.resources.ExperimentalResourceApi

@OptIn(ExperimentalResourceApi::class)
@Composable
fun App() {
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

    MaterialTheme {
        var greetingText by remember { mutableStateOf("Hello, World!") }
        var showImage by remember { mutableStateOf(false) }

        var myLocation by remember {
            mutableStateOf(
                Location(0.0, 0.0)
            )
        }
        val locationService by remember { mutableStateOf(LocationService()) }
        val coroutineScope = rememberCoroutineScope()

        var markersData = loadMarkersFromHtml()
        var markers = remember(markersData.value.isFinished) {

            if (!markersData.value.isFinished) {
                return@remember listOf()
            }

            if (false) {
//                println("marker count = ${markersData.value.markerInfos.size}")
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
                markersData.value.markerInfos.map { marker ->
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
                println("marker count = ${snapShot.size}")
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
            if (true) {
//                try {
//                    locationService.getCurrentLocation().let {
//                        myLocation = it
//
//                        // Update the marker position
//                        markers = markers.map { marker ->
//                            if (marker.key == "marker4") {
//                                println("marker4, myLocation = ${myLocation.latitude}, ${myLocation.longitude}")
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
//                    println("Error: ${e.message}")
//                }
            }

            locationService.currentLocation { location ->
                myLocation = location ?: run {
                    println("Error: Unable to get current location");
                    return@run myLocation // just return the most recent location
                }
            }

            snapshotFlow { myLocation }
                .collect { location ->
//                    println("location = ${location.latitude}, ${location.longitude}")

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
//                            println("marker4, myLocation = ${myLocation.latitude}, ${myLocation.longitude}")
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
//                        println("marker4, myLocation = ${myLocation.latitude}, ${myLocation.longitude}")
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
//                    println("heading = ${it.trueHeading}, ${it.magneticHeading}")
//                }
//            }
        }

//        LaunchedEffect(myLocation) {
//            // Update the marker position
//            markers = markers.toMutableList().map { marker ->
//                if (marker.key == "marker4") {
//                    println("marker4, myLocation = ${myLocation.latitude}, ${myLocation.longitude}")
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

            if (markersData.value.isFinished) {
                GoogleMaps(
                    modifier = Modifier.fillMaxSize(),
                    markers = markers,
                    cameraPosition = remember { CameraPosition(
                        target = LatLong(
                            37.422160,
                            -122.084270
//                            myLocation.latitude,
//                            myLocation.longitude
                        ),
                        zoom = 12f  // note: forced zoom level
                    )},
//                    cameraLocationLatLong = remember(myLocation) { LatLong(
////                        37.422160,
////                        -122.084270
//                        myLocation.latitude,
//                        myLocation.longitude
//                    )},
//                    cameraLocationBounds = remember {
//                        CameraLocationBounds(
//                            coordinates = mapBounds,
//                            padding = 80
//                        )
//                    },
//                    myLocation = remember(myLocation) { LatLong(
//                        myLocation.latitude,
//                        myLocation.longitude
//                    )},
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
