import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.scrollable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.Button
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.Settings
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import loadMarkers.LoadingState
import loadMarkers.MarkersResult
import loadMarkers.loadMarkers
import loadMarkers.sampleData.kUseRealNetwork
import co.touchlab.kermit.Logger as Log

val json = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}

const val kMaxReloadDistanceMiles = 2
const val kTalkRadiusMiles = 0.5
const val kMaxMarkerCacheAgeSeconds = 60 * 60 * 24 * 30  // 30 days

sealed class BottomSheetScreen {
    data object None : BottomSheetScreen()
    data object Settings : BottomSheetScreen()
    data class MarkerDetails(val markerId: String) : BottomSheetScreen()
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun App() {

    MaterialTheme {
        val coroutineScope = rememberCoroutineScope()

        val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
        val scaffoldState = rememberScaffoldState()
        var bottomSheetScreen by remember { mutableStateOf<BottomSheetScreen>(BottomSheetScreen.None) }

        var isTrackingEnabled by remember { mutableStateOf(false) }
        var findMeCameraLocation by remember { mutableStateOf<Location?>(null) } // used to center map on user

        val settings = remember {
            Settings().apply {
//                clear()  // Force clear all settings
                // Log.setMinSeverity(Severity.Warn)
                printAppSettings()
            }
        }
        val gpsLocationService by remember { mutableStateOf(GPSLocationService()) }
        var userLocation: Location by remember {
            mutableStateOf(settings.lastKnownUserLocation())
        }
        val markersLoadResult: MarkersResult = loadMarkers(
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
        val cachedMapMarkers =
            remember { mutableStateListOf<MapMarker>() } // prevents flicker when loading new markers
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
            gpsLocationService.onUpdatedGPSLocation(
                errorCallback = { error ->
                    Log.w("Error: $error")
                }
            ) { location ->
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

                // todo check for new markers inside talk radius & add to recentlySeen list
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
            scaffoldState = bottomSheetScaffoldState,
            sheetPeekHeight = 0.dp,
            sheetContentColor = MaterialTheme.colors.onBackground,
            sheetBackgroundColor = MaterialTheme.colors.background,
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            sheetContent = {
                when(bottomSheetScreen) {
                    BottomSheetScreen.None -> {
                        Text("None")
                    }
                    is BottomSheetScreen.Settings -> {
                        val scrollState = rememberScrollState()
                        Column(
                            Modifier.fillMaxWidth()
                                .padding(16.dp)
                                .scrollable(scrollState, orientation = Orientation.Vertical)
                            ,
                            horizontalAlignment = Alignment.Start,
                        ) {
                            Text("Settings")

                            Button(onClick = {
                                coroutineScope.launch {
                                    bottomSheetScaffoldState.bottomSheetState.collapse()
                                }
                            }) {
                                Text("Save")
                            }
                        }
                    }
                    is BottomSheetScreen.MarkerDetails -> {
                        Text("Marker Details")
                    }
                }
            },
            drawerContent =  {
                Text("Application Menu")
            }
        ) {
            Scaffold(
                scaffoldState = scaffoldState,
                topBar = {
                    Column(
                        Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TopAppBar(
                            navigationIcon = {
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        bottomSheetScaffoldState.drawerState.apply {
                                            if(isClosed) open() else close()
                                        }
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
                                // Settings
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        bottomSheetScreen = BottomSheetScreen.Settings
                                        bottomSheetScaffoldState.bottomSheetState.apply {
                                            if (isCollapsed) expand() else collapse()
                                        }
                                    }
                                }) { // show settings page
                                    Icon(
                                        imageVector = Icons.Default.Settings,
                                        contentDescription = "Settings"
                                    )
                                }

                                // Recent Markers History List
                                IconButton(onClick = {
                                    coroutineScope.launch {
                                        bottomSheetScreen = BottomSheetScreen.MarkerDetails("markerId")
                                        bottomSheetScaffoldState.bottomSheetState.apply {
                                            if (isCollapsed) expand() else collapse()
                                        }
                                    }
                                }) { // show marker history panel
                                    Icon(
                                        imageVector = Icons.Default.History,
                                        contentDescription = "Hide/Show Marker List"
                                    )
                                }
                            }
                        )

                        // Show loading error
                        AnimatedVisibility(
                            visible = markersLoadResult.loadingState is LoadingState.Error,
                        ) {
                            if (markersLoadResult.loadingState is LoadingState.Error) {
                                Text(
                                    modifier = Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colors.error),
                                    text = "Error: ${markersLoadResult.loadingState.errorMessage}",
                                    fontStyle = FontStyle.Normal,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colors.onError
                                )
                            }
                        }
                    }
                },
                floatingActionButton = {
                    Column {
                        FloatingActionButton(
                            modifier = Modifier
                                .padding(16.dp),
                            onClick = {
                                isTrackingEnabled = !isTrackingEnabled
                                coroutineScope.launch {
                                    if (isTrackingEnabled) {
                                        gpsLocationService.allowBackgroundLocationUpdates()
                                    } else {
                                        gpsLocationService.preventBackgroundLocationUpdates()
                                    }
                                }
                            }) {
                            Icon(
                                imageVector = if (isTrackingEnabled)
                                    Icons.Default.Pause
                                else Icons.Default.PlayArrow,
                                contentDescription = "Toggle track your location"
                            )
                        }

                        FloatingActionButton(
                            modifier = Modifier
                                .padding(16.dp),
                            onClick = {
                                // center on location
                                findMeCameraLocation = userLocation.copy()
                            }) {
                            Icon(
                                imageVector = Icons.Default.MyLocation,
                                contentDescription = "Center on your location"
                            )
                        }
                    }
                }
            ) {
                val didMapMarkersUpdate =
                    MapContent(
                        isFinishedLoadingMarkerData = markersLoadResult.isMarkerPageParseFinished,
                        initialUserLocation = settings.lastKnownUserLocation(),
                        userLocation = userLocation,
                        mapMarkers = mapMarkers,
                        mapBounds = null,
                        shouldUpdateMapMarkers = shouldUpdateMapMarkers,  // todo - implement?
                        isTrackingEnabled = isTrackingEnabled,
                        centerOnUserCameraLocation = findMeCameraLocation
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
    isTrackingEnabled: Boolean = false,
    centerOnUserCameraLocation: Location? = null,
): Boolean {
    var didMapMarkersUpdate by remember(shouldUpdateMapMarkers) { mutableStateOf(true) }
    var isFirstUpdate by remember { mutableStateOf(true) } // force map to update at least once

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (isFinishedLoadingMarkerData || !isFirstUpdate) {
            GoogleMaps(
                modifier = Modifier.fillMaxSize(),
                isTrackingEnabled = isTrackingEnabled,
                userLocation = LatLong( // passed to map to track location
                    userLocation.latitude,
                    userLocation.longitude
                ),
                markers = mapMarkers.ifEmpty { null },
                cameraLocationLatLong = remember(centerOnUserCameraLocation) {
                    // 37.422160,
                    // -122.084270  // googleplex
                    centerOnUserCameraLocation?.let {
                        LatLong(
                            centerOnUserCameraLocation.latitude,
                            centerOnUserCameraLocation.longitude
                        )
                    } ?: run {
                        null
                    }
                },
                shouldUpdateMapMarkers = shouldUpdateMapMarkers,
                cameraOnetimePosition = remember {
                    CameraPosition(
                        target = LatLong(
//                            37.422160,
//                            -122.084270  // googleplex
                            initialUserLocation.latitude,
                            initialUserLocation.longitude
                        ),
                        zoom = 12f  // note: forced zoom level
                    )
                },
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
