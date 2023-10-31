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
import data.AppSettings
import data.AppSettings.Companion.kMarkersLastUpdatedLocation
import data.AppSettings.Companion.kMarkersResult
import data.AppSettings.Companion.kRecentlySeenMarkersSet
import data.AppSettings.Companion.kUiRecentlySeenMarkersList
import data.LoadingState
import data.loadMarkerDetails.calculateMarkerDetailsPageUrl
import data.loadMarkerDetails.loadMarkerDetails
import data.loadMarkerDetails.parseMarkerDetailsPageHtml
import data.loadMarkerDetails.sampleData.almadenVineyardsM2580
import data.loadMarkers.MarkersResult
import data.loadMarkers.distanceBetween
import data.loadMarkers.loadMarkers
import data.loadMarkers.sampleData.kUseRealNetwork
import data.settings
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield
import kotlinx.datetime.Clock
import kotlinx.serialization.json.Json
import maps.CameraLocationBounds
import maps.CameraPosition
import maps.LatLong
import maps.Marker
import maps.MarkerIdStr
import maps.RecentlySeenMarker
import maps.RecentlySeenMarkersList
import network.httpClient
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

    // Can pass in a MapMarker or just an id string
    data class MarkerDetailsScreen(val marker: Marker? = null, val id: String? = marker?.id) : BottomSheetScreen()
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

        // Error Message state & value
        var isShowingError by remember {
            mutableStateOf<String?>(null)
        }

        // Speaking UI
        var isCurrentlySpeaking by remember {
            mutableStateOf(false)
        }
        var currentlySpeakingMarker: RecentlySeenMarker? by remember {
            mutableStateOf(null)
        }

        // Settings
