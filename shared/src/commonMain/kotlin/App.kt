import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddRoad
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.rememberBottomSheetScaffoldState
import androidx.compose.material.rememberScaffoldState
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.Settings
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import loadMarkers.printAppSettings
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

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun App() {

    MaterialTheme {
        val coroutineScope = rememberCoroutineScope()

        val bottomSheetState = rememberBottomSheetScaffoldState()
        val scaffoldState = rememberScaffoldState()

        val settings = remember {
            Settings().apply {
//                clear()  // Force clear all settings
                // Log.setMinSeverity(Severity.Warn)
                printAppSettings()
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
            //    kSunnyvaleFakeDataset,
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

            // Experimenting with flows - LEAVE FOR REFERENCE
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

        BottomSheetScaffold(
            scaffoldState = bottomSheetState,
            sheetPeekHeight = 0.dp,
            sheetContent = { /* show bottom sheet content for a tapped selected marker*/ },
        ) {
            Scaffold(
                scaffoldState = scaffoldState,
                topBar = {
                    TopAppBar(
                        navigationIcon = {
                            IconButton(onClick = {
                                coroutineScope.launch {
                                    bottomSheetState.bottomSheetState.apply { if (isCollapsed) expand() else collapse() }
                                }
                            }) {
                                Icon(
                                    imageVector = Icons.Default.Menu,
                                    contentDescription = "Settings"
                                )
                            }
                        },
                        title = {
                            Text(
                                text = "Fred's Markers",
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.Medium
                            )
                        },
                        actions = {
                            IconButton(onClick = {}) {// viewModel.onEvent(MapEvent.OnClickDrawRoute) }) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Settings"
                                )
                            }
                            IconButton(onClick = {}) {//  viewModel.onEvent(MapEvent.OnResetMap) }) {
                                Icon(
                                    imageVector = Icons.Default.Info,
                                    contentDescription = "Hide/Show Marker List"
                                )
                            }
                        }
                    )
                },
                /*
                floatingActionButton = {
                    FloatingActionButton(
                        onClick = { viewModel.onEvent(MapEvent.ToggleCustomMap) }) {
                        Icon(
                            imageVector = if (viewModel.uiState.isCustomMap)
                                Icons.Default.ToggleOff else Icons.Default.ToggleOn,
                            contentDescription = "Toggle Custom View"
                        )
                    }
                }
                 */
            ) {
                val didMapMarkersUpdate =
                    MapContent(
                        isFinishedLoadingMarkerData = markersLoadResult.isMarkerPageParseFinished,
                        initialUserLocation = settings.lastKnownUserLocation(),
                        userLocation = userLocation,
                        mapMarkers = mapMarkers,
                        mapBounds = null,  // todo - implement?
                        shouldUpdateMapMarkers = shouldUpdateMapMarkers,
                    )
                if (didMapMarkersUpdate) {
                    shouldUpdateMapMarkers = false
                }
            }
        }
    }
}

@Composable
fun MapContent(
    isFinishedLoadingMarkerData: Boolean = false,
    initialUserLocation: Location,  // only sets the initial position, not tracked. Use `userLocation` for tracking.
    userLocation: Location,
    mapMarkers: List<MapMarker>,
    mapBounds: List<LatLong>? = null,
    shouldUpdateMapMarkers: Boolean,
): Boolean {
    var didMapMarkersUpdate by remember(shouldUpdateMapMarkers) { mutableStateOf(true) }
    var isFirstUpdate by remember { mutableStateOf(true) } // force map to update at least once

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if(isFinishedLoadingMarkerData || !isFirstUpdate) {
            GoogleMaps(
                modifier = Modifier.fillMaxSize(),
                markers = mapMarkers.ifEmpty { null },
                shouldUpdateMapMarkers = shouldUpdateMapMarkers,
                initialCameraPosition = remember { CameraPosition(
                    target = LatLong(
//                            37.422160,
//                            -122.084270  // googleplex
                        initialUserLocation.latitude,
                        initialUserLocation.longitude
                    ),
                    zoom = 12f  // note: forced zoom level
                )},
//                    cameraLocationLatLong = remember(myLocation) { LatLong( // track camera to location w/o zoom
////                        37.422160,
////                        -122.084270  // googleplex
//                        userLocation.latitude,
//                        userLocation.longitude
//                    )},
                cameraLocationBounds = remember {  // Center around bound of markers
                    mapBounds?.let {
                        CameraLocationBounds(
                            coordinates = mapBounds,
                            padding = 80  // in pixels
                        )
                    } ?: run {
                        null // won't center around bounds
                    }
                },
                userLocation = LatLong( // passed to map to track location
                    userLocation.latitude,
                    userLocation.longitude
                ),
            )

            isFirstUpdate = false
            didMapMarkersUpdate = false
        }
    }

    return didMapMarkersUpdate
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
