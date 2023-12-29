
import CommonBilling.BillingState
import CommonSpeech.SpeechState
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.BottomSheetScaffold
import androidx.compose.material.BottomSheetScaffoldState
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Scaffold
import androidx.compose.material.Snackbar
import androidx.compose.material.SnackbarDuration
import androidx.compose.material.SnackbarHost
import androidx.compose.material.Text
import androidx.compose.material.TextButton
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
import androidx.compose.runtime.collectAsState
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
import androidx.compose.ui.unit.sp
import data.MarkersRepo
import data.appSettings
import data.configPropertyFloat
import data.configPropertyString
import data.loadMarkerDetails.loadMarkerDetails
import data.loadMarkers.distanceBetweenInMiles
import data.loadMarkers.loadMarkers
import data.loadMarkers.sampleData.kUseRealNetwork
import data.util.LoadingState
import kotlinx.atomicfu.locks.SynchronizedObject
import kotlinx.atomicfu.locks.synchronized
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.yield
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.json.Json
import presentation.app.AppDrawerContent
import presentation.app.MarkerDetailsScreen
import presentation.app.RecentlySeenMarkers
import presentation.app.onboarding.AboutBoxDialog
import presentation.app.settingsScreen.SettingsScreen
import presentation.app.settingsScreen.resetMarkerCacheSettings
import presentation.maps.Location
import presentation.maps.MapContent
import presentation.maps.Marker
import presentation.maps.MarkerIdStr
import presentation.maps.RecentlySeenMarker
import presentation.maps.RecentlySeenMarkersList
import presentation.maps.toLocation
import presentation.speech.speakRecentlySeenMarker
import presentation.uiComponents.AppTheme
import kotlin.random.Random
import kotlin.time.Duration.Companion.seconds
import co.touchlab.kermit.Logger as Log

// Reset App at launch
const val kForceClearAllSettingsAtLaunch = false

data class CommonAppMetadata(
    var isDebuggable: Boolean = false,
    var versionStr: String = "0.0.0",
    var androidBuildNumberStr: String = "n/a",   // Android only
    var iOSBundleVersionStr: String = "n/a", // iOS only
    var installAtEpochMilli: Long = 0L,
    var platformId: String = "unknown"
)
var appMetadata = CommonAppMetadata()
var debugLog = mutableListOf("Debug log: start time:" + Clock.System.now())

var appNameStr = configPropertyString("app.name", "app.name string not found")
val kMaxReloadRadiusMiles = configPropertyFloat("app.maxReloadRadiusMiles", 2.0f).toDouble()
const val kMaxMarkerDetailsAgeSeconds = 60 * 60 * 24 * 30  // 30 days

// Improve performance by restricting cluster size & updates
var frameCount = 0
var didFullFrameRender = false
var isTemporarilyPreventPerformanceTuningActive = false // prevents premature optimization after returning from background

// Speech
//var iosCommonSpeech: CommonSpeech = CommonSpeech()  // Speech for iOS only
var unspokenText: String? = null // used to speak text in chunks

// Attempt to fix database contention / race condition issue // todo remove soon
val synchronizedObject = SynchronizedObject()

// Error Messages Flow
@Suppress("ObjectPropertyName") // for leading underscore
var _errorMessageFlow: MutableSharedFlow<String> = MutableSharedFlow()
val errorMessageFlow: SharedFlow<String> = _errorMessageFlow  // read-only shared flow sent from Platform side

// Serialization
val json = Json {
    prettyPrint = true
    isLenient = true
    ignoreUnknownKeys = true
}