//        val settings = remember {
//            AppSettings.use(settings).apply {
//                if(kForceClearSettingsAtLaunch) { clearAllSettings() }
//                // Log.setMinSeverity(Severity.Warn)
//                printAppSettings()
//            }
//        }
        var talkRadiusMiles by remember {
            mutableStateOf(settings.talkRadiusMiles)
        }
        var isMarkersLastUpdatedLocationVisible by
            remember(settings.isMarkersLastUpdatedLocationVisible) {
                mutableStateOf(settings.isMarkersLastUpdatedLocationVisible)
            }

        // Google Maps UI elements
        var isTrackingEnabled by remember {
            mutableStateOf(false)
        }
        var centerOnUserCameraLocation by remember {
            mutableStateOf<Location?>(null) // used to center map on user location
        }

        // GPS Location Service
        val gpsLocationService by remember {
            mutableStateOf(GPSLocationService())
        }
        var userLocation: Location by remember {
            mutableStateOf(settings.lastKnownUserLocation)
        }

        // Last "markers updated" location
        var cachedMarkersLastUpdatedLocation by remember {
            mutableStateOf(settings.markersLastUpdatedLocation)
        }

        // Recently Seen Markers
        val recentlySeenMarkersSet by remember {
            mutableStateOf(settings.recentlySeenMarkersSet.list.toMutableSet())
        }
        val recentlySeenMarkersForUiList by remember {
            mutableStateOf(settings.uiRecentlySeenMarkersList.list.toMutableStateList())
        }

        // Holds the set of saved markers, this prevents flicker when loading new markers while processing the marker page(s)
        val previousMarkers = remember {
            mutableStateListOf<Marker>()
        }

        // Load markers
        var loadingStateIcon: ImageVector by remember {
            mutableStateOf(Icons.Default.CloudDownload)
        }
        var markersResult: MarkersResult =
            loadMarkers(
                settings,
                userLocation = userLocation, // when user location changes, triggers potential load markers from server
                maxReloadDistanceMiles = kMaxReloadDistanceMiles.toInt(),
                onSetMarkersLastUpdatedLocation = { location ->
                    // Update the UI with the latest location
                    cachedMarkersLastUpdatedLocation = location
                },
                onUpdateLoadingState = {
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
                },
                showLoadingState = false,
                //    kSunnyvaleFakeDataset,
                //    kTepoztlanFakeDataset,
                //    kSingleItemPageFakeDataset,
                useFakeDataSetId = kUseRealNetwork
            )

        var shouldRedrawMapMarkers by remember {
            mutableStateOf(true)
        }

        // Update the markers AFTER page has finished parsing
        val markers =
            remember(markersResult.isParseMarkersPageFinished) {
            // More pages to load?
            if (!markersResult.isParseMarkersPageFinished) {
                // While loading new markers, use the cached markers to prevent flicker
                return@remember previousMarkers
            }

            // Update the markers list with the latest marker data. (after it's loaded)
            // Note: new markers are merged with the previous markers list.
            val fetchedMarkers =
                markersResult.markerIdToMarker.map { marker ->
                    marker.value
                }

            // Update the markers list with the updated marker data
            mutableStateListOf<Marker>().also { snapShot ->
                snapShot.clear()
                snapShot.addAll(fetchedMarkers)

                // Update the new "previous" markers list
                previousMarkers.clear()
                previousMarkers.addAll(fetchedMarkers)

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

        // Marker Details Loading State (for Bottom Sheet)
        var markerDetailsResult by remember {
            mutableStateOf<LoadingState<Marker>>(LoadingState.Loading)
        }

        // Update user GPS location & Recently Seen Markers
        LaunchedEffect(Unit, markersResult.loadingState) {
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
                    settings.lastKnownUserLocation = location

                    // 2. Check for new markers inside talk radius & add to recentlySeen list
                    addSeenMarkersToRecentlySeenList(
                        markers,
                        userLocation,
                        talkRadiusMiles,
                        recentlySeenMarkersSet,
                        recentlySeenMarkersForUiList,
                        onUpdateIsSeenMapMarkers = { updatedIsSeenMarkers ->
                            // Update the `isSeen` value of markers
                            coroutineScope.launch {
                                markersResult = setCurrentMarkersAndSaveToSettings(
                                        markersResult,
                                        updatedMarkers = updatedIsSeenMarkers,  // using these values to update the markers
                                        markers = markers,
                                        previousMarkers = previousMarkers,
                                        settings = settings
                                    )

                                // Speak the top marker
                                if (!isTextToSpeechSpeaking()) {
                                    // Speak the marker title
                                    if(settings.shouldSpeakWhenUnseenMarkerFound) {
                                        if(settings.uiRecentlySeenMarkersList.list.isNotEmpty()) {
                                            currentlySpeakingMarker = settings.uiRecentlySeenMarkersList.list.first()
                                            currentlySpeakingMarker?.let { speakingMarker ->
                                                speakRecentlySeenMarker(
                                                    speakingMarker,
                                                    markersResult,
                                                    settings.shouldSpeakDetailsWhenUnseenMarkerFound,
                                                    coroutineScope,
                                                    onError = { errorMessage ->
                                                        Log.w(errorMessage)
                                                        isShowingError = errorMessage
                                                    },
                                                    onUpdateMarkersResult = { updatedMarkersResult ->
                                                        Log.d { "in addSeenMarkersToRecentlySeenList onUpdateMarkersResult() updatedMarkersResult size= ${updatedMarkersResult.markerIdToMarker.size}" }
                                                        markersResult = updatedMarkersResult
                                                    }
                                                )
                                            }
                                        }
                                    }
                                }
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
            val shouldStart = settings.shouldStartBackgroundTrackingWhenAppLaunches
            if (shouldStart) {
                startTracking()
            }
        }

        // Poll for speech completed
        LaunchedEffect(Unit) {
            while (true) {
                delay(1000)
                isCurrentlySpeaking = isTextToSpeechSpeaking()
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
                                        markers,
                                        previousMarkers,
                                        recentlySeenMarkersSet,
                                        recentlySeenMarkersForUiList
                                    )
                                    userLocation = jiggleLocationToForceUpdate(userLocation)
                                }
                            },
                        )
                    }
                    is BottomSheetScreen.MarkerDetailsScreen -> {
                        // Use id string (coming from map marker in google maps)
                        // or marker id (coming from item in marker details screen)
                        val bottomSheetParams =
                            bottomSheetActiveScreen as BottomSheetScreen.MarkerDetailsScreen
                        val localMarker = bottomSheetParams.marker
                        val markerIdFromParamId = bottomSheetParams.id
                        val markerIdFromMarker = localMarker?.id
                        // Guard
                        if(markerIdFromParamId == null && markerIdFromMarker == null) {
                            throw IllegalStateException("Error: Both markerIdFromId and " +
                                    "markerIdFromMarker are null, need to have at least one non-null value.")
                        }

                        // Get marker from current mapMarkers list
                        val marker =  remember(markerIdFromMarker, markerIdFromParamId) {
                            val markerId: MarkerIdStr =
                                markerIdFromParamId
                                ?: markerIdFromMarker
                                ?: run {
                                    isShowingError = "Error: Unable to find marker id=$markerIdFromMarker"
                                    return@remember localMarker ?: Marker()
                                }

                            markersResult.markerIdToMarker[markerId] ?: run {
                                isShowingError = "Error: Unable to find marker with id=$markerId"
                                return@remember localMarker ?: Marker()
                            }
                        }
                        val isMarkerDetailsAlreadyLoaded = marker.isDetailsLoaded

                        markerDetailsResult = loadMarkerDetails(marker)

                        // Update the MapMarker with Marker Details (if they were loaded)
                        LaunchedEffect(markerDetailsResult) {
                            // Did fresh marker details get loaded?
                            if (markerDetailsResult is LoadingState.Loaded
                                && !isMarkerDetailsAlreadyLoaded
                                && (markerDetailsResult as LoadingState.Loaded<Marker>).data.isDetailsLoaded
                            ) {
                                // Update the markers and save to settings
                                coroutineScope.launch {
                                    val updatedDetailsMarkersResult =
                                        updateMarkersWithMarkerDetails(
                                            markersResult,
                                            markerDetailsResult,
                                            markers,
                                            settings
                                        )

                                    markersResult = setCurrentMarkersAndSaveToSettings(
                                        updatedDetailsMarkersResult,
                                        markers = markers,
                                        previousMarkers = previousMarkers,
                                        settings = settings
                                    )
                                }
                            }
                        }

                        MarkerDetailsScreen(
                            bottomSheetScaffoldState,
                            markerDetailsResult,
                            isCurrentlySpeaking = isCurrentlySpeaking,
                            onClickStartSpeakingMarker = { speakMarker ->
                                currentlySpeakingMarker =
                                    speakMarkerWithDetails(speakMarker, true)
                            },
                            onClickStopSpeakingMarker = {
                                stopTextToSpeech()
                                isCurrentlySpeaking = false
                            },
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
                    markersResult,
                    onSetBottomSheetActiveScreen = { screen ->
                        bottomSheetActiveScreen = screen
                    },
                )
            }
        ) {
            var isRecentlySeenMarkersPanelVisible by remember {
                mutableStateOf(settings.isRecentlySeenMarkersPanelVisible)
            }

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
                                        isRecentlySeenMarkersPanelVisible = !isRecentlySeenMarkersPanelVisible
                                        settings.isRecentlySeenMarkersPanelVisible =
                                            isRecentlySeenMarkersPanelVisible
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
                            visible = markersResult.loadingState is LoadingState.Error,
                        ) {
                            if (markersResult.loadingState is LoadingState.Error) {
                                Text(
                                    modifier = Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colors.error),
                                    text = "Error: ${(markersResult.loadingState as LoadingState.Error).errorMessage}",
                                    fontStyle = FontStyle.Normal,
                                    fontWeight = FontWeight.Medium,
                                    color = MaterialTheme.colors.onError
                                )
                            }
                        }
                    }
                },
            ) {
                val transitionRecentMarkersPanelState: Float by animateFloatAsState(
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
                                    .fillMaxHeight(1.0f - transitionRecentMarkersPanelState),
                                isFinishedLoadingMarkerData = markersResult.isParseMarkersPageFinished,
                                initialUserLocation = settings.lastKnownUserLocation,
                                userLocation = userLocation,
                                markers = markers,
                                mapBounds = null,
                                shouldRedrawMapMarkers = shouldRedrawMapMarkers, // redraw the map & markers
                                isTrackingEnabled = isTrackingEnabled,
                                centerOnUserCameraLocation = centerOnUserCameraLocation,
                                talkRadiusMiles = talkRadiusMiles,
                                cachedMarkersLastUpdatedLocation =
                                    remember(
                                        settings.isMarkersLastUpdatedLocationVisible,
                                        cachedMarkersLastUpdatedLocation
                                    ) {
                                        if (settings.isMarkersLastUpdatedLocationVisible)
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
                            // The map has been updated, so don't redraw it again.
                            shouldRedrawMapMarkers = false
                        }
                    }

                    RecentlySeenMarkers(
                        recentlySeenMarkersForUiList,
                        onClickRecentlySeenMarkerItem = { markerId ->
                            // Show marker details
                            coroutineScope.launch {
                                bottomSheetActiveScreen = BottomSheetScreen.MarkerDetailsScreen(id = markerId)
                                bottomSheetScaffoldState.bottomSheetState.apply {
                                    if (isCollapsed) expand()
                                }
                            }
                        },
                        currentSpokenRecentlySeenMarker = currentlySpeakingMarker,
                        isCurrentlySpeaking = isCurrentlySpeaking,
                        onClickStartSpeakingMarker = { recentlySeenMarker ->
                            currentlySpeakingMarker = recentlySeenMarker
                            speakRecentlySeenMarker(
                                recentlySeenMarker,
                                markersResult,
                                settings.shouldSpeakDetailsWhenUnseenMarkerFound,
                                coroutineScope,
                                onError = { errorMessage ->
                                    Log.w(errorMessage)
                                    isShowingError = errorMessage
                                },
                                onUpdateMarkersResult = { updatedMarkersResult ->
                                    Log.d { "in speakRecentlySeenMarker() onClickStartSpeakingMarker updatedMarkersResult size= ${updatedMarkersResult.markerIdToMarker.size}" }
                                    markersResult = updatedMarkersResult
                                }
                            )
                        },
                        onClickStopSpeakingMarker = {
                            stopTextToSpeech()
                            isCurrentlySpeaking = false
                        },
                    )
                }
            }
        }
    }
}

