import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.History
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
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.Settings
import components.PreviewPlaceholder
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import loadMarkers.LoadingState
import loadMarkers.MarkersResult
import loadMarkers.distanceBetween
import loadMarkers.loadMarkers
import loadMarkers.sampleData.kUseRealNetwork
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import screens.SettingsScreen
import co.touchlab.kermit.Logger as Log

val json = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}

const val kMaxReloadDistanceMiles = 2.0
const val kMaxMarkerCacheAgeSeconds = 60 * 60 * 24 * 30  // 30 days

sealed class BottomSheetScreen {
    data object SettingsScreen : BottomSheetScreen()
    data class MarkerDetailsScreen(val markerId: String) : BottomSheetScreen()
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun App() {

    AppTheme {
        val coroutineScope = rememberCoroutineScope()

        val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
        val scaffoldState = rememberScaffoldState()
        var bottomSheetActiveScreen by remember {
            mutableStateOf<BottomSheetScreen>(BottomSheetScreen.SettingsScreen)
        }

        val settings = remember {
            Settings().apply {
                // clear()  // Force clear all settings & stored data
                // Log.setMinSeverity(Severity.Warn)
                printAppSettings()
            }
        }
        var talkRadiusMiles by remember { mutableStateOf(settings.talkRadiusMiles()) }
        val cachedMarkersLastUpdatedLocation by remember(settings.cachedMarkersLastUpdatedLocation()) {
            mutableStateOf(settings.cachedMarkersLastUpdatedLocation())
        }
        var isMarkersLastUpdatedLocationVisible by remember(settings.isMarkersLastUpdatedLocationVisible()) {
            mutableStateOf(settings.isMarkersLastUpdatedLocationVisible())
        }

        // Google Maps UI elements
        var isTrackingEnabled by remember { mutableStateOf(false) }
        var centerOnUserCameraLocation by remember { mutableStateOf<Location?>(null) } // used to center map on user

        // GPS Location
        val gpsLocationService by remember { mutableStateOf(GPSLocationService()) }
        var userLocation: Location by remember {
            mutableStateOf(settings.lastKnownUserLocation())
        }

        // Recently Seen Markers
        val recentlySeenMarkersSet by remember {
            mutableStateOf(mutableSetOf<RecentMapMarker>())
        }
        val recentlySeenMarkersForUiList by remember {
            mutableStateOf(recentlySeenMarkersSet.toMutableStateList())
        }
        var isRecentlySeenMarkersPanelVisible by remember {
            mutableStateOf(settings.isRecentlySeenMarkersPanelVisible())
        }

        // Error state
        var isShowingError by remember { mutableStateOf<String?>(null) }

        // Load markers
        val markersLoadResult: MarkersResult = loadMarkers(
            settings,
            userLocation = userLocation, // when user location changes, triggers potential load markers from server
            maxReloadDistanceMiles = kMaxReloadDistanceMiles.toInt(),
            showLoadingState = false,
            useFakeDataSetId = kUseRealNetwork,
            //    kSunnyvaleFakeDataset,
            //    kTepoztlanFakeDataset,
            //    kSingleItemPageFakeDataset
        )
        val cachedMapMarkers =
            remember { mutableStateListOf<MapMarker>() } // prevents flicker when loading new markers
        var shouldRedrawMapMarkers by remember { mutableStateOf(true) }
        val mapMarkers = remember(markersLoadResult.isMarkerPageParseFinished) {

            // More pages to load?
            if (!markersLoadResult.isMarkerPageParseFinished) {
                // While loading new markers, use the cached markers to prevent flicker
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
                    shouldRedrawMapMarkers = true
                }

                // Log.d { "Final map-applied marker count = ${snapShot.size}" }
            }
        }
        if (false) {
            // LEAVE FOR REFERENCE
            //val mapBounds by remember(mapMarkers) {
            //    mutableStateOf(
            //        mapMarkers.map {
            //            it.position
            //        }.toList()
            //    )
            //}
        }

        // Update user location & Update Recently Seen Markers
        LaunchedEffect(Unit) {
            // Set the last known location to the current location in settings
            gpsLocationService.onUpdatedGPSLocation(
                errorCallback = { errorMessage ->
                    Log.w("Error: $errorMessage")
                    isShowingError = errorMessage
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
                    isShowingError = "Unable to get current location"
                    Log.w(isShowingError.toString())
                    return@run userLocation // just return the most recent location
                }
                isShowingError = null
            }

            // Save the last known location to settings (using Flow)
            snapshotFlow { userLocation }
                .collect { location ->
                    settings.setLastKnownUserLocation(location)

                    // Check for new markers inside talk radius & add to recentlySeen list
                    mapMarkers.map { marker ->
                        // if marker is within talk radius, add to recently seen list
                        val markerLat = marker.position.latitude
                        val markerLong = marker.position.longitude
                        val distanceFromMarkerToUserLocationMiles = distanceBetween(
                            userLocation.latitude,
                            userLocation.longitude,
                            markerLat,
                            markerLong
                        )

                        fun MutableSet<RecentMapMarker>.containsMarker(marker: MapMarker): Boolean {
                            return recentlySeenMarkersSet.any {
                                it.key() == marker.key
                            }
                        }

                        // add marker to recently seen set?
                        if (distanceFromMarkerToUserLocationMiles < talkRadiusMiles * 1.75) {
                            // Not already in the `seen` set?
                            if (!recentlySeenMarkersSet.containsMarker(marker)) {
                                // Add to the `seen` set
                                val newlySeenMarker = RecentMapMarker(
                                    marker,
                                    Clock.System.now().toEpochMilliseconds(),
                                    recentlySeenMarkersSet.size + 1
                                )
                                recentlySeenMarkersSet.add(newlySeenMarker)
                                recentlySeenMarkersForUiList.add(newlySeenMarker)
                                Log.d("Added Marker ${marker.key} is within talk radius of $talkRadiusMiles miles, distance=$distanceFromMarkerToUserLocationMiles miles, total recentlySeenMarkers=${recentlySeenMarkersSet.size}")

                                // Trim the UI list to 5 items
                                if (recentlySeenMarkersForUiList.size > 5) {
                                    Log.d("Trimming recentlySeenMarkersForUiList.size=${recentlySeenMarkersForUiList.size}")
                                    // remove old markers until there are only 5
                                    do {
                                        val oldestMarker =
                                            recentlySeenMarkersForUiList.minByOrNull { recentMarker ->
                                                recentMarker.timeAddedToRecentList
                                            }

                                        // remove the oldest marker
                                        oldestMarker?.let { oldMarker ->
                                            recentlySeenMarkersForUiList.remove(oldMarker)
                                        }
                                        Log.d("Removed oldest marker, recentlySeenMarkersList.size=${recentlySeenMarkersForUiList.size}")
                                    } while (recentlySeenMarkersForUiList.size > 5)
                                }
                            }
                        }
                    }

                    // Update the UI list
                    val oldList = recentlySeenMarkersForUiList.toList()
                    recentlySeenMarkersForUiList.clear()
                    recentlySeenMarkersForUiList.addAll(oldList.sortedByDescending { recentMarker ->
                        recentMarker.timeAddedToRecentList
                    }.toMutableStateList())
                }

            if (false) {
                // LEAVE FOR REFERENCE
                //    // Get heading updates
                //    locationService.currentHeading { heading ->
                //        heading?.let {
                //            Log.d { "heading = ${it.trueHeading}, ${it.magneticHeading}" }
                //        }
                //    }
            }
        }

        fun startTracking() {
            isTrackingEnabled = true
            gpsLocationService.allowBackgroundLocationUpdates()
        }

        fun stopTracking() {
            isTrackingEnabled = false
            gpsLocationService.preventBackgroundLocationUpdates()
        }

        // Turn on tracking automatically, depending on settings
        LaunchedEffect(Unit) {
            val shouldStart = settings.shouldAutomaticallyStartTrackingWhenAppLaunches()
            if (shouldStart) {
                startTracking()
            }
        }

        BottomSheetScaffold(
            scaffoldState = bottomSheetScaffoldState,
            sheetElevation = 16.dp,
            sheetGesturesEnabled = false, // interferes with map gestures
            sheetPeekHeight = 0.dp,
            sheetContentColor = MaterialTheme.colors.onBackground,
            sheetBackgroundColor = Color.LightGray,
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            sheetContent = {
                when (bottomSheetActiveScreen) {
                    is BottomSheetScreen.SettingsScreen -> {
                        SettingsScreen(
                            settings,
                            bottomSheetScaffoldState,
                            talkRadiusMiles,
                            onTalkRadiusChange = { talkRadiusMiles = it },
                            onShouldShowMarkerDataLastSearchedLocationChange = {
                                isMarkersLastUpdatedLocationVisible = it
                            },
                            onShouldResetMarkerInfoCache = {
                                // Reset Marker Info Cache & reset the `seen markers` list
                                recentlySeenMarkersSet.clear()
                                recentlySeenMarkersForUiList.clear()
                                mapMarkers.clear()
                                cachedMapMarkers.clear()

                                // Trigger a reload of the markers from the server
                                settings.setCachedMarkersLastUpdatedLocation(Location(0.1, 0.1))
                                settings.setCachedMarkersLastUpdatedEpochSeconds(0L)
                                coroutineScope.launch {
                                    delay(50)
                                    userLocation = Location(
                                        userLocation.latitude + 0.001,
                                        userLocation.longitude + 0.001
                                    )
                                }
                            }
                        )
                    }
                    is BottomSheetScreen.MarkerDetailsScreen -> {
                        Text("Marker Details")
                    }
                }
            },
            drawerElevation = 16.dp,
            drawerScrimColor = Color.Black.copy(alpha = 0.5f),
            drawerGesturesEnabled = !bottomSheetScaffoldState.drawerState.isClosed,
            drawerContent = {
                Row(
                    Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Text(
                        "Fred's Talking Markers",
                        fontSize = MaterialTheme.typography.h5.fontSize,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .weight(3f)
                    )
                    IconButton(
                        modifier = Modifier
                            .offset(16.dp, (-16).dp),
                        onClick = {
                            coroutineScope.launch {
                                bottomSheetScaffoldState.drawerState.close()
                            }
                        }) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close"
                        )
                    }
                }
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
                                            if (isClosed) open() else close()
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
                                        bottomSheetActiveScreen = BottomSheetScreen.SettingsScreen
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
                                        isRecentlySeenMarkersPanelVisible =
                                            !isRecentlySeenMarkersPanelVisible
                                        settings.setIsRecentlySeenMarkersPanelVisible(
                                            isRecentlySeenMarkersPanelVisible
                                        )
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
            ) {
                val transitionRecentMarkersPanel: Float by animateFloatAsState(
                    if (isRecentlySeenMarkersPanelVisible) 0.5f else 0f,
                    animationSpec = tween(500)
                )

                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AnimatedVisibility(isShowingError != null) {
                            Text(
                                modifier = Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colors.error),
                                text = "Error: $isShowingError",
                                textAlign = TextAlign.Center,
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colors.onError
                            )
                        }

