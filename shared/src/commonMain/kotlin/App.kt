import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
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
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import data.AppSettings
import data.AppSettings.Companion.kMarkersLastUpdatedLocation
import data.AppSettings.Companion.kMarkersResult
import data.AppSettings.Companion.kRecentlySeenMarkersSet
import data.AppSettings.Companion.kUiRecentlySeenMarkersList
import data.MarkersRepo
import data.appSettings
import data.loadMarkerDetails.loadMarkerDetails
import data.loadMarkers.LoadMarkersResult
import data.loadMarkers.distanceBetweenInMiles
import data.loadMarkers.loadMarkers
import data.loadMarkers.sampleData.kUseRealNetwork
import data.util.LoadingState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import presentation.MarkerDetailsScreen
import presentation.SettingsScreen
import presentation.app.AppDrawerContent
import presentation.app.AppTheme
import presentation.app.RecentlySeenMarkers
import presentation.maps.Location
import presentation.maps.MapContent
import presentation.maps.Marker
import presentation.maps.MarkerIdStr
import presentation.maps.RecentlySeenMarker
import presentation.maps.RecentlySeenMarkersList
import presentation.maps.toLocation
import presentation.speech.speakMarker
import presentation.speech.speakRecentlySeenMarker
import kotlin.random.Random
import co.touchlab.kermit.Logger as Log

val json = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}

const val kAppName = "Fred's History Hunt" // todo use resource?
const val kForceClearAllSettingsAtLaunch = false
const val kMaxReloadDistanceMiles = 2.0
const val kMaxMarkerDetailsAgeSeconds = 60 * 60 * 24 * 30  // 30 days

sealed class BottomSheetScreen {
    data object SettingsScreen : BottomSheetScreen()

