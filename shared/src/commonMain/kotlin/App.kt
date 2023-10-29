import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
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
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudDownload
import androidx.compose.material.icons.filled.CloudOff
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
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.russhwolf.settings.Settings
import data.LoadingState
import data.clearMarkersLastUpdateEpochSeconds
import data.clearMarkersLastUpdatedLocation
import data.clearMarkersResult
import data.isAutomaticStartBackgroundUpdatesWhenAppLaunchTurnedOn
import data.isMarkersLastUpdatedLocationVisible
import data.isRecentlySeenMarkersPanelVisible
import data.lastKnownUserLocation
import data.loadMarkerDetails.loadMapMarkerDetails
import data.loadMarkers.MarkersResult
import data.loadMarkers.distanceBetween
import data.loadMarkers.loadMarkers
import data.loadMarkers.sampleData.kUseRealNetwork
import data.markersLastUpdatedLocation
import data.printAppSettings
import data.setIsRecentlySeenMarkersPanelVisible
import data.setLastKnownUserLocation
import data.setMarkersResult
import data.talkRadiusMiles
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import maps.CameraLocationBounds
import maps.CameraPosition
import maps.LatLong
import maps.MapMarker
import maps.MarkerIdStr
import maps.RecentMapMarker
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import presentation.MarkerDetailsScreen
import presentation.SettingsScreen
import presentation.app.AppDrawerContent
import presentation.app.AppTheme
import presentation.app.RecentlySeenMarkers
import presentation.uiComponents.PreviewPlaceholder
import kotlin.random.Random
import co.touchlab.kermit.Logger as Log

val json = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}

const val kForceClearSettingsAtLaunch = false
const val kMaxReloadDistanceMiles = 2.0
const val kMaxMarkerCacheAgeSeconds = 60 * 60 * 24 * 30  // 30 days

sealed class BottomSheetScreen {
    data object SettingsScreen : BottomSheetScreen()

    // Can pass in a marker or just an id
    data class MarkerDetailsScreen(val marker: MapMarker? = null, val id: String? = marker?.id) : BottomSheetScreen()
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

        // Settings
        val settings = remember {
            Settings().apply {
                if(kForceClearSettingsAtLaunch) { clear() }
                // Log.setMinSeverity(Severity.Warn)
                printAppSettings()
            }
        }
        var talkRadiusMiles by remember { mutableStateOf(settings.talkRadiusMiles()) }
        var isMarkersLastUpdatedLocationVisible by remember(settings.isMarkersLastUpdatedLocationVisible()) {
            mutableStateOf(settings.isMarkersLastUpdatedLocationVisible())
        }

        // Google Maps UI elements
        var isTrackingEnabled by remember { mutableStateOf(false) }
        var centerOnUserCameraLocation by remember { mutableStateOf<Location?>(null) } // used to center map on user

        // GPS Location Service
        val gpsLocationService by remember { mutableStateOf(GPSLocationService()) }
        var userLocation: Location by remember { mutableStateOf(settings.lastKnownUserLocation()) }

        // Recently Seen Markers
        val recentlySeenMarkersSet by remember {
            mutableStateOf(mutableSetOf<RecentMapMarker>()) // todo add to settings
        }
        val recentlySeenMarkersForUiList by remember {
            mutableStateOf(recentlySeenMarkersSet.toMutableStateList())
        }
        var isRecentlySeenMarkersPanelVisible by remember {
            mutableStateOf(settings.isRecentlySeenMarkersPanelVisible())
        }

        // Error Message state & value
        var isShowingError by remember { mutableStateOf<String?>(null) }

        // Last "markers updated" location
        var cachedMarkersLastUpdatedLocation by remember {
            mutableStateOf(settings.markersLastUpdatedLocation())
        }

        // Holds the set of saved markers, this prevents flicker when loading new markers while processing the marker page(s)
        val previousMapMarkers = remember { mutableStateListOf<MapMarker>() }