private fun speakRecentlySeenMarker(
    recentlySeenMarker: RecentlySeenMarker,
    markersResult: MarkersResult,
    includeDetails: Boolean = false,
    coroutineScope: CoroutineScope,
    onError: (String) -> Unit = { },
    onUpdateMarkersResult: (MarkersResult) -> Unit = { }, // if details are loaded, update the markers list.
) {

   if(includeDetails) {
       val marker = markersResult.markerIdToMarker[recentlySeenMarker.id]

       marker?.let {
           if(!marker.isDetailsLoaded) {
               coroutineScope.launch {
                   // Load the marker details
                   val (updatedMarker, errorMessage) = try {
                       // if (!useFakeData) {
                       if (true) {
                           val markerDetailsPageUrl = marker.id.calculateMarkerDetailsPageUrl()
                           val response = httpClient.get(markerDetailsPageUrl)
                           val markerDetailsPageHtml = response.body<String>()

                           // parse the page html into a MarkerInfo object
                           val (errorMessage, parsedMarkerDetails) =
                               parseMarkerDetailsPageHtml(markerDetailsPageHtml)
                           errorMessage?.run { throw Exception(errorMessage) }

                           // update the passed-in marker with the parsed info and return it
                           parsedMarkerDetails ?: throw Exception("parsedMarkerDetails is null")
                           Pair(
                               parsedMarkerDetails.copy(
                                   position = marker.position,
                                   id = marker.id,
                                   title = marker.title,
                                   subtitle = marker.subtitle,
                                   alpha = marker.alpha,
                                   isSeen = marker.isSeen,
                                   isDetailsLoaded = true,
//                                   inscription = parsedMarkerDetails.inscription,
//                                   englishInscription = parsedMarkerDetails.englishInscription,
//                                   spanishInscription = parsedMarkerDetails.spanishInscription,
//                                   erected = parsedMarkerDetails.erected,
//                                   mainPhotoUrl = parsedMarkerDetails.mainPhotoUrl,
//                                   markerPhotos = parsedMarkerDetails.markerPhotos,
//                                   photoCaptions = parsedMarkerDetails.photoCaptions,
//                                   photoAttributions = parsedMarkerDetails.photoAttributions,
//                                   credits = parsedMarkerDetails.credits
                               ),
                               null // no error
                           )
                       } else {
                           // loadingState = fakeLoadingStateForMarkerDetailsPageHtml(mapMarker)  // for debugging - LEAVE FOR REFERENCE
                           val markerDetailsPageHtml = almadenVineyardsM2580()
                           val (errorMessage, detailsMarker) =
                               parseMarkerDetailsPageHtml(markerDetailsPageHtml)
                           Pair(detailsMarker, null)
                       }
                   } catch (e: Exception) {
                       Pair(null, e.message ?: e.cause?.message ?: "Loading error")
                   }

                   // todo add error message handling callback
                   if(errorMessage != null) {
                       onError(errorMessage)
                       return@launch
                   }
                   if(updatedMarker == null) {
                       onError("Error: updatedMarker is null")
                       return@launch
                   }

                   speakMarkerWithDetails(updatedMarker, true)
                   settings.markersResult = markersResult.copy(
                       markerIdToMarker = markersResult.markerIdToMarker
                           .toMutableMap()
                           .apply {
                               put(marker.id, updatedMarker)
                           }
                   )
                   // Update the markers list with the updated marker details
                   onUpdateMarkersResult(settings.markersResult)
               }

               return@let
           }

           speakMarkerWithDetails(marker, true)
       }
   } else {
       speakTextToSpeech(recentlySeenMarker.title)
   }
}