    data class MarkerDetailsScreen(
        val marker: Marker? = null,  // Can pass in a MapMarker...
        val id: String? = marker?.id // ...or just an id string
    ) : BottomSheetScreen()
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun App(
    markersRepo: MarkersRepo = MarkersRepo(appSettings),
    gpsLocationService: GPSLocationService = GPSLocationService()
) {
    AppTheme {
        val coroutineScope = rememberCoroutineScope()
        val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
        val scaffoldState = rememberScaffoldState()
        var bottomSheetActiveScreen by remember {
            mutableStateOf<BottomSheetScreen>(BottomSheetScreen.SettingsScreen)
        }

        // Error Message state & value
        var errorMessageStr by remember {
            mutableStateOf<String?>(null)
        }

        // Seen Marker & Speaking UI
        var isTextToSpeechCurrentlySpeaking by remember {
            mutableStateOf(false)
        }
        var currentSpeakingMarker: RecentlySeenMarker? by remember {
            mutableStateOf(null)
        }
        var seenRadiusMiles by remember {
            mutableStateOf(appSettings.seenRadiusMiles)
        }

        // Google Maps UI elements
        var isTrackingEnabled by remember {
            mutableStateOf(appSettings.isStartBackgroundTrackingWhenAppLaunchesEnabled)
        }
        var shouldCenterCameraOnLocation by remember {
            mutableStateOf<Location?>(null) // used to center map on user location
        }
        var userLocation: Location by remember {
            mutableStateOf(appSettings.lastKnownUserLocation)
        }

        // UI markers data last update-at location
        var isMarkersLastUpdatedLocationVisible by
            remember(appSettings.isMarkersLastUpdatedLocationVisible) {
                mutableStateOf(appSettings.isMarkersLastUpdatedLocationVisible)
            }
        // Location where markers data was last updated
        var markersLastUpdatedLocation by remember {
            mutableStateOf(appSettings.markersLastUpdatedLocation)
        }

        // Recently Seen Markers
        val recentlySeenMarkersSet by remember {
            mutableStateOf(appSettings.recentlySeenMarkersSet.list.toMutableSet())
        }
        val uiRecentlySeenMarkersList by remember {
            mutableStateOf(appSettings.uiRecentlySeenMarkersList.list.toMutableStateList())
        }

        // Holds the set of saved markers, this prevents flicker when loading new markers while processing the marker page(s)
        val previousMarkers = remember {
            mutableStateListOf<Marker>().also { snapShot ->
                snapShot.addAll(markersRepo.markers()) // init from repo
            }
        }

        // Load markers
        var loadingStateIcon: ImageVector by remember {
            mutableStateOf(Icons.Default.CloudDownload)
        }
        val loadMarkersResult: LoadMarkersResult =
            loadMarkers(
                appSettings,
                markersRepo = markersRepo,
                userLocation = userLocation, // when user location changes, triggers potential load markers from server
                maxReloadDistanceMiles = kMaxReloadDistanceMiles.toInt(),
                onUpdateMarkersLastUpdatedLocation = { updatedLocation ->
                    markersLastUpdatedLocation = updatedLocation
                },
                onUpdateLoadingState = { loadingState ->
                    // Update the UI (icon) with the latest loading state
                    loadingStateIcon = calcLoadingStateIcon(loadingState)
                },
                showLoadingState = false,
                //    kSunnyvaleFakeDataset,
                //    kTepoztlanFakeDataset,
                //    kSingleItemPageFakeDataset,
                useFakeDataSetId = kUseRealNetwork,
                coroutineScope = coroutineScope
            )

        var shouldRedrawMapMarkers by remember {
            mutableStateOf(true)
        }
        var shouldShowInfoMarker by remember {
            mutableStateOf<Marker?>(null)
        }

        // On every location update or markers update, check if the markers need to be updated.
        // - Update "basic info" for markers AFTER index page has finished parsing.
        // - Parse results held in `loadMarkersResult`, saved to `markersRepo`, and then used to update the UI.
        val loadMarkers = remember(
            loadMarkersResult.isParseMarkersPageFinished,
            userLocation
        ) {
            // Finished parsing the markers basic info page?
            if (!loadMarkersResult.isParseMarkersPageFinished) {
                Log.d("🎯 SKIP loadMarkersResult.isParseMarkersPageFinished=false")
                return@remember previousMarkers // use the cached markers (prevents flickering)
            }

            // Update the markers with the latest data
            mutableStateListOf<Marker>().also { markersSnapShot ->
                // Log.d("🎯 loadMarkers START update")
                coroutineScope.launch(Dispatchers.IO) {
//                    delay(200) // debounce // todo remove soon - seems to not be necessary
                    val startTime = Clock.System.now()

                    loadMarkersResult.markerIdToMarker.values.forEach { marker ->
                        markersRepo.upsertMarkerBasicInfo(marker) // only basic info needs to be updated
                    }
                    markersRepo.updateIsParseMarkersPageFinished(true) // ensures the markers are updated

                    Log.d("🎯 loadMarkers END UPDATE, total time = ${(Clock.System.now() - startTime)}\n")
                }

                markersSnapShot.clear()
                markersSnapShot.addAll(markersRepo.markers())
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

        var finalMarkers by remember {
            mutableStateOf(markersRepo.markers())
        }
        // Set finalMarkers after any update to the MarkersRepo
        LaunchedEffect(markersRepo.updateLoadMarkersResultFlow) {
            markersRepo.updateLoadMarkersResultFlow.collectLatest { loadMarkersResult ->
                delay(250) // debounce - allow the loadMarkers to finish processing

                // Update the final markers list with the updated marker data
                val startTime = Clock.System.now()
                //Log.d("🍉 START markersRepo.updateMarkersResultFlow.collectLatest: \n" +
                //    "    ⎣ finalMarkers.size = ${finalMarkers.size},\n" +
                //    "    ⎣ loadMarkersResult.size = ${loadMarkersResult.markerIdToMarker.size},\n"
                //)

                // Update the final markers list with the updated marker data
                finalMarkers = loadMarkersResult.markerIdToMarker.values.toMutableStateList()
                loadMarkers.clear()
                loadMarkers.addAll(loadMarkersResult.markerIdToMarker.values)
                previousMarkers.clear()
                previousMarkers.addAll(loadMarkersResult.markerIdToMarker.values)

                Log.d("🍉 END - markersRepo.updateMarkersResultFlow.collectLatest:\n" +
                    "    ⎣ finalMarkers.size = ${finalMarkers.size}\n" +
                    "    ⎣ time to update all markers = ${(Clock.System.now() - startTime)}\n"
                )

                userLocation = jiggleLocationToForceUiUpdate(userLocation)
                shouldRedrawMapMarkers = true
                yield() // todo needed? remove?

            }
        }

        // Marker Details Loading State (for Bottom Sheet)
        var markerDetailsResult by remember {
            mutableStateOf<LoadingState<Marker>>(LoadingState.Loading)
        }

        // 1) Update user GPS location
        // 2) Check for Recently Seen Markers
        var isProcessingRecentlySeenList by remember {
            mutableStateOf(false)
        }
        LaunchedEffect(Unit, loadMarkersResult.loadingState, seenRadiusMiles, finalMarkers) {
            // Set the last known location to the current location in settings
            gpsLocationService.onUpdatedGPSLocation(
                errorCallback = { errorMessage ->
                    Log.w("Error: $errorMessage")
                    errorMessageStr = errorMessage
                }
            ) { updatedLocation ->
                //    val locationTemp = Location(
                //        37.422160,
                //        -122.084270 // googleplex
                //        // 18.976794,
                //        // -99.095387 // Tepoztlan
                //    )
                //    myLocation = locationTemp ?: run { // use fake location above
                userLocation = updatedLocation ?: run {
                    errorMessageStr = "Unable to get current location"
                    Log.w(errorMessageStr.toString())
                    return@run userLocation // just return most recent location
                }
                errorMessageStr = null
            }

            // Save last known location & add any recently seen markers
            snapshotFlow { userLocation }
                .collect { location ->
                    // 1. Save the last known location to settings
                    appSettings.lastKnownUserLocation = location
                    yield() // allows UI to update the location

                    //Log.d("👁️ 0.CHECK - isProcessingRecentlySeenList=$isProcessingRecentlySeenList")
                    if(!isProcessingRecentlySeenList) { // guard against re-enter while processing the list
                        isProcessingRecentlySeenList = true

                        //Log.d("👁️ 2.START - Collecting recently seen markers after location update..., finalMarkers.size=${finalMarkers.size}")
                        coroutineScope.launch(Dispatchers.IO) {
                            addSeenMarkersToRecentlySeenList(
                                markersRepo.markers(), // SSoT is the Repo
                                userLocation,
                                seenRadiusMiles,
                                recentlySeenMarkersSet,
                                uiRecentlySeenMarkersList,
                                onUpdateIsSeenMarkers = { updatedIsSeenMarkers,
                                                          updatedRecentlySeenMarkers,
                                                          updatedUiRecentlySeenMarkers ->
                                    // Update the UI with the updated markers
                                    coroutineScope.launch {
                                        //val startTime = Clock.System.now()
                                        //Log.d("👁️ 2.1-START, onUpdatedIsSeenMarkers")

                                        recentlySeenMarkersSet.clear()
                                        recentlySeenMarkersSet.addAll(updatedRecentlySeenMarkers)
                                        uiRecentlySeenMarkersList.clear()
                                        uiRecentlySeenMarkersList.addAll(updatedUiRecentlySeenMarkers)

                                        // Update the settings
                                        appSettings.recentlySeenMarkersSet =
                                            RecentlySeenMarkersList(recentlySeenMarkersSet.toList())
                                        appSettings.uiRecentlySeenMarkersList =
                                            RecentlySeenMarkersList(uiRecentlySeenMarkersList)
                                        yield()

                                        // Update `isSeen` in the markers repo (will trigger a redraw of the map & markers)
                                        updatedIsSeenMarkers.forEach { updatedMarker ->
                                            markersRepo.updateMarkerIsSeen(
                                                updatedMarker,
                                                isSeen = true
                                            )
                                        }

                                        // Speak the top marker
                                        if (!isTextToSpeechSpeaking()) { // Don't interrupt current speech
                                            if (appSettings.isSpeakWhenUnseenMarkerFoundEnabled
                                                && appSettings.uiRecentlySeenMarkersList.list.isNotEmpty()
                                            ) {
                                                val nextUnspokenMarker =
                                                    appSettings.uiRecentlySeenMarkersList.list.first()
                                                currentSpeakingMarker =
                                                    speakRecentlySeenMarker(
                                                        nextUnspokenMarker,
                                                        appSettings.isSpeakDetailsWhenUnseenMarkerFoundEnabled,
                                                        coroutineScope,
                                                        onError = { errorMessage ->
                                                            Log.w(errorMessage)
                                                            errorMessageStr = errorMessage
                                                        },
                                                        markersRepo = markersRepo,
                                                        onUpdateLoadingState = { loadingState ->
                                                            loadingStateIcon =
                                                                calcLoadingStateIcon(
                                                                    loadingState
                                                                )
                                                        }
                                                    )
                                            }
                                        }

                                        // Update the UI with the updated markers
                                        //finalMarkers = markersRepo.markers() // pull from SSoT after update
                                        shouldRedrawMapMarkers = true
                                        isProcessingRecentlySeenList = false

                                        // Log.d("👁️ 2.1-END, processing time = ${(Clock.System.now() - startTime)}")
                                    }
                                },
                                onFinishedProcessRecentlySeenList = { startTime ->
                                    //Log.d("👁️ 5.END With NO-CHANGES - onFinishedProcessRecentlySeenList, processing time = ${(Clock.System.now() - startTime)}")
                                    isProcessingRecentlySeenList = false
                                }
                            )
                        }
                    }
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
            if (appSettings.isStartBackgroundTrackingWhenAppLaunchesEnabled) {
                startTracking()
            }
        }

        // Update isCurrentlySpeaking UI flag
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                while (true) {
                    delay(250)
                    isTextToSpeechCurrentlySpeaking = isTextToSpeechSpeaking()
                }
            }
        }

        // Poll for next unspoken marker
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                while (true) {
                    delay(1000)  // allow time for last text-to-speech to end

                    if (appSettings.isSpeakWhenUnseenMarkerFoundEnabled
                        && uiRecentlySeenMarkersList.isNotEmpty()
                    ) {
                        if (!isTextToSpeechSpeaking() && !isTextToSpeechCurrentlySpeaking) {

                            // look for next unspoken marker
                            val nextUnspokenMarker =
                                uiRecentlySeenMarkersList.firstOrNull { marker ->
                                    !(markersRepo.marker(marker.id)?.isSpoken
                                        ?: false) // default not spoken yet
                                }
                            nextUnspokenMarker?.let { speakMarker ->
                                isTextToSpeechCurrentlySpeaking = true
                                yield() // allow UI to update

                                currentSpeakingMarker = speakRecentlySeenMarker(
                                    speakMarker,
                                    appSettings.isSpeakDetailsWhenUnseenMarkerFoundEnabled,
                                    coroutineScope,
                                    onError = { errorMessage ->
                                        Log.w(errorMessage)
                                        errorMessageStr = errorMessage
                                    },
                                    markersRepo = markersRepo,
                                    onUpdateLoadingState = { loadingState ->
                                        loadingStateIcon = calcLoadingStateIcon(loadingState)
                                    }
                                )
                            }
                        }
                    }
                }
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
                            settings = appSettings,
                            bottomSheetScaffoldState,
                            seenRadiusMiles,
                            onSeenRadiusChange = { updatedRadiusMiles ->
                                seenRadiusMiles = updatedRadiusMiles
                            },
                            onIsCachedMarkersLastUpdatedLocationVisibleChange = {
                                isMarkersLastUpdatedLocationVisible = it
                            },
                            onResetMarkerSettings = {
                                coroutineScope.launch {
                                    resetMarkerCacheSettings(
                                        appSettings,
                                        loadMarkers,
                                        previousMarkers,
                                        recentlySeenMarkersSet,
                                        uiRecentlySeenMarkersList,
                                        markersRepo
                                    )

                                    finalMarkers = markersRepo.markers()
                                    shouldRedrawMapMarkers = true
                                    userLocation = jiggleLocationToForceUiUpdate(userLocation)
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
                        val markerIdStrFromParamId = bottomSheetParams.id
                        val markerIdStrFromMarker = localMarker?.id
                        // Guard
                        if (markerIdStrFromParamId.isNullOrBlank() && markerIdStrFromMarker.isNullOrBlank()) {
                            throw IllegalStateException(
                                "Error: Both markerIdFromId and " +
                                        "markerIdFromMarker are null, need to have at least one non-null value."
                            )
                        }

                        // Get marker from current mapMarkers list
                        val marker = remember(markerIdStrFromMarker, markerIdStrFromParamId) {
                            val markerId: MarkerIdStr =
                                markerIdStrFromParamId
                                    ?: markerIdStrFromMarker
                                    ?: run {
                                        errorMessageStr =
                                            "Error: Unable to find marker id=$markerIdStrFromMarker"
                                        return@remember localMarker ?: Marker()
                                    }

                            markersRepo.marker(markerId) ?: run {
                                errorMessageStr = "Error: Unable to find marker with id=$markerId"
                                return@remember localMarker ?: Marker()
                            }
                        }

                        markerDetailsResult = loadMarkerDetails(marker) // reactive composable

                        // Update the `marker` with Marker details (as they are loaded)
                        LaunchedEffect(markerDetailsResult) {
                            val updatedDetailsMarker =
                                (markerDetailsResult as? LoadingState.Loaded<Marker>)?.data

                            // Did fresh details get loaded?
                            if (updatedDetailsMarker != null
                                && markerDetailsResult is LoadingState.Loaded
                                && !marker.isDetailsLoaded
                                && updatedDetailsMarker.isDetailsLoaded
                            ) {
                                markersRepo.updateMarkerDetails(updatedDetailsMarker)
                            }
                        }

                        MarkerDetailsScreen(
                            bottomSheetScaffoldState,
                            markerDetailsResult,
                            isTextToSpeechCurrentlySpeaking = isTextToSpeechCurrentlySpeaking,
                            onClickStartSpeakingMarker = { speakMarker ->
                                markersRepo.updateMarkerIsSpoken(speakMarker, isSpoken = true)  // todo - should be a function and calls speakRecentlySeenMarker()
                                appSettings.lastSpokenRecentlySeenMarker = RecentlySeenMarker(
                                    speakMarker.id,
                                    speakMarker.title
                                )
                                currentSpeakingMarker = speakMarker(speakMarker, true)
                            },
                            onLocateMarkerOnMap = { locateMarker ->
                                coroutineScope.launch {
                                    shouldCenterCameraOnLocation = locateMarker.position.toLocation()
                                    shouldShowInfoMarker = locateMarker
                                }
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
                    markersRepo.markersResult(),
                    onSetBottomSheetActiveScreen = { screen ->
                        bottomSheetActiveScreen = screen
                    },
                )
            }
        ) {
            var isRecentlySeenMarkersPanelVisible by remember {
                mutableStateOf(appSettings.isRecentlySeenMarkersPanelVisible)
            }

            FixIssue_ScreenRotationLeavesDrawerPartiallyOpen(bottomSheetScaffoldState)

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
                                    text = "Fred's History Hunt",
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
                                        appSettings.isRecentlySeenMarkersPanelVisible =
                                            isRecentlySeenMarkersPanelVisible
                                    }
                                }) { // show marker history panel
                                    // Loading status icon
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
                                    // History icon
                                    AnimatedVisibility(
                                        loadingStateIcon == Icons.Default.Cloud,
                                        enter = fadeIn(tween(500)),
                                        exit = fadeOut(tween(500))
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.History,
                                            contentDescription = "Hide/Show Marker List"
                                        )
                                    }
                                }
                            }
                        )

                        // Show loading error
                        AnimatedVisibility(
                            visible = loadMarkersResult.loadingState is LoadingState.Error,
                        ) {
                            if (loadMarkersResult.loadingState is LoadingState.Error) {
                                Text(
                                    modifier = Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colors.error),
                                    text = "Error: ${(loadMarkersResult.loadingState as LoadingState.Error).errorMessage}",
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

                    // Show Error
                    AnimatedVisibility(errorMessageStr != null) {
                        Text(
                            modifier = Modifier.fillMaxWidth()
                                .background(MaterialTheme.colors.error),
                            text = "Error: $errorMessageStr",
                            textAlign = TextAlign.Center,
                            fontStyle = FontStyle.Normal,
                            fontWeight = FontWeight.Medium,
                            color = MaterialTheme.colors.onError
                        )
                    }

                    // Map Content
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        //val startTime = Clock.System.now()
                        //Log.d("✏️✏️⬇️  START map rendering, finalMarkers.size = ${finalMarkers.size}, shouldRedrawMapMarkers=$shouldRedrawMapMarkers\n" )

                        // Show Map
                        MapContent(
                            modifier = Modifier
                                .fillMaxHeight(1.0f - transitionRecentMarkersPanelState),
                            isFinishedLoadingMarkerData = loadMarkersResult.isParseMarkersPageFinished,
                            initialUserLocation = appSettings.lastKnownUserLocation,
                            userLocation = userLocation,
                            markers = markersRepo.markers(),
                            mapBounds = null,  // leave for future use
                            shouldRedrawMarkers = shouldRedrawMapMarkers, // redraw the map & markers
                            onDidRedrawMarkers = { // map & markers have been redrawn
                                shouldRedrawMapMarkers = false
                            },
                            isTrackingEnabled = isTrackingEnabled,
                            shouldCenterCameraOnLocation = shouldCenterCameraOnLocation,
                            onDidCenterCameraOnLocation = {
                                shouldCenterCameraOnLocation = null
                            },
                            seenRadiusMiles = seenRadiusMiles,
                            cachedMarkersLastUpdatedLocation =
                                remember(
                                    appSettings.isMarkersLastUpdatedLocationVisible,
                                    markersLastUpdatedLocation
                                ) {
                                    if (appSettings.isMarkersLastUpdatedLocationVisible)
                                        markersLastUpdatedLocation
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
                                shouldCenterCameraOnLocation = userLocation.copy()
                            },
                            isMarkersLastUpdatedLocationVisible = isMarkersLastUpdatedLocationVisible,
                            isMapOptionSwitchesVisible = !isRecentlySeenMarkersPanelVisible,  // hide map options when showing marker list
                            onMarkerInfoClick = { marker ->
                                // Show marker details
                                coroutineScope.launch {
                                    bottomSheetActiveScreen =
                                        BottomSheetScreen.MarkerDetailsScreen(marker)
                                    bottomSheetScaffoldState.bottomSheetState.apply {
                                        if (isCollapsed) expand()
                                    }
                                }
                            },
                            shouldShowInfoMarker = shouldShowInfoMarker,
                            onDidShowInfoMarker = {
                                shouldShowInfoMarker = null
                            }
                        )

                        //Log.d("✏️✏️🛑  END map rendering, time to render = ${(Clock.System.now() - startTime)}\n" )
                    }

                    RecentlySeenMarkers(
                        uiRecentlySeenMarkersList,
                        onClickRecentlySeenMarkerItem = { markerId ->
                            // Show marker details
                            coroutineScope.launch {
                                bottomSheetActiveScreen =
                                    BottomSheetScreen.MarkerDetailsScreen(id = markerId)
                                bottomSheetScaffoldState.bottomSheetState.apply {
                                    if (isCollapsed) expand()
                                }
                            }
                        },
                        currentlySpeakingMarker = currentSpeakingMarker,
                        isTextToSpeechCurrentlySpeaking = isTextToSpeechCurrentlySpeaking,
                        onClickStartSpeakingMarker = { recentlySeenMarker, shouldSpeakDetails: Boolean ->
                            stopTextToSpeech()

                            coroutineScope.launch {
                                delay(50) // allow UI to update
                                currentSpeakingMarker =
                                    speakRecentlySeenMarker(
                                        recentlySeenMarker,
                                        shouldSpeakDetails = shouldSpeakDetails,
                                        coroutineScope,
                                        onError = { errorMessage ->
                                            Log.w(errorMessage)
                                            errorMessageStr = errorMessage
                                        },
                                        markersRepo = markersRepo,
                                        onUpdateLoadingState = { loadingState -> // todo make function
                                            loadingStateIcon =
                                                calcLoadingStateIcon(loadingState)
                                        }
                                    )
                            }
                        },
                        onClickStopSpeakingMarker = {
                            stopTextToSpeech()
                        },
                        markersRepo = markersRepo,
                    )
                }
            }
        }
    }
}