        // Load markers
        var loadingStateIcon: ImageVector by remember { mutableStateOf(Icons.Default.CloudDownload) }
        var fetchedMarkersResult: MarkersResult =
            loadMarkers(
                settings,
                userLocation = userLocation, // when user location changes, triggers potential load markers from server
                maxReloadDistanceMiles = kMaxReloadDistanceMiles.toInt(),
                onSetMarkersLastUpdatedLocation = { location ->
                    // Update the UI with the latest location
                    cachedMarkersLastUpdatedLocation = location
                },
                showLoadingState = false,
                useFakeDataSetId = kUseRealNetwork,
                //    kSunnyvaleFakeDataset,
                //    kTepoztlanFakeDataset,
                //    kSingleItemPageFakeDataset,
                onLoadingStateChange = {
                    // Update the UI with the latest loading state
                    Log.d { "Loading state changed: $it" }
                    loadingStateIcon = when (it) {
                        is LoadingState.Loading -> {
                            Icons.Default.CloudDownload
                        }
                        is LoadingState.Loaded -> {
                            Icons.Default.CloudDownload
                        }
                        is LoadingState.Finished -> {
                            Icons.Default.Cloud
                        }
                        is LoadingState.Error -> {
                            Icons.Default.CloudOff
                        }
                    }
                }
            )

        var shouldRedrawMapMarkers by remember { mutableStateOf(true) }