sealed class BottomSheetScreen {
    data object SettingsScreen : BottomSheetScreen()
    data class MarkerDetailsScreen(
        val marker: Marker? = null,  // Can pass in a MapMarker...
        val id: String? = marker?.id // ...or just an id string
    ) : BottomSheetScreen()
    data object None : BottomSheetScreen()
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun App(
    commonBilling: CommonBilling,
    commonAppMetadata: CommonAppMetadata,
    commonSpeech: CommonSpeech = CommonSpeech(), // only for iOS
    markersRepo: MarkersRepo = MarkersRepo(appSettings),
    gpsLocationService: GPSLocationService = GPSLocationService()
) {
    AppTheme {
        val coroutineScope = rememberCoroutineScope()
        val bottomSheetScaffoldState = rememberBottomSheetScaffoldState()
        val scaffoldState = rememberScaffoldState()
        var bottomSheetActiveScreen by remember {
            mutableStateOf<BottomSheetScreen>(BottomSheetScreen.None)
        }
        var isOnboardingDialogVisible by remember { mutableStateOf(false)}
        var isAboutBoxDialogVisible by remember { mutableStateOf(false)}
        var appSettingsIsSpeakWhenUnseenMarkerFoundEnabledState by remember {
            mutableStateOf(appSettings.isSpeakWhenUnseenMarkerFoundEnabled)
        }

        // Error Message state & value
        var errorMessageStr by remember {
            mutableStateOf<String?>(null)
        }
        LaunchedEffect(Unit) {
            errorMessageFlow.collectLatest { errorMessage ->
                Log.w(errorMessage)
                errorMessageStr = errorMessage

                delay(5000)
                errorMessageStr = null
            }
        }

        // Billing State & Message
        val billingState = commonBilling.billingStateFlow()
            .collectAsState(BillingState.NotPurchased()).value
        var billingMessageStr by remember {
            mutableStateOf<String?>(null)
        }
        LaunchedEffect(Unit) {
            commonBilling.billingMessageFlow().collectLatest { billingMessage ->
                billingMessageStr = billingMessage

                // Clear message on UI after a few seconds
                coroutineScope.launch {
                    delay(8.seconds)
                    billingMessageStr = null
                }
            }
        }
        // Set installation time stamp for trial period
        LaunchedEffect(Unit) {
            if(!appSettings.hasKey("installAtEpochMilli")) {
                if(commonAppMetadata.installAtEpochMilli == 0L) { // iOS does not set this at so we set it here. Android sets this from Build settings.
                    commonAppMetadata.installAtEpochMilli = Clock.System.now().toEpochMilliseconds()
                }

                appSettings.installAtEpochMilli = commonAppMetadata.installAtEpochMilli
                appMetadata = commonAppMetadata.copy(
                    installAtEpochMilli = appSettings.installAtEpochMilli
                )
            }

            // todo Launch onboarding dialog if first time or Pro-offer if expired? // todo
        }

        // Speech State
        var isMarkerCurrentlySpeaking by remember { // reactive to text-to-speech state
            mutableStateOf(false)
        }
        var activeSpeakingMarker: RecentlySeenMarker? by remember {
            mutableStateOf(null)
        }
        // iOS is the only platform that uses CommonSpeech (currently) This is implementation #2
        LaunchedEffect(Unit) {
            iosCommonSpeech = commonSpeech
            iosCommonSpeech.speechStateCommonFlow.collectLatest { speechState ->
                isMarkerCurrentlySpeaking = when(speechState) {
                    is SpeechState.Speaking -> {
                        Log.d("CommonSpeech.SpeechState.Speaking")
                        true
                    }

                    is SpeechState.NotSpeaking -> {
                        Log.d("CommonSpeech.SpeechState.NotSpeaking")
                        false
                    }
                }
            }
        }

        // Google Maps UI elements
        var userLocation: Location by remember {
            mutableStateOf(appSettings.lastKnownUserLocation)
        }
        var isTrackingEnabled by remember {
            mutableStateOf(appSettings.isStartBackgroundTrackingWhenAppLaunchesEnabled)
        }
        var shouldCenterCameraOnLocation by remember {
            mutableStateOf<Location?>(null) // used to center map on user location
        }
        var shouldZoomCameraToLatLongZoom by remember {
            mutableStateOf<LatLongZoom?>(null) // used to zoom to a specific location
        }
        var shouldAllowCacheReset by remember { mutableStateOf(false) }

        // UI marker-data last-updated-at location
        var isMarkersLastUpdatedLocationVisible by
            remember(appSettings.isMarkersLastUpdatedLocationVisible) {
            mutableStateOf(appSettings.isMarkersLastUpdatedLocationVisible)
        }
        var markersLastUpdatedLocation by remember {
            mutableStateOf(appSettings.markersLastUpdatedLocation)
        }

        // Seen Marker Radius
        var seenRadiusMiles by remember {
            mutableStateOf(appSettings.seenRadiusMiles)
        }

        // Recently Seen Markers
        val recentlySeenMarkersSet by remember {
            mutableStateOf(appSettings.recentlySeenMarkersSet.list.toMutableSet())
        }
        val uiRecentlySeenMarkersList by remember {
            mutableStateOf(appSettings.uiRecentlySeenMarkersList.list.toMutableStateList())
        }

        // Load markers based on user location (Reactively)
        var networkLoadingState: LoadingState<String> by remember {
            mutableStateOf(LoadingState.Finished)
        }
        var loadingStateIcon: ImageVector by remember(networkLoadingState) {
            mutableStateOf(
                calcLoadingStateIcon(networkLoadingState)
            )
        }
        loadMarkers(
            appSettings,
            markersRepo,
            userLocation, // when user location changes, triggers potential load markers from server
            maxReloadRadiusMiles = kMaxReloadRadiusMiles.toInt(),
            onUpdateMarkersLastUpdatedLocation = { updatedLocation ->
                markersLastUpdatedLocation = updatedLocation
            },
            onUpdateLoadingState = { loadingState ->
                networkLoadingState = loadingState
            },
            showLoadingState = false,
            //    kSunnyvaleFakeDataset,
            //    kTepoztlanFakeDataset,
            //    kSingleItemPageFakeDataset,
            useFakeDataSetId = kUseRealNetwork,
        )
        //val loadMarkersResult: LoadMarkersResult = LoadMarkersResult( // empty set markers // LEAVE for testing
        //    loadingState = LoadingState.Finished,
        //    isParseMarkersPageFinished = true,
        //    markerIdToMarker = mutableMapOf()
        //)

        // Command to Update the Map Markers (Cluster Items)
        var shouldCalcClusterItems by remember {
            mutableStateOf(true)
        }

        // Command to Show Info Marker
        var shouldShowInfoMarker by remember {
            mutableStateOf<Marker?>(null)
        }

        // Markers used to render onto the Map
        val finalMarkers = remember {
            mutableStateListOf<Marker>().apply {
                addAll(markersRepo.markers())
            }
        }
        // Set finalMarkers after any update to the MarkersRepo
        LaunchedEffect(markersRepo.updateLoadMarkersResultFlow) {
            markersRepo.updateLoadMarkersResultFlow.collectLatest { loadMarkersResult ->
                coroutineScope.launch(Dispatchers.IO) {
                    val startTime = Clock.System.now()

                    // Update the final markers list with the updated marker data
                    synchronized(synchronizedObject) {
                        finalMarkers.clear()
                        finalMarkers.addAll(loadMarkersResult.markerIdToMarkerMap.values)
                    }

                    Log.d(
                        "🍉 END - markersRepo.updateMarkersResultFlow.collectLatest:\n" +
                                "    ⎣ finalMarkers.size = ${finalMarkers.size}\n" +
                                "    ⎣ time to update all markers = ${(Clock.System.now() - startTime)}\n"
                    )

                    shouldCalcClusterItems = true
                }
            }
        }

        // Marker Details Load Result (for Bottom Sheet)
        var markerDetailsLoadingState by remember {
            mutableStateOf<LoadingState<Marker>>(LoadingState.Loading)
        }

        // Optimize energy usage by pausing "seen marker" tracking when user is not moving.
        var isSeenTrackingPaused by remember { mutableStateOf(false) }
        var isSeenTrackingPausedPhase by remember { mutableStateOf(0) }

        // 1) Update user GPS location
        var isProcessingRecentlySeenList by remember {
            mutableStateOf(false)
        }
        LaunchedEffect(Unit, seenRadiusMiles) { //, shouldCalculateMapMarkers) {
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
                errorMessageStr = null // clear any previous error message
            }

            // Save last known location & add any recently seen markers
            snapshotFlow { userLocation }
                .collect { location ->
                    // 1. Save the last known location to settings
                    appSettings.lastKnownUserLocation = location
                    yield() // allows UI to update the location
                    isSeenTrackingPaused = false
                }

            // LEAVE FOR REFERENCE
            //    // Get heading updates
            //    locationService.currentHeading { heading ->
            //        heading?.let {
            //            Log.d { "heading = ${it.trueHeading}, ${it.magneticHeading}" }
            //        }
            //    }
        }

        // Track and update `isSeen` for any markers within the `seenRadiusMiles`
        var previousUserLocation by remember { mutableStateOf(userLocation) }
        LaunchedEffect(Unit, isSeenTrackingPaused) {
            while(!isSeenTrackingPaused) {
                delay(1000)

                //Log.d("👁️ 0.CHECK - isProcessingRecentlySeenList=$isProcessingRecentlySeenList")
                if(!isProcessingRecentlySeenList) { // guard against re-enter while processing the list
                    isProcessingRecentlySeenList = true

                    Log.d("👁️ 2.START - Collecting recently seen markers after location update..., finalMarkers.size=${finalMarkers.size}")
                    addSeenMarkersToRecentlySeenList(
                        finalMarkers, // SSoT is finalMarkers
                        userLocation,
                        seenRadiusMiles,
                        recentlySeenMarkersSet,
                        uiRecentlySeenMarkersList,
                        onUpdateIsSeenMarkers = { updatedIsSeenMarkers,
                                                  updatedRecentlySeenMarkers,
                                                  updatedUiRecentlySeenMarkers ->
                            // Update the UI with the updated markers
                        coroutineScope.launch {
                            val startTime = Clock.System.now()
                            Log.d("👁️ 2.1-START, onUpdatedIsSeenMarkers")

                            recentlySeenMarkersSet.addAll(updatedRecentlySeenMarkers)
                            uiRecentlySeenMarkersList.clear()
                            uiRecentlySeenMarkersList.addAll(
                                updatedUiRecentlySeenMarkers
                            )

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

                            // Speak the next unspoken marker
                            if (!isTextToSpeechSpeaking()) { // Don't interrupt current speech
                                if (appSettings.isSpeakWhenUnseenMarkerFoundEnabled
                                    && appSettings.uiRecentlySeenMarkersList.list.isNotEmpty()
                                ) {
                                    val nextUnspokenMarker =
                                        appSettings.uiRecentlySeenMarkersList.list.first()
                                    activeSpeakingMarker =
                                        speakRecentlySeenMarker(
                                            nextUnspokenMarker,
                                            appSettings.isSpeakDetailsWhenUnseenMarkerFoundEnabled,
                                            markersRepo = markersRepo,
                                            coroutineScope,
                                            onUpdateLoadingState = { loadingState ->
                                                loadingStateIcon =
                                                    calcLoadingStateIcon(
                                                        loadingState
                                                    )
                                            },
                                            onSetUnspokenText = { nextTextChunk ->
                                                unspokenText = nextTextChunk
                                            },
                                            onError = { errorMessage ->
                                                Log.w(errorMessage)
                                                errorMessageStr = errorMessage
                                            }
                                        )
                                }
                            }

                            // Update the UI with the updated markers
                            shouldCalcClusterItems = true
                            shouldAllowCacheReset = true
                            isProcessingRecentlySeenList = false
                            Log.d("👁️ 2.1-END, processing time = ${(Clock.System.now() - startTime)}")

                            // If more than 5 markers "seen", then show the "Dense Marker Area" warning
                            if (uiRecentlySeenMarkersList.size >= 5) {
                                launch {
                                    scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                                    scaffoldState.snackbarHostState
                                        .showSnackbar(
                                            "Warning: Marker-dense Area\n" +
                                                    "Some recently seen markers are not listed.",
                                            duration = SnackbarDuration.Short
                                        )
                                }
                            }
                            }
                        },
                        onFinishedProcessRecentlySeenList = { startTime ->
                            Log.d("👁️ 5.END With NO-CHANGES - onFinishedProcessRecentlySeenList, processing time = ${(Clock.System.now() - startTime)}")
                            isProcessingRecentlySeenList = false
                        }
                    )
                }

                // Pause tracking if user is not moving
                if(!isSeenTrackingPaused) {
                    if(userLocation == previousUserLocation) {
                        isSeenTrackingPausedPhase++

                        if (isSeenTrackingPausedPhase >= 3) {
                            isSeenTrackingPaused = true
                        }
                    }
                }

                previousUserLocation = userLocation
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

        // Poll to update `isMarkerCurrentlySpeaking` UI flag for reactive UI
        LaunchedEffect(Unit) {
            withContext(Dispatchers.IO) {
                while (true) {
                    delay(150)
                    isMarkerCurrentlySpeaking = isTextToSpeechSpeaking()

                    // Speak the next chunk of unspoken text (in any)
                    if(!isMarkerCurrentlySpeaking) {

                        unspokenText?.let { text ->
                            // If more than 4000, Take the next 4000 characters.
                            if(text.length >= 4000) {
                                val lastWordBoundaryIndex =
                                    text.substring(0, 4000).lastIndexOf(" ")
                                val nextTextToSpeak =
                                    text.substring(0, lastWordBoundaryIndex)
                                val restOfUnspokenText =
                                    text.substring(lastWordBoundaryIndex)
                                unspokenText = restOfUnspokenText

                                speakTextToSpeech(nextTextToSpeak)
                                return@let
                            }

                            speakTextToSpeech(text)
                        }
                    }
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
                        if (!isTextToSpeechSpeaking() && !isMarkerCurrentlySpeaking) {

                            // look for next unspoken marker
                            val nextUnspokenMarker =
                                uiRecentlySeenMarkersList.firstOrNull { marker ->
                                    !(markersRepo.marker(marker.id)?.isSpoken
                                        ?: false) // default not spoken yet
                                }
                            nextUnspokenMarker?.let { speakMarker ->
                                isMarkerCurrentlySpeaking = true
                                yield() // allow UI to update

                                activeSpeakingMarker = speakRecentlySeenMarker(
                                    speakMarker,
                                    appSettings.isSpeakDetailsWhenUnseenMarkerFoundEnabled,
                                    markersRepo = markersRepo,
                                    coroutineScope,
                                    onUpdateLoadingState = { loadingState ->
                                        loadingStateIcon = calcLoadingStateIcon(loadingState)
                                    }
                                ) { errorMessage ->
                                    Log.w(errorMessage)
                                    errorMessageStr = errorMessage
                                }
                            }
                        }
                    }
                }
            }
        }

        // Force update UI when app first opens
        LaunchedEffect(Unit) {
            // Update location
            userLocation = jiggleLocationToForceUiUpdate(userLocation)
        }

        // For render performance tuning
        didFullFrameRender = false

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
                        openBottomSheet(bottomSheetScaffoldState, coroutineScope)
                        SettingsScreen(
                            appSettings = appSettings,
                            markersRepo,
                            bottomSheetScaffoldState,
                            seenRadiusMiles,
                            appSettingsIsSpeakWhenUnseenMarkerFoundEnabledState,
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
                                        finalMarkers,
                                        recentlySeenMarkersSet,
                                        uiRecentlySeenMarkersList,
                                        markersRepo
                                    )
                                    activeSpeakingMarker = null
                                    delay(1000) // wait for reset
                                    shouldCalcClusterItems = true
                                    shouldAllowCacheReset = true
                                    delay(1500) // wait for loading to finish
                                    userLocation = jiggleLocationToForceUiUpdate(userLocation)
                                }
                            },
                            onDismiss = {
                                coroutineScope.launch {
                                    bottomSheetScaffoldState.bottomSheetState.collapse()
                                    bottomSheetActiveScreen = BottomSheetScreen.None
                                }
                            }
                        )
                    }
                    is BottomSheetScreen.MarkerDetailsScreen -> {
                        openBottomSheet(bottomSheetScaffoldState, coroutineScope)

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
                                errorMessageStr =
                                    "Error: Unable to find marker with id=$markerId"
                                return@remember localMarker ?: Marker()
                            }
                        }

                        markerDetailsLoadingState = loadMarkerDetails(marker) // reactive composable

                        // Update the `marker` with Marker details (as they are loaded)
                        LaunchedEffect(markerDetailsLoadingState) {
                            val updatedDetailsMarker =
                                (markerDetailsLoadingState as? LoadingState.Loaded<Marker>)?.data

                            // Did fresh details get loaded?
                            if (updatedDetailsMarker != null
                                && markerDetailsLoadingState is LoadingState.Loaded
                                && !marker.isDetailsLoaded
                                && updatedDetailsMarker.isDetailsLoaded
                            ) {
                                markersRepo.updateMarkerDetails(updatedDetailsMarker)
                            }
                        }

                        MarkerDetailsScreen(
                            marker,
                            markerDetailsLoadingState,
                            isTextToSpeechCurrentlySpeaking = isMarkerCurrentlySpeaking,
                            onClickStartSpeakingMarker = { speakMarker ->
                                activeSpeakingMarker = speakRecentlySeenMarker(
                                    RecentlySeenMarker(speakMarker.id, speakMarker.title),
                                    true,
                                    markersRepo = markersRepo,
                                    coroutineScope,
                                    onUpdateLoadingState = { loadingState ->
                                        loadingStateIcon = calcLoadingStateIcon(loadingState)
                                    },
                                    onError = { errorMessage ->
                                        Log.w(errorMessage)
                                        errorMessageStr = errorMessage
                                    },
                                    onSetUnspokenText = { nextTextChunk ->
                                        unspokenText = nextTextChunk
                                    }
                                )
                            },
                            onLocateMarkerOnMap = { locateMarker ->
                                coroutineScope.launch {
                                    shouldCenterCameraOnLocation =
                                        locateMarker.position.toLocation()
                                    shouldShowInfoMarker = locateMarker
                                    shouldZoomCameraToLatLongZoom =
                                        LatLongZoom(locateMarker.position, 14f)
                                }
                            },
                            onDismiss = {
                                coroutineScope.launch {
                                    bottomSheetScaffoldState.bottomSheetState.collapse()
                                    bottomSheetActiveScreen = BottomSheetScreen.None
                                }
                            }
                        )
                    }
                    else -> {
                        if(bottomSheetScaffoldState.bottomSheetState.isExpanded) {
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.collapse()
                            }
                        }
                    }
                }
            },
            drawerElevation = 16.dp,
            drawerScrimColor = Color.Black.copy(alpha = 0.5f),
            drawerGesturesEnabled = !bottomSheetScaffoldState.drawerState.isClosed,
            drawerContent = {
                if(bottomSheetScaffoldState.drawerState.isOpen) {
                    AppDrawerContent(
                        finalMarkers, // SSoT is the finalMarkers
                        onSetBottomSheetActiveScreen = { screen ->
                            bottomSheetActiveScreen = screen
                        },
                        onShowAboutBox = { isAboutBoxDialogVisible = true },
                        onShowOnboarding = { isOnboardingDialogVisible = true },
                        onCloseDrawer = {
                            coroutineScope.launch {
                                bottomSheetScaffoldState.drawerState.close()
                            }
                        },
                        onExpandBottomSheet = {
                            coroutineScope.launch {
                                bottomSheetScaffoldState.bottomSheetState.expand()
                            }
                        },
                        activeSpeakingMarker = activeSpeakingMarker,
                        isMarkerCurrentlySpeaking = isMarkerCurrentlySpeaking,
                        onClickStartSpeakingMarker = { marker, isSpeakDetailsEnabled: Boolean ->
                            if(isTextToSpeechSpeaking()) stopTextToSpeech()
                            coroutineScope.launch {
                                delay(150)
                                activeSpeakingMarker =
                                    speakRecentlySeenMarker(
                                        RecentlySeenMarker(marker.id, marker.title),
                                        isSpeakDetailsEnabled = isSpeakDetailsEnabled,
                                        markersRepo = markersRepo,
                                        coroutineScope,
                                        onUpdateLoadingState = { loadingState ->
                                            networkLoadingState = loadingState
                                        }
                                    ) { errorMessage ->
                                        Log.w(errorMessage)
                                        errorMessageStr = errorMessage
                                    }
                            }
                        },
                        onClickStopSpeakingMarker = {
                            stopTextToSpeech()
                        },
                        onLocateMarker = { marker ->
                            coroutineScope.launch {
                                //shouldCenterCameraOnLocation = marker.position.toLocation() // no zooming
                                bottomSheetScaffoldState.drawerState.close()
                                shouldShowInfoMarker = marker
                                shouldZoomCameraToLatLongZoom = LatLongZoom(marker.position, 14f)
                            }
                        },
                        commonBilling = commonBilling,
                        billingState = billingState,
                    )
                }
            },
            snackbarHost = {
                SnackbarHost(
                    hostState = scaffoldState.snackbarHostState,
                    snackbar = {
                        Snackbar(
                            modifier = Modifier.padding(8.dp),
                            backgroundColor = MaterialTheme.colors.secondary,
                            shape = RoundedCornerShape(8.dp),
                            actionOnNewLine = true,
                            elevation = 8.dp,
                            action = {
                                TextButton(
                                    onClick = {
                                        scaffoldState.snackbarHostState.currentSnackbarData?.dismiss()
                                    }
                                ) {
                                    Text(
                                        text = "DISMISS",
                                        color = MaterialTheme.colors.onSecondary
                                    )
                                }
                            }
                        ) {
                            Text(
                                text = scaffoldState.snackbarHostState.currentSnackbarData?.message
                                    ?: "",
                                fontStyle = FontStyle.Italic,
                                fontWeight = FontWeight.Bold,
                                fontSize = MaterialTheme.typography.body1.fontSize,
                                color = MaterialTheme.colors.onError,
                                modifier = Modifier.padding(4.dp)
                            )
                        }
                    }
                )
            },
        ) {
            var isRecentlySeenMarkersPanelVisible by remember {
                mutableStateOf(appSettings.isRecentlySeenMarkersPanelVisible)
            }

            FixIssue_ScreenRotationLeavesDrawerPartiallyOpen(bottomSheetScaffoldState)

            val startTime = Clock.System.now()
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
                                    text = appNameStr,
                                    fontStyle = FontStyle.Normal,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 16.sp
                                )
                            },
                            actions = {
                                // Settings
                                IconButton(onClick = {
                                    // Toggle the settings panel
                                    bottomSheetActiveScreen =
                                        if(bottomSheetActiveScreen != BottomSheetScreen.SettingsScreen) {
                                            BottomSheetScreen.SettingsScreen
                                        } else {
                                            BottomSheetScreen.None
                                        }
                                }) {
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
                                }) {
                                    // Loading status icon
                                    AnimatedVisibility(
                                        networkLoadingState !is LoadingState.Finished,
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
                                        networkLoadingState is LoadingState.Finished,
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
                            visible = networkLoadingState is LoadingState.Error,
                        ) {
                            if (networkLoadingState is LoadingState.Error) {
                                Text(
                                    modifier = Modifier.fillMaxWidth()
                                        .background(MaterialTheme.colors.error),
                                    text = "Error: ${(networkLoadingState as LoadingState.Error).errorMessage}",
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
                //Log.d("🦉 Frame count = $frameCount, time = ${(Clock.System.now() - startTime)}" )

                Column(
                    modifier = Modifier.fillMaxHeight(),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.Start
                ) {
                    if(frameCount<=2) return@Column // prevent FoUC: frame 0=FoUC, 1=too-zoomed-out, 2=zoomed-in-properly

                    // Show Error
                    AnimatedVisibility(errorMessageStr != null) {
                        if(errorMessageStr != null) {
                            Text(
                                modifier = Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colors.error),
                                text = errorMessageStr?.let{ "Error: $it" } ?: "",
                                textAlign = TextAlign.Center,
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colors.onError
                            )
                        }
                    }

                    // Show Billing Message
                    AnimatedVisibility(billingMessageStr != null) {
                        if(billingMessageStr != null && billingMessageStr.toString().isNotBlank()) {
                            Text(
                                modifier = Modifier.fillMaxWidth()
                                    .background(MaterialTheme.colors.secondary),
                                text = billingMessageStr?.let{ "Billing: $it" } ?: "",
                                textAlign = TextAlign.Center,
                                fontStyle = FontStyle.Normal,
                                fontWeight = FontWeight.Medium,
                                color = MaterialTheme.colors.onSecondary
                            )
                        }
                    }

                    // Map Content
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        // Log.d("✏️✏️⬇️  START map rendering, finalMarkers.size = ${finalMarkers.size}, shouldRedrawMapMarkers=$shouldCalculateMarkers\n")

                        // Show Map
                        MapContent(
                            modifier = Modifier
                                .fillMaxHeight(1.0f - transitionRecentMarkersPanelState),
                            initialUserLocation =
                                appSettings.lastKnownUserLocation,
                            userLocation = userLocation,
                            markers = finalMarkers,
                            mapBounds = null,  // leave for future use
                            shouldCalcClusterItems = shouldCalcClusterItems, // calculate the markers clusters/heatmaps
                            onDidCalcClusterItems = { // map & markers have been redrawn
                                shouldCalcClusterItems = false
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
                            },
                            shouldZoomToLatLongZoom = shouldZoomCameraToLatLongZoom,
                            onDidZoomToLatLongZoom = {
                                shouldZoomCameraToLatLongZoom = null  // reset
                            },
                            shouldAllowCacheReset = shouldAllowCacheReset,
                            onDidAllowCacheReset = {
                                shouldAllowCacheReset = false
                            },
                        )

                        Log.d("✏️✏️🛑  END map rendering, time to render = ${(Clock.System.now() - startTime)}\n")
                    }

                    //Log.d("✏️✏️⬇️  START recently seen markers rendering")
                    RecentlySeenMarkers(
                        uiRecentlySeenMarkersList,
                        activeSpeakingMarker = activeSpeakingMarker,
                        isTextToSpeechCurrentlySpeaking = isMarkerCurrentlySpeaking,
                        appSettingsIsSpeakWhenUnseenMarkerFoundEnabledState, // reactive to settings
                        markersRepo = markersRepo,
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
                        onClickStartSpeakingMarker = { recentlySeenMarker, isSpeakDetailsEnabled: Boolean ->
                            if(isTextToSpeechSpeaking()) stopTextToSpeech()
                            coroutineScope.launch {
                                isTemporarilyPreventPerformanceTuningActive=true // prevents using emojis
                                delay(150)

                                activeSpeakingMarker =
                                    speakRecentlySeenMarker(
                                        recentlySeenMarker,
                                        isSpeakDetailsEnabled = isSpeakDetailsEnabled,
                                        markersRepo = markersRepo,
                                        coroutineScope,
                                        onUpdateLoadingState = { loadingState ->
                                            networkLoadingState = loadingState
                                        }
                                    ) { errorMessage ->
                                        Log.w(errorMessage)
                                        errorMessageStr = errorMessage
                                    }
                            }
                        },
                        onClickStopSpeakingMarker = {
                            isTemporarilyPreventPerformanceTuningActive=true // prevents using emojis
                            stopTextToSpeech()
                        },
                        onClickPauseSpeakingAllMarkers = {
                            appSettings.isSpeakWhenUnseenMarkerFoundEnabled = false
                            appSettingsIsSpeakWhenUnseenMarkerFoundEnabledState = false
                            isTemporarilyPreventPerformanceTuningActive=true // prevents using emojis
                            stopTextToSpeech()
                        },
                        onClickResumeSpeakingAllMarkers = {
                            appSettings.isSpeakWhenUnseenMarkerFoundEnabled = true
                            appSettingsIsSpeakWhenUnseenMarkerFoundEnabledState = true
                            isTemporarilyPreventPerformanceTuningActive=true // prevents using emojis
                        },
                    )
                    Log.d("✏️✏️🛑  END recently seen markers rendering, " +
                            "finalMarkers.size = ${finalMarkers.size}, " +
                            "time to render = ${(Clock.System.now() - startTime)}")
                }
            }
            frameCount++

            // Show Onboarding
            if (isOnboardingDialogVisible) {
                OnboardingDialog(
                    onDismiss = {
                        isOnboardingDialogVisible = false
                    }
                )
            }

            // Show AboutBox
            if (isAboutBoxDialogVisible) {
                AboutBoxDialog(
                    onDismiss = {
                        isAboutBoxDialogVisible = false
                    }
                )
            }

            //Log.d("🎃 END Frame time to render = ${(Clock.System.now() - startTime)}\n" )
        }
        didFullFrameRender = true
    }
}