                        // Show Map
                        val didMapMarkersUpdate =
                            MapContent(
                                modifier = Modifier
                                    .fillMaxHeight(1.0f - transitionRecentMarkersPanel),
                                isFinishedLoadingMarkerData = markersLoadResult.isMarkerPageParseFinished,
                                initialUserLocation = settings.lastKnownUserLocation(),
                                userLocation = userLocation,
                                mapMarkers = mapMarkers,
                                mapBounds = null,
                                shouldRedrawMapMarkers = shouldRedrawMapMarkers, // redraw the map & markers
                                isTrackingEnabled = isTrackingEnabled,
                                centerOnUserCameraLocation = centerOnUserCameraLocation,
                                talkRadiusMiles = talkRadiusMiles,
                                cachedMarkersLastUpdatedLocation =
                                remember(
                                    settings.isMarkersLastUpdatedLocationVisible(),
                                    cachedMarkersLastUpdatedLocation
                                ) {
                                    if (settings.isMarkersLastUpdatedLocationVisible())
                                        cachedMarkersLastUpdatedLocation
                                    else
                                        null
                                },
                                onToggleIsTrackingEnabled = {
                                    isTrackingEnabled = !isTrackingEnabled
                                    coroutineScope.launch {
                                        if (isTrackingEnabled) {
                                            startTracking()
                                        } else {
                                            stopTracking()
                                        }
                                    }
                                },
                                onFindMeButtonClicked = {
                                    // center on location
                                    centerOnUserCameraLocation = userLocation.copy()
                                },
                                isMarkersLastUpdatedLocationVisible = isMarkersLastUpdatedLocationVisible,
                                isMapOptionSwitchesVisible = !isRecentlySeenMarkersPanelVisible  // hide map options when showing marker list
                            )
                        if (didMapMarkersUpdate) {
                            shouldRedrawMapMarkers =
                                false  // The map has been updated, so don't redraw it again.
                        }
                    }

                    // Show `recently seen markers` list
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(MaterialTheme.colors.background),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        LazyColumn(
                            userScrollEnabled = true,
                        ) {
                            item {
                                Text(
                                    text = "Recently Seen Markers",
                                    color = MaterialTheme.colors.onBackground,
                                    fontStyle = FontStyle.Normal,
                                    fontSize = MaterialTheme.typography.h5.fontSize,
                                    fontWeight = FontWeight.Medium,
                                    textAlign = TextAlign.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(start = 8.dp)
                                )
                            }
                            if (recentlySeenMarkersForUiList.isEmpty()) {
                                item {

                                    Spacer(modifier = Modifier.height(48.dp))
                                    Text(
                                        text = "No recently seen markers, drive around to see some!",
                                        color = MaterialTheme.colors.onBackground,
                                        fontStyle = FontStyle.Normal,
                                        fontSize = MaterialTheme.typography.h6.fontSize,
                                        fontWeight = FontWeight.Medium,
                                        textAlign = TextAlign.Center,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(start = 8.dp)
                                    )
                                }
                            }

                            items(recentlySeenMarkersForUiList.size) {
                                val marker = recentlySeenMarkersForUiList.elementAt(it)
//                                    val marker = RecentMapMarker(
//                                        MapMarker(
//                                            key = "marker1",
//                                            position = LatLong(
//                                                37.422160,
//                                                -122.084270
//                                            ),
//                                            title = "Googleplex",
//                                            alpha = 1.0f
//                                        ),
//                                        Clock.System.now().toEpochMilliseconds()
//                                    )
                                Text(
                                    text = marker.seenOrder.toString() + ":" + marker.key() + ":" + marker.marker.title,
                                    color = MaterialTheme.colors.onBackground,
                                    fontStyle = FontStyle.Normal,
                                    fontSize = MaterialTheme.typography.h6.fontSize,
                                    fontWeight = FontWeight.Medium,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(
                                            start = 8.dp,
                                            top = 0.dp,
                                            bottom = 8.dp,
                                            end = 8.dp
                                        )
                                        .border(
                                            width = 1.dp,
                                            color = MaterialTheme.colors.onBackground,
                                            shape = RoundedCornerShape(8.dp)
                                        )
                                        .heightIn(min = 48.dp)
                                        .padding(8.dp, top = 0.dp, bottom = 4.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun MapContent(
    modifier: Modifier = Modifier,
    isFinishedLoadingMarkerData: Boolean = false,  // only sets the initial position, not tracked. Use `userLocation` for tracking.
    initialUserLocation: Location,
    userLocation: Location,
    mapMarkers: List<MapMarker>,
    mapBounds: List<LatLong>? = null,
    shouldRedrawMapMarkers: Boolean,
    isTrackingEnabled: Boolean = false,
    centerOnUserCameraLocation: Location? = null,
    talkRadiusMiles: Double = .5,
    cachedMarkersLastUpdatedLocation: Location? = null,
    onToggleIsTrackingEnabled: (() -> Unit)? = null,
    onFindMeButtonClicked: (() -> Unit)? = null,
    isMarkersLastUpdatedLocationVisible: Boolean = false,
    isMapOptionSwitchesVisible: Boolean = true
): Boolean {
    var didMapMarkersUpdate by remember(shouldRedrawMapMarkers) { mutableStateOf(true) }
    var isFirstUpdate by remember { mutableStateOf(true) } // force map to update at least once

    Column(Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
        if (isFinishedLoadingMarkerData || !isFirstUpdate) {
            GoogleMaps(
                modifier = modifier,
                isTrackingEnabled = isTrackingEnabled,
                userLocation = LatLong( // passed to map to track location
                    userLocation.latitude,
                    userLocation.longitude
                ),
                markers = mapMarkers.ifEmpty { null },
                shouldRedrawMapMarkers = shouldRedrawMapMarkers,
                cameraOnetimePosition =
                    if (isFirstUpdate) {  // set initial camera position
                        CameraPosition(
                            target = LatLong(
                                initialUserLocation.latitude,
                                initialUserLocation.longitude
                            ),
                            zoom = 12f  // note: forced zoom level
                        )
                    } else
                        null,
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
                talkRadiusMiles = talkRadiusMiles,
                cachedMarkersLastUpdatedLocation = cachedMarkersLastUpdatedLocation,
                onToggleIsTrackingEnabledClick = onToggleIsTrackingEnabled,
                onFindMeButtonClick = onFindMeButtonClicked,
                isMarkersLastUpdatedLocationVisible = isMarkersLastUpdatedLocationVisible,
                isMapOptionSwitchesVisible = isMapOptionSwitchesVisible
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


@OptIn(ExperimentalResourceApi::class)
@Composable
fun SplashScreenForPermissions(
    isPermissionsGranted: Boolean = false,
) {
    if(isPermissionsGranted) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.primary)
        )
        return
    }


    Column(
        modifier = Modifier
//            .heightIn(min = 48.dp, max=300.dp)
            .background(MaterialTheme.colors.primary),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .weight(1f)
                ,
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if(LocalInspectionMode.current) {
                    PreviewPlaceholder(
                        "Freds head",
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .background(Color.Red)
                    )
                } else {
                    Image(
                        painter = painterResource("fred-head-owl-1.png"),
                        contentDescription = null,
                        modifier = Modifier
                            .weight(1f),
                        contentScale = ContentScale.FillWidth,
                    )
                }
    //            PreviewPlaceholder("Freds head",
    //                modifier = Modifier
    //                    .weight(1f)
    //                    .fillMaxHeight()
    //                    .background(Color.Red)
    //            )
                Text(
                    "Fred's Historic Markers",
                    color = Color.White,
                    textAlign = TextAlign.Center,
                    fontSize = MaterialTheme.typography.h3.fontSize,
                    modifier = Modifier.weight(1f)
                )
            }
            Spacer(modifier = Modifier.weight(2.5f))
    }
}

//@Preview
//@Composable
//fun Test() {
//    Text("Hello, World!")
//}