        // Update the markers AFTER the page has finished parsing
        val mapMarkers = remember(fetchedMarkersResult.isParseMarkersPageFinished) {
            // More pages to load?
            if (!fetchedMarkersResult.isParseMarkersPageFinished) {
                // While loading new markers, use the cached markers to prevent flicker
                return@remember previousMapMarkers
            }

            // Update the markers list with the latest marker data. (after it's loaded)
            // todo check that other data is not wiped out (marker details)
            val fetchedMarkers =
                fetchedMarkersResult.markerIdToMapMarkerMap.map { marker ->
                    marker.value
                }

            // Update the markers list with the updated marker data
            mutableStateListOf<MapMarker>().also { snapShot ->
                // Log.d("pre-snapshot markersData.markerInfos.size: ${markersLoadResult.markerInfos.size}")
                snapShot.clear()
                snapShot.addAll(fetchedMarkers)

                // Update the new "previous" markers list
                previousMapMarkers.clear()
                previousMapMarkers.addAll(fetchedMarkers)

                // Force a redraw of the map & markers
                coroutineScope.launch {
                    yield() // allow the UI to update // todo test if needed
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

        // For Marker Details Bottom Sheet
        var fetchMarkerDetailsResult by remember {
            mutableStateOf<LoadingState<MapMarker>>(LoadingState.Loading)
        }

        // Update user GPS location & Recently Seen Markers
        LaunchedEffect(Unit, fetchedMarkersResult.loadingState) {
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

            // Save last known location & add any recently seen markers
            snapshotFlow { userLocation }
                .collect { location ->
                    // 1. Save the last known location to settings
                    settings.setLastKnownUserLocation(location)

                    // 2. Check for new markers inside talk radius & add to recentlySeen list
                    addMarkersToRecentlySeenList(
                        mapMarkers,
                        userLocation,
                        talkRadiusMiles,
                        recentlySeenMarkersSet,
                        recentlySeenMarkersForUiList,
                        onUpdateMarkersIsSeen = { updatedMapMarkers ->
                            // Seen new markers, so update the current map markers list
                            coroutineScope.launch {
                                // Update the current Map markers with new `isSeen` value (true)
                                fetchedMarkersResult = fetchedMarkersResult.copy(
                                    markerIdToMapMarkerMap =
                                        updatedMapMarkers.associateBy { mapMarker ->
                                            mapMarker.id
                                        }
                                )

                                // todo make this a function?
                                mapMarkers.clear()
                                mapMarkers.addAll(updatedMapMarkers)
                                previousMapMarkers.clear()
                                previousMapMarkers.addAll(updatedMapMarkers)

                                // save the updated markers list to settings
                                settings.setMarkersResult(fetchedMarkersResult)
                            }
                        }
                    )
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

        // Turn on tracking automatically?
        LaunchedEffect(Unit) {
            val shouldStart = settings.isAutomaticStartBackgroundUpdatesWhenAppLaunchTurnedOn()
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
            sheetBackgroundColor = MaterialTheme.colors.background,
            sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
            sheetContent = {
                when (bottomSheetActiveScreen) {
                    is BottomSheetScreen.SettingsScreen -> {
                        SettingsScreen(
                            settings,
                            bottomSheetScaffoldState,
                            talkRadiusMiles,
                            onTalkRadiusChange = { updatedRadiusMiles -> talkRadiusMiles = updatedRadiusMiles },
                            onIsCachedMarkersLastUpdatedLocationVisibleChange = {
                                isMarkersLastUpdatedLocationVisible = it
                            },
                            onResetMarkerSettings = {
                                coroutineScope.launch {
                                    resetMarkerSettings(
                                        settings,
                                        mapMarkers,
                                        previousMapMarkers,
                                        recentlySeenMarkersSet,
                                        recentlySeenMarkersForUiList
                                    )
                                    userLocation = jiggleLocationToForceUpdate(userLocation)
                                }
                            },
                        )
                    }
                    is BottomSheetScreen.MarkerDetailsScreen -> {
                        // Use id string (coming from map marker) or marker id (coming from marker details screen)
                        val localMarker = (bottomSheetActiveScreen as BottomSheetScreen.MarkerDetailsScreen).marker
                        val markerIdFromMarker = localMarker?.id
                        val markerIdFromId = (bottomSheetActiveScreen as BottomSheetScreen.MarkerDetailsScreen).id
                        // Guard
                        if(markerIdFromId == null && markerIdFromMarker == null) {
                            throw IllegalStateException("Error: Both markerIdFromId and markerIdFromMarker are null, need to have at least one")
                        }

                        // Get marker from current mapMarkers list
                        val marker =  remember(markerIdFromMarker, markerIdFromId) {
                            val markerId: MarkerIdStr =
                                markerIdFromId
                                ?: markerIdFromMarker
                                ?: run {
                                    isShowingError = "Error: Unable to find marker id=$markerIdFromMarker"
                                    return@remember localMarker ?: MapMarker()
                                }

                            fetchedMarkersResult.markerIdToMapMarkerMap[markerId] ?: run {
                                isShowingError = "Error: Unable to find marker with id=$markerId"
                                return@remember localMarker ?: MapMarker()
                            }
                        }
                        val isMarkerDetailsAlreadyLoaded = marker.isDetailsLoaded

                        // Fetch the marker details
                        fetchMarkerDetailsResult = loadMapMarkerDetails(marker)

                        // Update the MapMarker with Marker Details (if they were loaded)
                        LaunchedEffect(fetchMarkerDetailsResult) {

                            // Did the marker details get loaded?
                            //   - if so, save the markers list to settings.
                            if (fetchMarkerDetailsResult is LoadingState.Loaded
                                && !isMarkerDetailsAlreadyLoaded
                                && (fetchMarkerDetailsResult as LoadingState.Loaded<MapMarker>).data.isDetailsLoaded
                            ) {
                                coroutineScope.launch {
                                    val updatedMarkerDetailsMarkersResult = updateMapMarkersWithFetchedMarkerDetails(
                                        fetchedMarkersResult,
                                        fetchMarkerDetailsResult,
                                        mapMarkers,
                                        settings
                                    )

                                    fetchedMarkersResult = fetchedMarkersResult.copy(
                                        markerIdToMapMarkerMap =
                                            updatedMarkerDetailsMarkersResult
                                                .markerIdToMapMarkerMap
                                                .values
                                                .associateBy { mapMarker ->
                                                    mapMarker.id
                                                }
                                    )

                                    // todo make this a function?
                                    previousMapMarkers.clear()
                                    previousMapMarkers.addAll(fetchedMarkersResult.markerIdToMapMarkerMap.values)
                                    mapMarkers.clear()
                                    mapMarkers.addAll(fetchedMarkersResult.markerIdToMapMarkerMap.values)

                                    Log.d("save the updated markers list to settings")
                                    settings.setMarkersResult(fetchedMarkersResult)
                                }
                            }
                        }

                        MarkerDetailsScreen(
                            bottomSheetScaffoldState,
                            fetchMarkerDetailsResult,
                        )
                    }
                }
            },
            drawerElevation = 16.dp,
            drawerScrimColor = Color.Black.copy(alpha = 0.5f),
            drawerGesturesEnabled = !bottomSheetScaffoldState.drawerState.isClosed,
            drawerContent = {
                AppDrawerContent(
                    bottomSheetScaffoldState,
                    fetchedMarkersResult,
                    onSetBottomSheetActiveScreen = { screen ->
                        bottomSheetActiveScreen = screen
                    },
                )
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
                                // Loading status
                                AnimatedVisibility(
                                    loadingStateIcon != Icons.Default.Cloud,
                                    enter = fadeIn(tween(1500)),
                                    exit = fadeOut(tween(1500))
                                ) {
                                    Icon(
                                        imageVector = loadingStateIcon,
                                        contentDescription = "Loading Status"
                                    )
                                }

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
                            visible = fetchedMarkersResult.loadingState is LoadingState.Error,
                        ) {
                            if (fetchedMarkersResult.loadingState is LoadingState.Error) {
                                Text(
                                    modifier = Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colors.error),
                                    text = "Error: ${(fetchedMarkersResult.loadingState as LoadingState.Error).errorMessage}",
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

                    // Map Content
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
                                isFinishedLoadingMarkerData = fetchedMarkersResult.isParseMarkersPageFinished,
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
                                isMapOptionSwitchesVisible = !isRecentlySeenMarkersPanelVisible,  // hide map options when showing marker list
                                onMarkerClick = { marker ->
                                    // Show marker details
                                    coroutineScope.launch {
                                        bottomSheetActiveScreen =
                                            BottomSheetScreen.MarkerDetailsScreen(marker)
                                        bottomSheetScaffoldState.bottomSheetState.apply {
                                            if (isCollapsed) expand()
                                        }
                                    }
                                },
                            )
                        if (didMapMarkersUpdate) {
                            shouldRedrawMapMarkers =
                                false  // The map has been updated, so don't redraw it again.
                        }
                    }

                    // Recently Seen Markers
                    RecentlySeenMarkers(
                        recentlySeenMarkersForUiList,
                        onClickRecentlySeenMarkerItem = { marker ->
                            // Show marker details
                            coroutineScope.launch {
                                bottomSheetActiveScreen = BottomSheetScreen.MarkerDetailsScreen(marker)
                                bottomSheetScaffoldState.bottomSheetState.apply {
                                    if (isCollapsed) expand()
                                }
                            }
                        }
                    )
                }
            }
        }
    }
}

private fun updateMapMarkersWithFetchedMarkerDetails(
    fetchMarkersResult: MarkersResult,
    fetchMarkerDetailsResult: LoadingState<MapMarker>,
    mapMarkers: SnapshotStateList<MapMarker>,
    settings: Settings
): MarkersResult {
    var updatedFetchMarkersResult = fetchMarkersResult

    // Update the marker with the details
    if (fetchMarkerDetailsResult is LoadingState.Loaded) {
        val updatedDetailsMapMarker = fetchMarkerDetailsResult.data

        // Find the matching marker id in the list
        val index = mapMarkers.indexOfFirst { marker ->
            marker.id == updatedDetailsMapMarker.id
        }
        // Update the marker to show & indicate the details have been loaded
        if (index >= 0 && !mapMarkers[index].isDetailsLoaded) {
            mapMarkers[index] = updatedDetailsMapMarker.copy(
                isDetailsLoaded = true
            )

            // Update the markers list with the updated marker with the updated details
            println("in updateMapMarkersWithFetchedMarkerDetails() " +
                    "fetchedMarkersResult.markerIdToMapMarkerMap[marker.id]?.isDetailsLoaded = ${fetchMarkersResult.markerIdToMapMarkerMap[updatedDetailsMapMarker.id]?.isDetailsLoaded}")
            updatedFetchMarkersResult = updatedFetchMarkersResult.copy(
                markerIdToMapMarkerMap = mapMarkers.associateBy { mapMarker ->
                    mapMarker.id
                }
            )

            // Update the settings with the updated marker data
            settings.setMarkersResult(updatedFetchMarkersResult)
        }
    }

    return updatedFetchMarkersResult
}

// force a change in location to trigger a reload of the markers
private fun jiggleLocationToForceUpdate(userLocation: Location) = Location(
    userLocation.latitude +
            Random.nextDouble(0.0001, 0.0002),
    userLocation.longitude +
            Random.nextDouble(0.0001, 0.0002)
)

private fun resetMarkerSettings(
    settings: Settings,
    mapMarkers: SnapshotStateList<MapMarker>,
    previousMapMarkers: SnapshotStateList<MapMarker>,
    recentlySeenMarkersSet: MutableSet<RecentMapMarker>,
    recentlySeenMarkersForUiList: SnapshotStateList<RecentMapMarker>
) {
    // Reset the `seen markers` list, UI elements
    recentlySeenMarkersSet.clear()
    recentlySeenMarkersForUiList.clear()
    mapMarkers.clear()
    previousMapMarkers.clear()

    // Reset the settings cache of markers
    settings.clearMarkersResult()
    settings.clearMarkersLastUpdatedLocation()
    settings.clearMarkersLastUpdateEpochSeconds()
}

// Check for new markers inside talk radius & add to recentlySeen list
private fun addMarkersToRecentlySeenList(
    mapMarkers: SnapshotStateList<MapMarker>,
    userLocation: Location,
    talkRadiusMiles: Double,
    recentlySeenMarkersSet: MutableSet<RecentMapMarker>,
    recentlySeenMarkersForUiList: SnapshotStateList<RecentMapMarker>,
    onUpdateMarkersIsSeen: (SnapshotStateList<MapMarker>) -> Unit
) {
    var updatedMarkers = mapMarkers
    var didUpdateMarkersIsSeen = false

    mapMarkers.forEach { marker ->
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
                it.key() == marker.id
            }
        }

        // add marker to recently seen set?
        if (distanceFromMarkerToUserLocationMiles < talkRadiusMiles * 1.75) {  // idk why 1.75, just seems to work
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
                Log.d("Added Marker ${marker.id} is within talk radius of $talkRadiusMiles miles, distance=$distanceFromMarkerToUserLocationMiles miles, total recentlySeenMarkers=${recentlySeenMarkersSet.size}")

                // Update the markers for Map UI
                updatedMarkers = updatedMarkers.map { mapMarker: MapMarker ->
                    if (mapMarker.id == marker.id) {
                        mapMarker.copy(isSeen = true)
                    } else {
                        mapMarker
                    }
                }.toMutableStateList()
                didUpdateMarkersIsSeen = true

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

    // Update the "seen" markers for the Map
    if(didUpdateMarkersIsSeen) {
        onUpdateMarkersIsSeen(updatedMarkers)
    }

    // Update the UI list of recently seen markers (& reverse sort by time)
    val oldList = recentlySeenMarkersForUiList.toList()
    recentlySeenMarkersForUiList.clear()
    recentlySeenMarkersForUiList.addAll(oldList.sortedByDescending { recentMarker ->
        recentMarker.timeAddedToRecentList
    }.toMutableStateList())
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
    isMapOptionSwitchesVisible: Boolean = true,
    onMarkerClick: ((MapMarker) -> Unit)? = null
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
                isMapOptionSwitchesVisible = isMapOptionSwitchesVisible,
                onMarkerClick = onMarkerClick
            )

            isFirstUpdate = false
            didMapMarkersUpdate = false
        }
    }

    return didMapMarkersUpdate
}

expect fun getPlatformName(): String

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