private fun speakMarkerWithDetails(
    marker: Marker,
    includeDetails: Boolean = false
): RecentlySeenMarker {
    val currentlySpeakingRecentlySeenMarker =
        RecentlySeenMarker(
            marker.id,
            marker.title,
        )

    if(includeDetails) {
        // Speak the marker title and inscription
        val title = marker.title
        val subtitle = marker.subtitle
        val inscription = marker.inscription
        val englishInscription = marker.englishInscription
        val spanishInscription = marker.spanishInscription

        var finalSpeechText = "Marker details"
        if (title.isNotEmpty()) {
            finalSpeechText += " for $title"
        }
        if (subtitle.isNotEmpty()) {
            finalSpeechText += ", with subtitle of $subtitle"
        }
        val inscriptionPrefix = " has inscription reading"
        finalSpeechText += if (englishInscription.isNotEmpty()) {
            " $inscriptionPrefix $englishInscription"
        } else if (inscription.isNotEmpty()) {
            " $inscriptionPrefix $inscription"
        } else if (spanishInscription.isNotEmpty()) {
            " tiene inscripción que dice $spanishInscription"
        } else {
            " and there is no inscription available."
        }

        speakTextToSpeech(finalSpeechText)
    } else {
        speakTextToSpeech(marker.title)
    }

    return currentlySpeakingRecentlySeenMarker
}