@OptIn(ExperimentalMaterialApi::class)
private fun openBottomSheet(
    bottomSheetScaffoldState: BottomSheetScaffoldState,
    coroutineScope: CoroutineScope
) {
    if (bottomSheetScaffoldState.bottomSheetState.isCollapsed) {
        coroutineScope.launch {
            bottomSheetScaffoldState.bottomSheetState.expand()
        }
    }
}

private fun calcLoadingStateIcon(loadingState: LoadingState<String>) =
    when (loadingState) {
        is LoadingState.Loading -> {
            Icons.Default.CloudDownload
        }
        is LoadingState.Loaded -> {
            Icons.Default.Cloud // CloudDone uses a checkmark
        }
        is LoadingState.Finished -> {
            Icons.Default.Cloud
        }
        is LoadingState.Error -> {
            Icons.Default.CloudOff
        }
    }

// Force a change in location to trigger a reload of the markers
// todo refactor? maybe use callback and set `shouldRedrawMapMarkers = true`
private fun jiggleLocationToForceUiUpdate(userLocation: Location) = Location(
    userLocation.latitude +
            Random.nextDouble(-0.00001, 0.00001),
    userLocation.longitude +
            Random.nextDouble(-0.00001, 0.00001)
)


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
            val oldList = localUiRecentlySeenMarkersList.toList()
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