private fun calcLoadingStateIcon(it: LoadingState<String>) =
    when (it) {
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

// force a change in location to trigger a reload of the markers
// todo refactor? maybe use callback and set `shouldRedrawMapMarkers = true`
private fun jiggleLocationToForceUiUpdate(userLocation: Location) = Location(
    userLocation.latitude +
            Random.nextDouble(-0.00001, 0.00001),
    userLocation.longitude +
            Random.nextDouble(-0.00001, 0.00001)
)

private fun resetMarkerCacheSettings(
    settings: AppSettings,
    markers: SnapshotStateList<Marker>,
    previousMarkers: SnapshotStateList<Marker>,
    recentlySeenMarkersSet: MutableSet<RecentlySeenMarker>,
    uiRecentlySeenMarkersList: SnapshotStateList<RecentlySeenMarker>,
    markersRepo: MarkersRepo,
) {
    // Reset the `seen markers` list, UI elements
    markers.clear()
    previousMarkers.clear()
    recentlySeenMarkersSet.clear()
    uiRecentlySeenMarkersList.clear()

    // Reset the settings cache of markers
    settings.clear(kMarkersResult)
    settings.clear(kMarkersLastUpdatedLocation)
    settings.clear(kRecentlySeenMarkersSet)
    settings.clear(kUiRecentlySeenMarkersList)
    markersRepo.clearAllMarkers()
}

// Check for new markers inside talk radius & add to recentlySeen list
private fun addSeenMarkersToRecentlySeenList(
    markers: List<Marker>,
    userLocation: Location,
    seenRadiusMiles: Double,
    recentlySeenMarkersSet: MutableSet<RecentlySeenMarker>,
    uiRecentlySeenMarkersList: SnapshotStateList<RecentlySeenMarker>,
    onUpdateIsSeenMarkers: (
            isSeenMarkers: SnapshotStateList<Marker>,
            recentlySeenMarkersSet: MutableSet<RecentlySeenMarker>,
            uiRecentlySeenMarkersList: SnapshotStateList<RecentlySeenMarker>,
        ) -> Unit = { _, _, _ -> },
    onFinishedProcessRecentlySeenList: (Instant) -> Unit = {},
) {
    val updatedIsSeenMarkers: SnapshotStateList<Marker> =
        listOf<Marker>().toMutableStateList() // start with empty list
    var didUpdateMarkers = false
    val localRecentlySeenMarkersSet = recentlySeenMarkersSet.toMutableSet()
    val localUiRecentlySeenMarkersList = uiRecentlySeenMarkersList.toMutableList()

        if (markers.isEmpty()) {
            Log.d("👁️ SKIP addSeenMarkersToRecentlySeenList bc markers.isEmpty()")
            onFinishedProcessRecentlySeenList(Clock.System.now())
            return
        }

        val startTime = Clock.System.now()
        //Log.d("👁️⬇️ START update isSeen: markers.size=${markers.size}, recentlySeenMarkersSet.size=${recentlySeenMarkersSet.size}, uiRecentlySeenMarkersList.size=${uiRecentlySeenMarkersList.size}\n")
        markers.forEach { marker ->

            if(!marker.isSeen) {
                // if unseen marker is within talk radius, then add to recently seen list.
                val markerLat = marker.position.latitude
                val markerLong = marker.position.longitude
                val distanceFromMarkerToUserLocationMiles = distanceBetweenInMiles(
                    userLocation.latitude,
                    userLocation.longitude,
                    markerLat,
                    markerLong,
                )

                // add marker to recently seen set?
                if (distanceFromMarkerToUserLocationMiles < seenRadiusMiles) {

                    fun MutableSet<RecentlySeenMarker>.containsMarker(marker: Marker): Boolean {
                        return any { recentMapMarker ->
                            recentMapMarker.id == marker.id
                        }
                    }

                    updatedIsSeenMarkers.add(marker.copy(isSeen = true))

                    // Not already in the `recently seen` set of all `recently seen` markers?
                    if (!localRecentlySeenMarkersSet.containsMarker(marker)) {
                        // Add to the `seen` set
                        val newlySeenMarker = RecentlySeenMarker(
                            marker.id,
                            marker.title,
                            Clock.System.now().toEpochMilliseconds()
                        )
                        localRecentlySeenMarkersSet.add(newlySeenMarker)
                        localUiRecentlySeenMarkersList.add(newlySeenMarker)
                        //Log.d(
                        //    "👁️ Added Marker ${marker.id} is within talk radius of $seenRadiusMiles miles, " +
                        //            "distance=$distanceFromMarkerToUserLocationMiles miles, " +
                        //            "total recentlySeenMarkers=${recentlySeenMarkersSet.size}"
                        //)

                        // Trim the UI list to 5 items
                        if (localUiRecentlySeenMarkersList.size > 5) {
                            // Log.d("👁️ Trimming recentlySeenMarkersForUiList.size=${localUiRecentlySeenMarkersList.size}")
                            // remove old markers until there are only 5
                            do {
                                val oldestMarker =
                                    localUiRecentlySeenMarkersList.minByOrNull { recentMarker ->
                                        recentMarker.insertedAtEpochMilliseconds
                                    }

                                // remove the oldest marker
                                oldestMarker?.let { oldMarker ->
                                    localUiRecentlySeenMarkersList.remove(oldMarker)
                                    // Log.d("👁️ Removed oldest marker, recentlySeenMarkersList.size=${localUiRecentlySeenMarkersList.size}")
                                }
                            } while (localUiRecentlySeenMarkersList.size > 5)
                        }
                    }

                    didUpdateMarkers = true
                }
            }
        }

        // Update the "isSeen" marker colors for the Map
        // & save the updated markers list to settings.
        if (didUpdateMarkers) {
            // Update the UI list of recently seen markers (& reverse sort by insert-time)
            val oldList = localUiRecentlySeenMarkersList.toList() // todo refactor this for clarity
            localUiRecentlySeenMarkersList.clear()
            localUiRecentlySeenMarkersList.addAll(
                oldList.sortedByDescending { recentMarker ->
                    recentMarker.insertedAtEpochMilliseconds
                }.toMutableStateList()
            )

            onUpdateIsSeenMarkers(
                updatedIsSeenMarkers,
                localRecentlySeenMarkersSet,
                localUiRecentlySeenMarkersList.toList().toMutableStateList(),
            )

            Log.d("👁️⬆️ END update isSeen: processing time = ${Clock.System.now() - startTime}\n")
            return
        }

        //Log.d("👁️🛑 END NO-update isSeen: processing time = ${Clock.System.now() - startTime}\n")
        onFinishedProcessRecentlySeenList(Clock.System.now())
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun FixIssue_ScreenRotationLeavesDrawerPartiallyOpen(bottomSheetScaffoldState: BottomSheetScaffoldState) {
    // Fix a bug in Material 2 where the drawer isn't fully closed when the screen is rotated
    var isFinished by remember(bottomSheetScaffoldState.drawerState.isOpen) {
        if(bottomSheetScaffoldState.drawerState.isClosed) { // could simplify but want this to be explicit
            mutableStateOf(false)
        } else
            mutableStateOf(true)
    }
    LaunchedEffect(Unit, bottomSheetScaffoldState.drawerState.isOpen) {
        delay(250)
        while (!isFinished) {
            if (bottomSheetScaffoldState.drawerState.isClosed) {
                bottomSheetScaffoldState.drawerState.close() // yes it polls but it's only 250ms
            }
            if(bottomSheetScaffoldState.drawerState.isOpen) {
                isFinished = true
            }
        }
    }
}

expect fun getPlatformName(): String