// Clears & Sets the current map markers, previous map markers, and saves the markers to settings
private fun setCurrentMarkersAndSaveToSettings(
    updatedMarkersResult: MarkersResult,                // A) Can use this instead of `updatedMarkers`.
    updatedMarkers: SnapshotStateList<Marker>? = null,  // B) Prefer to use `updatedMarkersResult`, if possible.
    markers: SnapshotStateList<Marker>,
    previousMarkers: SnapshotStateList<Marker>,
    settings: AppSettings
): MarkersResult {

    val localUpdatedMarkers =
        updatedMarkers ?:  // prefer to use `updatedMarkersResult`(next line), if possible
        updatedMarkersResult.markerIdToMarker.values.toList()

    // Update the markers result
    val newMarkersResult = updatedMarkersResult.copy(
        markerIdToMarker =
            localUpdatedMarkers
                .associateBy { mapMarker ->
                    mapMarker.id
                }
    )

    // Update the current Map markers
    markers.clear()
    markers.addAll(localUpdatedMarkers)

    // Update the previous markers list
    previousMarkers.clear()
    previousMarkers.addAll(localUpdatedMarkers)

    // save the updated markers list to settings
    Log.d("save the updated markers list to settings")
    settings.markersResult = newMarkersResult

    return newMarkersResult
}

private fun updateMarkersWithMarkerDetails(
    markersResult: MarkersResult,  // will be updated with data from `fetchMarkerDetailsResult`
    markerDetailsResult: LoadingState<Marker>,
    markers: SnapshotStateList<Marker>,
    settings: AppSettings
): MarkersResult {
    var updatedMarkersResult = markersResult

    // Update the marker with the details
    if (markerDetailsResult is LoadingState.Loaded) {
        val updatedDetailsMarker = markerDetailsResult.data

        // Find the matching marker id in the list
        val index = markers.indexOfFirst { marker ->
            marker.id == updatedDetailsMarker.id
        }
        // Found the marker?
        if (index >= 0 && !markers[index].isDetailsLoaded) {
            val preserveMarker = markers[index] // preserve the marker data

            // TODO Make this a REPO function
            // Update the marker to show & indicate the details have been loaded
            markers[index] = updatedDetailsMarker.copy(
                title = preserveMarker.title, // keep the title value
                subtitle = preserveMarker.subtitle, // keep the subtitle value
                position = preserveMarker.position, // keep the position value
                alpha = preserveMarker.alpha, // keep the alpha value

                isSeen = preserveMarker.isSeen, // keep the isSeen value

                isDetailsLoaded = true, // ensure the `isDetailsLoaded` value reflects the details have been loaded
            )

            // Update markers list with updated details
            updatedMarkersResult = updatedMarkersResult.copy(
                markerIdToMarker = markers.associateBy { mapMarker ->
                    mapMarker.id
                }
            )
            // Save to settings
            settings.markersResult = updatedMarkersResult
        }
    }

    return updatedMarkersResult
}

// force a change in location to trigger a reload of the markers
private fun jiggleLocationToForceUpdate(userLocation: Location) = Location(
    userLocation.latitude +
            Random.nextDouble(0.0001, 0.0002),
    userLocation.longitude +
            Random.nextDouble(0.0001, 0.0002)
)

private fun resetMarkerSettings(
    settings: AppSettings,
    markers: SnapshotStateList<Marker>,
    previousMarkers: SnapshotStateList<Marker>,
    recentlySeenMarkersSet: MutableSet<RecentlySeenMarker>,
    uiRecentlySeenMarkersList: SnapshotStateList<RecentlySeenMarker>
) {
    // Reset the `seen markers` list, UI elements
    markers.clear()
    previousMarkers.clear()
    recentlySeenMarkersSet.clear()
    uiRecentlySeenMarkersList.clear()

    // Reset the settings cache of markers
    settings.clear(kMarkersResult)
    settings.clear(kMarkersLastUpdatedLocation)
    // settings.clear(kMarkersLastUpdateEpochSeconds)  // todo remove this?
    settings.clear(kRecentlySeenMarkersSet)
    settings.clear(kUiRecentlySeenMarkersList)
}

// Check for new markers inside talk radius & add to recentlySeen list
private fun addSeenMarkersToRecentlySeenList(
    markers: SnapshotStateList<Marker>,
    userLocation: Location,
    talkRadiusMiles: Double,
    recentlySeenMarkersSet: MutableSet<RecentlySeenMarker>,
    uiRecentlySeenMarkersList: SnapshotStateList<RecentlySeenMarker>,
    onUpdateIsSeenMapMarkers: (SnapshotStateList<Marker>) -> Unit,
    onUpdateCurrentlySpeaking: (Boolean, RecentlySeenMarker) -> Unit = {_,_ ->}
) {
    var updatedMarkers = markers // start with current value of `markers`.
    var didUpdateMarkers = false

    markers.forEach { marker ->
        // if marker is within talk radius, add to recently seen list
        val markerLat = marker.position.latitude
        val markerLong = marker.position.longitude
        val distanceFromMarkerToUserLocationMiles = distanceBetween(
            userLocation.latitude,
            userLocation.longitude,
            markerLat,
            markerLong
        )

        // add marker to recently seen set?
        if (distanceFromMarkerToUserLocationMiles < talkRadiusMiles * 1.75) {  // idk why 1.75, just seems to work
            fun MutableSet<RecentlySeenMarker>.containsMarker(marker: Marker): Boolean {
                return any { recentMapMarker ->
                    recentMapMarker.id == marker.id
                }
            }

            // Not already in the `recently seen` set of all `recently seen` markers?
            if (!recentlySeenMarkersSet.containsMarker(marker)) {
                // Add to the `seen` set
                val newlySeenMarker = RecentlySeenMarker(
                    marker.id,
                    marker.title,
                    Clock.System.now().toEpochMilliseconds(),
                    recentlySeenMarkersSet.size + 1
                )
                recentlySeenMarkersSet.add(newlySeenMarker)
                uiRecentlySeenMarkersList.add(newlySeenMarker)
                Log.d("Added Marker ${marker.id} is within talk radius of $talkRadiusMiles miles, distance=$distanceFromMarkerToUserLocationMiles miles, total recentlySeenMarkers=${recentlySeenMarkersSet.size}")

                // TODO Make this a REPO function
                // Update `isSeen` to change color of markers in Map UI
                updatedMarkers = updatedMarkers.map { updatedMarker: Marker ->
                    if (updatedMarker.id == newlySeenMarker.id) {
                        updatedMarker.copy(isSeen = true)
                    } else {
                        updatedMarker//.copy(isSeen = marker.isSeen)
                    }
                }.toMutableStateList()

                // Trim the UI list to 5 items
                if (uiRecentlySeenMarkersList.size > 5) {
                    Log.d("Trimming recentlySeenMarkersForUiList.size=${uiRecentlySeenMarkersList.size}")
                    // remove old markers until there are only 5
                    do {
                        val oldestMarker =
                            uiRecentlySeenMarkersList.minByOrNull { recentMarker ->
                                recentMarker.insertedAtEpochMilliseconds
                            }

                        // remove the oldest marker
                        oldestMarker?.let { oldMarker ->
                            uiRecentlySeenMarkersList.remove(oldMarker)
                        }
                        Log.d("Removed oldest marker, recentlySeenMarkersList.size=${uiRecentlySeenMarkersList.size}")
                    } while (uiRecentlySeenMarkersList.size > 5)
                }

                didUpdateMarkers = true
            }
        }
    }

    // Update the UI list of recently seen markers (& reverse sort by inserted time)
    val oldList = uiRecentlySeenMarkersList.toList()
    uiRecentlySeenMarkersList.clear()
    uiRecentlySeenMarkersList.addAll(
        oldList.sortedByDescending { recentMarker ->
            recentMarker.insertedAtEpochMilliseconds
        }.toMutableStateList())

    // Update the "isSeen" marker colors for the Map
    // & save the updated markers list to settings
    if(didUpdateMarkers) {
        onUpdateIsSeenMapMarkers(updatedMarkers)
        settings.recentlySeenMarkersSet = RecentlySeenMarkersList(recentlySeenMarkersSet.toList())
        settings.uiRecentlySeenMarkersList = RecentlySeenMarkersList(uiRecentlySeenMarkersList.toList())
    }
}

@Composable
fun MapContent(
    modifier: Modifier = Modifier,
    isFinishedLoadingMarkerData: Boolean = false,  // only sets the initial position, not tracked. Use `userLocation` for tracking.
    initialUserLocation: Location,
    userLocation: Location,
    markers: List<Marker>,
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
    onMarkerClick: ((Marker) -> Unit)? = null
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
                markers = markers.ifEmpty { null },
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
