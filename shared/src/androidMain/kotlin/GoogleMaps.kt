import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.requiredHeight
import androidx.compose.foundation.layout.requiredWidth
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.NoLiveLiterals
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMapOptions
import com.google.android.gms.maps.model.Dash
import com.google.android.gms.maps.model.Dot
import com.google.android.gms.maps.model.Gap
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MapStyleOptions
import com.google.maps.android.clustering.ClusterItem
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.MapsComposeExperimentalApi
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.google.maps.android.compose.rememberTileOverlayState
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import com.realityexpander.common.R
import data.loadMarkers.distanceBetweenInMiles
import data.loadMarkers.metersToMiles
import data.loadMarkers.milesToMeters
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.datetime.Clock
import org.jetbrains.compose.resources.ExperimentalResourceApi
import org.jetbrains.compose.resources.painterResource
import presentation.maps.CameraLocationBounds
import presentation.maps.CameraPosition
import presentation.maps.LatLong
import presentation.maps.Location
import presentation.maps.Marker
import presentation.maps.MarkerIdStr
import presentation.uiComponents.PreviewPlaceholder
import presentation.uiComponents.SwitchWithLabel
import kotlin.math.max
import kotlin.math.pow
import kotlin.time.Duration.Companion.milliseconds
import co.touchlab.kermit.Logger as Log

var startTime = Clock.System.now()
var restrictMarkerClusterRadiusPhase = 0
private const val kMaxRestrictMarkerClusterItemRadiusPhase = 3

// Android Google Maps implementation
@NoLiveLiterals
@OptIn(MapsComposeExperimentalApi::class, ExperimentalResourceApi::class)
@Composable
actual fun GoogleMaps(
    modifier: Modifier,
    isMapOptionSwitchesVisible: Boolean,
    isTrackingEnabled: Boolean,
    userLocation: LatLong?,
    markers: List<Marker>?,
    shouldCalculateClusterItemList: Boolean,
    onDidCalculateClusterItemList: () -> Unit, // Best for setting initial camera position bc zoom level is forced
    shouldSetInitialCameraPosition: CameraPosition?, // Best for tracking user location
    shouldCenterCameraOnLatLong: LatLong?, // Best for showing a bunch of markers
    onDidCenterCameraOnLatLong: () -> Unit,
    cameraLocationBounds: CameraLocationBounds?,
    polyLine: List<LatLong>?,
    onMapClick: ((LatLong) -> Unit)?,
    onMapLongClick: ((LatLong) -> Unit)?,
    onMarkerInfoClick: ((Marker) -> Unit)?,
    seenRadiusMiles: Double,
    cachedMarkersLastUpdatedLocation: Location?,
    onToggleIsTrackingEnabledClick: (() -> Unit)?,
    onFindMeButtonClick: (() -> Unit)?,
    isMarkersLastUpdatedLocationVisible: Boolean,
    shouldShowInfoMarker: Marker?,
    onDidShowInfoMarker: () -> Unit,
) {
    val cameraPositionState = rememberCameraPositionState()
    val uiSettings by remember {
        mutableStateOf(
            MapUiSettings(
                myLocationButtonEnabled = false, //!isTrackingEnabled,
                compassEnabled = false,
                mapToolbarEnabled = false,
                zoomControlsEnabled = false,  // the +/- buttons (obscures the FAB)
                zoomGesturesEnabled = true,
                scrollGesturesEnabled = true,
            )
        )
    }
    var properties by remember {
        mutableStateOf(
            MapProperties(
                isMyLocationEnabled = true,  // always show the dot
                minZoomPreference = 1f,
                maxZoomPreference = 25f,
                mapStyleOptions = MapStyleOptions(
                    mapStyle()  // Dark green map style
                )
            )
        )
    }

    // Local UI state
    var isMarkersEnabled by remember { mutableStateOf(true) }
    var isHeatMapEnabled by remember { mutableStateOf(false) }
    var showSomething by remember { mutableStateOf(false) } // LEAVE FOR TESTING PURPOSES

    // Usually used to setup the initial camera position (doesn't support tracking due to forcing zoom level)
    LaunchedEffect(shouldSetInitialCameraPosition) {
        shouldSetInitialCameraPosition?.let { cameraPosition ->
            //Log.d("💿 GoogleMaps-Android 👾: LaunchedEffect(shouldSetInitialCameraPosition), " +
            //        "shouldSetInitialCameraPosition = ${shouldSetInitialCameraPosition.target}, " +
            //        "zoom= ${shouldSetInitialCameraPosition.zoom}")
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        cameraPosition.target.latitude,
                        cameraPosition.target.longitude
                    ),
                    cameraPosition.zoom
                    // cameraPositionState.position.zoom // allows users to zoom in and out while maintaining the same center, why does this work?
                )
            )
        }

        //    // Follow the camera position - LEAVE FOR REFERENCE
        //    snapshotFlow { cameraPositionState.position }
        //        .collect { position ->
        //            // Log.d { "position = ${position.target.latitude}, ${position.target.longitude}" }
        //        }
    }

    // Set Camera to Bounds
    // Note: Zoom level is reset
    LaunchedEffect(cameraLocationBounds) {
        cameraLocationBounds?.let { cameraPositionBounds ->
            // Build the bounding box
            val latLngBounds = LatLngBounds.builder().apply {
                cameraPositionBounds.coordinates.forEach { latLong ->
                    include(LatLng(latLong.latitude, latLong.longitude))
                }
            }.build()

            cameraPositionState.move(
                CameraUpdateFactory.newLatLngBounds(latLngBounds, cameraPositionBounds.padding)
            )
        }
    }

    // Set Camera to presentation.maps.LatLong position (doesn't change zoom level)
    // Note: only allowed to change the camera position once per change in cameraLocationLatLong.
    //       This is to prevent the screen from locking the location. By only allowing the camera
    //       to change once, the user can pan around the map without the camera jumping back to
    //       the cameraLocationLatLong position.
    var previousCameraLocationLatLong by remember { mutableStateOf<LatLong?>(null) }
    LaunchedEffect(shouldCenterCameraOnLatLong) {
        if (previousCameraLocationLatLong == shouldCenterCameraOnLatLong) {
            return@LaunchedEffect
        }

        previousCameraLocationLatLong = shouldCenterCameraOnLatLong
        shouldCenterCameraOnLatLong?.let { cameraLocationLatLong ->
            println("💿 GoogleMaps-Android 👾: LaunchedEffect(shouldCenterCameraOnLatLong), shouldCenterCameraOnLatLong = $shouldCenterCameraOnLatLong")
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLng(
                    LatLng(
                        cameraLocationLatLong.latitude,
                        cameraLocationLatLong.longitude
                    )
                )
            )
            onDidCenterCameraOnLatLong()
        }
    }

    // Set Camera to User Location (Tracking) (doesn't change zoom level)
    LaunchedEffect(userLocation) {
        userLocation?.let { myLocation ->
            if (isTrackingEnabled) {
                cameraPositionState.animate(
                    CameraUpdateFactory.newLatLng(
                        LatLng(
                            myLocation.latitude,
                            myLocation.longitude
                        )
                    )
                )
            }
        }
    }

    val lightGrayMarkerPainterResource = painterResource("marker_lightgray.png")
    val redMarkerPainterResource = painterResource("marker_red.png")

    // Calculate Cluster items
    class SeeableClusterItem(
        val id: MarkerIdStr,
        val clusterItem: ClusterItem,
        var isSeen: Boolean = false
    ) : ClusterItem {
        override fun getPosition(): LatLng {
            return clusterItem.position
        }
        override fun getTitle(): String? {
            return clusterItem.title
        }
        override fun getSnippet(): String? {
            return clusterItem.snippet
        }
        override fun getZIndex(): Float? {
            return clusterItem.zIndex
        }
    }
    val cachedMarkerIdToSeeableClusterItemMap =
        remember { mutableStateMapOf<MarkerIdStr, SeeableClusterItem>() }
    val cachedClusterItemList = remember { mutableStateListOf<SeeableClusterItem>() }
    var finalRenderClusterItems by remember { mutableStateOf<List<SeeableClusterItem>>(listOf()) }
    var didUpdateClusterItems by remember { mutableStateOf(false) }
    var isCalculatingClusterItems by remember { mutableStateOf(false) }
    val ioCoroutineScope = rememberCoroutineScope { Dispatchers.IO }
    var isRestrictMarkerClusterRadiusActive by remember { mutableStateOf(false) } // to improve frame rate
    var previousCameraLocationLatLng by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    var shouldCalcRestrictMarkerClusterItems by remember(shouldCalculateClusterItemList) { mutableStateOf(false) }
    var lastRestrictClusterItemsCenterLocation by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    LaunchedEffect(
        shouldCalculateClusterItemList,
        shouldCalcRestrictMarkerClusterItems,
        cameraPositionState.position.target,
        LocalConfiguration.current.orientation,
        isRestrictMarkerClusterRadiusActive
    ) {
        delay(250) // debounce the cluster item calculation

        var forceRecalcFromCameraMove = false
        var forceRecalcFromRestrictionSizeChange = false
//        if(cameraPositionState.position.target != previousCameraLocationLatLng) {
//            previousCameraLocationLatLng = cameraPositionState.position.target
//            forceRecalcFromCameraMove = true
//        }
        if(shouldCalcRestrictMarkerClusterItems) {
            shouldCalcRestrictMarkerClusterItems = false
            forceRecalcFromRestrictionSizeChange = true
        }

        if(!forceRecalcFromCameraMove && !forceRecalcFromRestrictionSizeChange) {
            if (!shouldCalculateClusterItemList) {
                Log.d("💿 LaunchedEffect(shouldCalculateClusterItemList): " +
                    "shouldCalculateClusterItemList=false, No redraw necessary, Using cachedMarkerIdToSeeableClusterItemMap items, cachedMarkerIdToSeeableClusterItemMap.size = ${cachedMarkerIdToSeeableClusterItemMap.size}")
                return@LaunchedEffect
            }

            // Check if the markers are different than the cached markers
            if (markers?.size == cachedMarkerIdToSeeableClusterItemMap.size
                // check if any markers `isSeen` has changed
                && markers.all { marker ->
                    marker.isSeen == cachedMarkerIdToSeeableClusterItemMap[marker.id]?.isSeen
                }
            ) {
                Log.d("💿 LaunchedEffect(shouldCalculateClusterItemList): Using cachedMarkerIdToSeeableClusterItemMap because no isSeen in the list of markers has changed, cachedMarkerIdToSeeableClusterItemMap.size = ${cachedMarkerIdToSeeableClusterItemMap.size}")
                onDidCalculateClusterItemList()
                return@LaunchedEffect
            }
        }

        // Guard against multiple coroutines calculating the cluster items at the same time
        if(isCalculatingClusterItems) {
            Log.d("💿 LaunchedEffect(shouldCalculateClusterItemList): isCalculatingClusterItems=true, Using cachedMarkerIdToSeeableClusterItemMap items, cachedMarkerIdToSeeableClusterItemMap.size = ${cachedMarkerIdToSeeableClusterItemMap.size}")
            return@LaunchedEffect
        }

        // Calculate the cluster items - must update to change the isSeen property
        Log.d("💿 ⚝⚝⚝ 🔧 LaunchedEffect(shouldCalculateClusterItemList): START calculating new cluster items...")
        isCalculatingClusterItems = true
        //yield() // allow isCalculatingClusterItems to be set to true before continuing (prevents re-entrancy)

        val startTime = Clock.System.now()
        ioCoroutineScope.launch {

            fun Marker.isMarkerWithinRadiusMilesOfLatLng(
                radiusMiles: Double,
                centerLatLng: LatLng
            ): Boolean {
                val marker = this
                val distanceMiles =
                    distanceBetweenInMiles(
                        marker.position.latitude,
                        marker.position.longitude,
                        centerLatLng.latitude,
                        centerLatLng.longitude
                    )

                return distanceMiles <= radiusMiles
            }

            val localCachedClusterItemList = mutableListOf<SeeableClusterItem>()
            localCachedClusterItemList.clear()
            val clusterCenterLatLng =
                if(cameraPositionState.position.target.latitude != 0.0
                    && cameraPositionState.position.target.longitude != 0.0
                ) {
                    cameraPositionState.position.target
                } else {
                    // Default to user location if the camera position is 0.0, 0.0
                    userLocation?.let { userLocation ->
                        LatLng(userLocation.latitude, userLocation.longitude)
                    } ?: LatLng(0.0, 0.0)
                }
            val radiusMiles = calcClusterItemIncludeRadiusMetersForCameraZoomLevel(
                cameraPositionState,
                isRestrictMarkerClusterRadiusActive,
                restrictMarkerClusterRadiusPhase,
                seenRadiusMiles
            ).metersToMiles()
            markers?.forEachIndexed { idx, marker ->
                if(marker.isMarkerWithinRadiusMilesOfLatLng(radiusMiles, clusterCenterLatLng)) {
                    localCachedClusterItemList.add(
                        SeeableClusterItem(
                            id = marker.id,
                            clusterItem = object : ClusterItem {
                                override fun getTitle(): String = marker.title
                                override fun getSnippet(): String = marker.id
                                override fun getPosition(): LatLng =
                                    LatLng(marker.position.latitude, marker.position.longitude)

                                override fun getZIndex(): Float = 1.0f
                            },
                            isSeen = marker.isSeen
                        )
                    )
                }
            } ?: listOf<SeeableClusterItem>()

            cachedMarkerIdToSeeableClusterItemMap.clear()
            localCachedClusterItemList.forEach { clusterItem ->
                cachedMarkerIdToSeeableClusterItemMap[clusterItem.id] = clusterItem
            }

            // Check for changes
            if(finalRenderClusterItems.size == localCachedClusterItemList.size) {
                Log.d("💿 ⚝⚝⚝ 🌟 🔧  LaunchedEffect(shouldCalculateClusterItemList): No change in cluster items size, cachedClusterItemList.size = ${localCachedClusterItemList.size}")
                isCalculatingClusterItems = false
                didUpdateClusterItems = false
                onDidCalculateClusterItemList()
                return@launch
            }

            lastRestrictClusterItemsCenterLocation = clusterCenterLatLng

            finalRenderClusterItems = localCachedClusterItemList
            Log.d(
                "💿 ⚝⚝⚝ 🟨 🔧 LaunchedEffect(shouldCalculateClusterItemList): Recalculated updatedClusterItemList: \n" +
                        "  ⎣ cachedClusterItemList.size= ${cachedClusterItemList.size}\n" +
                        "  ⎣ radius= $radiusMiles\n" +
                        "  ⎣ time= ${Clock.System.now() - startTime}"
            )
            isCalculatingClusterItems = false
            didUpdateClusterItems = true
            onDidCalculateClusterItemList()
        }
    }

    // Attempt Increase the cluster item restriction radius every 1000ms until all ClusterItems for the view are loaded
    LaunchedEffect(isRestrictMarkerClusterRadiusActive) {
        while(true) {
            withContext(Dispatchers.IO) {
                delay(
                    (kMaxRestrictMarkerClusterItemRadiusPhase * 600).milliseconds
                    - ((kMaxRestrictMarkerClusterItemRadiusPhase - restrictMarkerClusterRadiusPhase) * 500).milliseconds
                )

                if (isRestrictMarkerClusterRadiusActive) {
                    if (restrictMarkerClusterRadiusPhase < kMaxRestrictMarkerClusterItemRadiusPhase) {
                        Log.d("💿 ⚝⚝⚝ 🟨 🔧 LaunchedEffect(isRestrictMarkerClusterRadiusActive): Increasing restrictMarkerClusterRadiusFrameCount, restrictMarkerClusterRadiusFrameCount = $restrictMarkerClusterRadiusPhase")
                        restrictMarkerClusterRadiusPhase++
                        shouldCalcRestrictMarkerClusterItems = true
                    }

                    if (restrictMarkerClusterRadiusPhase == kMaxRestrictMarkerClusterItemRadiusPhase) {
                        Log.d("💿 ⚝⚝⚝ 🟨 🔧 LaunchedEffect(isRestrictMarkerClusterRadiusActive): Disabling isRestrictMarkerClusterRadiusActive")
                        isRestrictMarkerClusterRadiusActive = false
                        shouldCalcRestrictMarkerClusterItems = true
                    }
                }
            }
        }
    }

    Box(modifier.fillMaxSize()) {
        val coroutineScope = rememberCoroutineScope()
        var heatmapTileProvider by remember {
            mutableStateOf(
                HeatmapTileProvider.Builder()
                    .weightedData(
                        listOf(
                            WeightedLatLng(
                                LatLng(0.0, 0.0),
                                0.0
                            )  // default cache value (heatmap must have at least 1 item, and this wont be visible)
                        )
                    )
                    .build()
            )
        }

        // Information marker - visible after user clicks "find marker" button in details panel
        var infoMarker by remember { mutableStateOf<Marker?>(null) }
        var infoMarkerMarkerState = rememberMarkerState()
        var infoMarkerInfoWindowOpenSequencePhase by remember { mutableIntStateOf(0) }

        GoogleMap(
            cameraPositionState = cameraPositionState,
            modifier = Modifier.background(MaterialTheme.colors.background, RectangleShape),
            uiSettings = uiSettings,
            properties = properties,
            onMapClick = {
                infoMarkerMarkerState.hideInfoWindow()
                infoMarker = null
            },
            googleMapOptionsFactory = {
                GoogleMapOptions().apply {
                    this.backgroundColor(0x000000)
                }
            }
        ) {

            // Heat Map Overlay
            if (markers != null && isHeatMapEnabled) {
                val startTime = Clock.System.now()

                TileOverlay(
                    tileProvider = remember(shouldCalculateClusterItemList, markers) {
                        if (!shouldCalculateClusterItemList) {
                            // Log.d { "💿 Using cached heatmap items, cachedHeatmap = $cachedTileProvider" }
                            return@remember heatmapTileProvider
                        } else {
                            // check if the markers are different than the cached markers
                            if (markers.size == cachedMarkerIdToSeeableClusterItemMap.size) {
                                // Log.d("💿 Using cached heatmap items because list of markers has not changed, cachedHeatmap = $cachedTileProvider")
                                return@remember heatmapTileProvider
                            }

                            // Calculate the heatmap
                            val result = HeatmapTileProvider.Builder()
                                .weightedData(
                                    if (markers.isNotEmpty()) {
                                        markers.map { marker ->
                                            WeightedLatLng(
                                                LatLng(
                                                    marker.position.latitude,
                                                    marker.position.longitude
                                                ),
                                                2.0
                                            )
                                        }
                                    } else {
                                        listOf( // default cache value (heatmap must have at least 1 item, and this wont be visible)
                                            WeightedLatLng(
                                                LatLng(0.0, 0.0), 0.0
                                            )
                                        )
                                    })
                                .radius(25) // convolution filter size in pixels
                                .build()
                            // Log.d("💿 Recalculating heatmap items, markers.size= ${markers.size}, HeatmapTileProvider= $result")
                            heatmapTileProvider = result
                            return@remember result
                        }
                    },
                    state = rememberTileOverlayState(),
                    visible = isHeatMapEnabled,
                    fadeIn = true,
                    transparency = 0.0f
                )

                Log.d("💿 🔥Heatmap TileOverlay(), END Heatmap, markers.size = ${markers.size}, time= ${Clock.System.now() - startTime}")
            }

            // Render the user's location "seen" circle radius
            userLocation?.let { myLocation ->
                Circle(
                    center = LatLng(myLocation.latitude, myLocation.longitude),
                    radius = seenRadiusMiles.milesToMeters(),
                    fillColor = Color.Blue.copy(alpha = 0.4f),
                    strokeColor = Color.White.copy(alpha = 0.8f),
                    strokeWidth = 4.0f
                )

                //    // If tracking, move the camera to the user's location
                //    coroutineScope.launch {
                //        if (isTrackingEnabled) {
                //            cameraPositionState.animate(
                //                CameraUpdateFactory.newLatLng(
                //                    LatLng(
                //                        myLocation.latitude,
                //                        myLocation.longitude
                //                    )
                //                )
                //            )
                //        }
                //    }
            }

            // Show cluster marker constraint radius (scanner echo)
            if(isRestrictMarkerClusterRadiusActive) {
                Circle(
                    center = LatLng(cameraPositionState.position.target.latitude, cameraPositionState.position.target.longitude),
                    radius = calcClusterItemIncludeRadiusMetersForCameraZoomLevel(
                        cameraPositionState,
                        isRestrictMarkerClusterRadiusActive,
                        restrictMarkerClusterRadiusPhase,
                        seenRadiusMiles
                    ),
                    fillColor = Color.Green.copy(alpha = 0.03f),
                    strokeColor = Color.White.copy(alpha = 0.2f),
                    strokePattern = listOf(
                        Gap(20f),
                        Dot(),
                        Gap(20f),
                    ),
                    strokeWidth = 40.0f
                )
                // LEAVE FOR REFERENCE
                //    SphericalUtil.computeDistanceBetween( //
                //        LatLng(location.latitude, location.longitude),
                //        LatLng(location.latitude, location.longitude)
                //    )

            }

            // Render the polyline
            polyLine?.let { polyLine ->
                Polyline(
                    points = List(polyLine.size) {
                        val latLong = polyLine[it]
                        LatLng(latLong.latitude, latLong.longitude)
                    },
                    color = Color(0XFF1572D5),
                    width = 16f
                )
                Polyline(
                    points = List(polyLine.size) {
                        val latLong = polyLine[it]
                        LatLng(latLong.latitude, latLong.longitude)
                    },
                    color = Color(0XFF00AFFE),
                    width = 8f
                )
            }

            // Show Debugging Info / Radius Circles for Restriction and Last Updated Location
            if(isMarkersLastUpdatedLocationVisible) {
                // Show Markers Last Updated Location
                cachedMarkersLastUpdatedLocation?.let { cachedMarkersLastUpdatedLocation ->
                    Circle(
                        center = LatLng(
                            cachedMarkersLastUpdatedLocation.latitude,
                            cachedMarkersLastUpdatedLocation.longitude
                        ),
                        radius = kMaxReloadDistanceMiles.milesToMeters(),
                        fillColor = Color.Yellow.copy(alpha = 0.1f),
                        strokeColor = Color.White.copy(alpha = 0.3f),
                        strokeWidth = 2.0f
                    )

                    //    // Show using a rectangle
                    //    val lastLoc = cachedMarkersLastUpdatedLocation
                    //    val searchDistanceLngDegrees = distanceBetween(
                    //        lastLoc.latitude, lastLoc.longitude,
                    //        lastLoc.latitude + kMaxReloadDistanceMiles.milesToDegrees()/100.0, lastLoc.longitude,
                    //        true
                    //    )
                    //    val searchDistanceLatDegrees = //searchDistanceLatDegrees
                    //        distanceBetween(
                    //            lastLoc.latitude, lastLoc.longitude,
                    //            lastLoc.latitude, lastLoc.longitude + kMaxReloadDistanceMiles.milesToDegrees()/100.0,
                    //            true
                    //        )
                    //    val polyLineRectangle = PolylineOptions()
                    //        .add(LatLng(lastLoc.latitude - searchDistanceLatDegrees, lastLoc.longitude - searchDistanceLngDegrees))
                    //        .add(LatLng(lastLoc.latitude + searchDistanceLatDegrees, lastLoc.longitude - searchDistanceLngDegrees))
                    //        .add(LatLng(lastLoc.latitude + searchDistanceLatDegrees, lastLoc.longitude + searchDistanceLngDegrees))
                    //        .add(LatLng(lastLoc.latitude - searchDistanceLatDegrees, lastLoc.longitude + searchDistanceLngDegrees))
                    //        .add(LatLng(lastLoc.latitude - searchDistanceLatDegrees, lastLoc.longitude - searchDistanceLngDegrees))
                    //    Polyline(
                    //        points = polyLineRectangle.points,
                    //        color = Color.Yellow.copy(alpha = 0.3f),
                    //        width = 16f,
                    //        startCap = SquareCap(),
                    //        endCap = SquareCap()
                    //    )
                }

                // Show Cluster Items Restriction Radius - when camera is outside this radius, it will recalculate the cluster items.
                Circle(
                    center = lastRestrictClusterItemsCenterLocation,
                    radius = calcClusterItemIncludeRadiusMetersForCameraZoomLevel(
                        cameraPositionState,
                        isRestrictMarkerClusterRadiusActive = true,
                        restrictMarkerClusterRadiusPhase = 1,
                        seenRadiusMiles
                    ),
                    fillColor = Color.Cyan.copy(alpha = 0.3f),
                    strokeColor = Color.Red.copy(alpha = 0.7f),
                    strokePattern = listOf(
                        Gap(20f),
                        Dash(20f),
                    ),
                    strokeWidth = 3.0f
                )
                // Show camera position dot
                Circle(
                    center = LatLng(
                        cameraPositionState.position.target.latitude,
                        cameraPositionState.position.target.longitude
                    ),
                    radius = 1.0,
                    fillColor = Color.Red.copy(alpha = 0.5f),
                    strokeColor = Color.Red.copy(alpha = 0.5f),
                    strokeWidth = 10.0f
                )
            }

            Clustering(
                items = remember(isMarkersEnabled, didUpdateClusterItems) {
                    if(!didUpdateClusterItems) {
                        if (!isMarkersEnabled)
                            return@remember listOf()
                        else
                            return@remember finalRenderClusterItems
                    }

                    println("💿 ⚝⚝⚝ 🟨 🔧 Clustering(), RENDERING Clustering(items), " +
                            "isMarkersEnabled = $isMarkersEnabled, " +
                            "didUpdateClusterItems = $didUpdateClusterItems"
                    )
                    didUpdateClusterItems = false // reset
                    finalRenderClusterItems
                },
                onClusterItemInfoWindowClick = { clusterItem ->
                    coroutineScope.launch {
                        val selectedMarker = Marker(
                            id = clusterItem.snippet.toString(),
                            position = LatLong(
                                clusterItem.position.latitude,
                                clusterItem.position.longitude
                            ),
                            title = clusterItem.title ?: "",
                            alpha = 1.0f,
                        )
                        onMarkerInfoClick?.run { onMarkerInfoClick(selectedMarker) }
                    }
                },
                onClusterItemInfoWindowLongClick = { clusterItem: ClusterItem ->
                    coroutineScope.launch {
                        openNavigationAction(
                            clusterItem.position.latitude,
                            clusterItem.position.longitude,
                            clusterItem.title ?: ""
                        )
                    }
                },
                onClusterItemClick = {
                    // Hide the current marker infoWindow (if any)
                    infoMarker = null
                    infoMarkerMarkerState.hideInfoWindow()
                    false // did not completely handle the click
                },
                clusterContent = { cluster ->
                    val isAllSeen = cluster.items.all { clusterItem ->
                        cachedMarkerIdToSeeableClusterItemMap[clusterItem.snippet]?.isSeen == true
                    }
                    Box(
                        modifier = Modifier
                            .requiredHeight(40.dp)
//                            .requiredWidth(40.dp)
                            .fillMaxWidth()
                    ) {
                        Text(
                            text = cluster.items.size.toString(),
                            modifier = Modifier
                                .sizeIn(minHeight = 40.dp, minWidth = 40.dp)
                                .background(
                                    Color.Black.copy(alpha = 0.5f),
                                    CircleShape.copy(all = CornerSize(50))
                                )
                                .border(
                                    width = if (isAllSeen) 1.dp else 2.dp,
                                    color = if (isAllSeen) Color.LightGray else Color.Red,
                                    shape = CircleShape.copy(all = CornerSize(50))
                                )
                                .padding(8.dp)
                                .align(Alignment.Center),
                            color = if (isAllSeen) Color.White.copy(alpha = 0.5f) else Color.White,
                            style = MaterialTheme.typography.body1,
                            textAlign = TextAlign.Center,
                        )
                    }
                },
                clusterItemContent = { clusterItem ->
                    Box(
                        modifier = Modifier
                            .requiredHeight(50.dp)
                            .requiredWidth(50.dp)
                    ) {
                        val marker = markers?.find { it.id == clusterItem.snippet }
                        val painterRes = if(marker?.isSeen == true) {
                            lightGrayMarkerPainterResource
                        } else {
                            redMarkerPainterResource
                        }

                        if(!LocalInspectionMode.current) {
                            Image(
                                painter = painterRes,
                                contentDescription = "historical marker",
                                modifier = Modifier.fillMaxSize(),
                                contentScale = ContentScale.Fit,
                                // colorFilter = ColorFilter.tint(Color.Green, BlendMode.SrcAtop) // changes color and blows away alpha
                            )
                        } else {
                            PreviewPlaceholder("Location marker, isSeen=${marker?.isSeen}")
                        }

                        // LEAVE FOR REFERENCE for iOS
                        //    Icon(
                        //        imageVector = Icons.Filled.LocationCity,
                        //        contentDescription = "Play",
                        //        tint = Color.White
                        //    )
                    }
                },
            )

            // Information marker
            // - Note: Known bug when opening second info marker there is a noticeable flicker
            //   (1/60th frame) when the info window "jumps up" above the marker icon.
            //   No fix known, afaik.
            shouldShowInfoMarker?.let { marker ->
                // Clear the previous info marker (if any)
                infoMarker = null // allows the current infoMarker to be cleared
                infoMarkerInfoWindowOpenSequencePhase = 0
                onDidShowInfoMarker()

                // Show the new info marker
                coroutineScope.launch {
                    infoMarker = marker
                }
            }
            infoMarker?.let { marker ->
                // Render the info marker (yes, it requires it to be rendered twice, IDK WHY DAMMIT)
                if(infoMarkerInfoWindowOpenSequencePhase < 2) {
                    infoMarkerMarkerState = rememberMarkerState(
                        key = marker.id,
                        position = LatLng(
                            marker.position.latitude,
                            marker.position.longitude
                        )
                    ).also {
                        if (infoMarkerInfoWindowOpenSequencePhase < 2) {
                            it.showInfoWindow()
                            infoMarkerInfoWindowOpenSequencePhase++
                        }
                    }
                }

                Marker(
                    state = infoMarkerMarkerState,
                    alpha = marker.alpha,
                    title = marker.title,
                    snippet = marker.id,
                    icon = bitmapDescriptorFromVector(
                        context = LocalContext.current,
                        vectorResId = R.drawable.invisible_map_icon_24 // invisible icon, 48x48, spacer for the infoWindow
                    ),
                    visible = true,
                    onInfoWindowClick = {
                        onMarkerInfoClick?.run { onMarkerInfoClick(marker) }
                    }
                )
            }
        }

        // Local Map Controls
        AnimatedVisibility (
            visible = isMapOptionSwitchesVisible,
            enter = fadeIn(),
            exit = fadeOut()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxHeight()
                    .padding(16.dp)
                    .align(Alignment.BottomStart),
                horizontalAlignment = Alignment.Start,
                verticalArrangement = Arrangement.Bottom
            ) {
                SwitchWithLabel(
                    label = "Markers",
                    state = isMarkersEnabled,
                    darkOnLightTextColor = true //properties.mapType == MapType.SATELLITE
                ) {
                    isMarkersEnabled = !isMarkersEnabled
                }
                SwitchWithLabel(
                    label = "Heat Map",
                    state = isHeatMapEnabled,
                    darkOnLightTextColor = true //properties.mapType == MapType.SATELLITE
                ) {
                    isHeatMapEnabled = !isHeatMapEnabled
                }
                SwitchWithLabel(
                    label = "Satellite",
                    state = properties.mapType == MapType.SATELLITE,
                    darkOnLightTextColor = true //properties.mapType == MapType.SATELLITE
                ) {
                    properties = properties.copy(
                        mapType = if (properties.mapType == MapType.SATELLITE)
                            MapType.NORMAL
                        else
                            MapType.SATELLITE,
                    )
                }

                if (showSomething) {  // leave for testing purposes
                    Text(
                        "SOMETHING",
                        modifier = Modifier
                            .background(color = Color.Red)
                    )
                }
            }
        }

        Column(
            modifier = Modifier
                .padding(16.dp)
                .align(Alignment.BottomEnd),
            horizontalAlignment = Alignment.End
        ) {
            // Toggle tracking
            if(onToggleIsTrackingEnabledClick != null) {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(16.dp),
                    onClick = {
                        onToggleIsTrackingEnabledClick()
                    }) {
                    Icon(
                        imageVector = if (isTrackingEnabled)
                            Icons.Default.Pause
                        else Icons.Default.PlayArrow,
                        contentDescription = "Toggle track your location"
                    )
                }
            }

            // Center on user's
            if(onFindMeButtonClick != null) {
                FloatingActionButton(
                    modifier = Modifier
                        .padding(16.dp),
                    onClick = {
                        onFindMeButtonClick()
                    }) {
                    Icon(
                        imageVector = Icons.Default.MyLocation,
                        contentDescription = "Center on your location"
                    )
                }
            }
        }

        val frameRenderTime = Clock.System.now() - startTime
        Log.d("💿 GoogleMaps-Android 👾: END GoogleMap(), processing time= $frameRenderTime")

        // Need to improve performance? (by restricting the radius of the cluster items)
        if(!isRestrictMarkerClusterRadiusActive) {
            if (frameRenderTime > 48.milliseconds  // todo tune
//                && lastRestrictClusterItemsLocation != cameraPositionState.position.target // single point
                && isLatLngOutsideRadius(
                    cameraPositionState.position.target,
                    centerLatLng = lastRestrictClusterItemsCenterLocation,
                    calcClusterItemIncludeRadiusMetersForCameraZoomLevel(
                        cameraPositionState,
                        isRestrictMarkerClusterRadiusActive = true, // use the smallest radius phase for slop
                        restrictMarkerClusterRadiusPhase = 1,
                        seenRadiusMiles = seenRadiusMiles
                    ).metersToMiles()
                )
            ) {
                println("💿 GoogleMaps-Android 👾🐌: SLOW FRAME RATE GoogleMap(), frame time= $frameRenderTime")
                isRestrictMarkerClusterRadiusActive = true
                restrictMarkerClusterRadiusPhase = 0 // reset to start
            }
        }
        // reset to start if moved (after first phase updates)  // todo check outside slop radius
        if(isRestrictMarkerClusterRadiusActive
            && restrictMarkerClusterRadiusPhase >= 1)
        {
            // Did the camera move outside `last restriction` radius? then reset the restriction phase
            if(
//                lastRestrictClusterItemsLocation != cameraPositionState.position.target
                isLatLngOutsideRadius(
                    cameraPositionState.position.target,
                    centerLatLng = lastRestrictClusterItemsCenterLocation,
                    calcClusterItemIncludeRadiusMetersForCameraZoomLevel(
                        cameraPositionState,
                        isRestrictMarkerClusterRadiusActive = true, // use the smallest radius phase for slop
                        restrictMarkerClusterRadiusPhase = 1,       // use the smallest radius phase for slop
                        seenRadiusMiles = seenRadiusMiles
                    ).metersToMiles()
                )
            ) {
                restrictMarkerClusterRadiusPhase = 0 // reset to start (smallest radius)
                shouldCalcRestrictMarkerClusterItems = true
            }
        }
        startTime = Clock.System.now()
    }
}

@NoLiveLiterals
private fun isLatLngOutsideRadius(
    latLng: LatLng,
    centerLatLng: LatLng,
    radiusMiles: Double
): Boolean {
    val distanceMiles =
        distanceBetweenInMiles(
            latLng.latitude,
            latLng.longitude,
            centerLatLng.latitude,
            centerLatLng.longitude
        )

    return distanceMiles > radiusMiles
}

private const val kClusterItemIncludeRadiusMeters = 6800.0 // 4.2 miles
@NoLiveLiterals
// Based on the Camera Zoom level, calculate the radius of the circle to include in the cluster
private fun calcClusterItemIncludeRadiusMetersForCameraZoomLevel(
    cameraPositionState: CameraPositionState,
    isRestrictMarkerClusterRadiusActive: Boolean = false,
    restrictMarkerClusterRadiusPhase: Int = 0,
    seenRadiusMiles: Double = 0.0
) =
    max(
        (2.0).pow(13 - cameraPositionState.position.zoom.toDouble()) *
            if(isRestrictMarkerClusterRadiusActive)
                kClusterItemIncludeRadiusMeters /
                    ( (2.0).pow(
                        kMaxRestrictMarkerClusterItemRadiusPhase -
                                restrictMarkerClusterRadiusPhase.toDouble())
                    )
            else
                kClusterItemIncludeRadiusMeters,
        seenRadiusMiles.milesToMeters() // never smaller than the `seen radius`
    )

@NoLiveLiterals
// https://mapstyle.withgoogle.com/
fun mapStyle(): String {
    return """
    [
  {
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#242f3e"
      }
    ]
  },
  {
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#746855"
      }
    ]
  },
  {
    "elementType": "labels.text.stroke",
    "stylers": [
      {
        "color": "#242f3e"
      }
    ]
  },
  {
    "featureType": "administrative.land_parcel",
    "elementType": "labels",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "administrative.locality",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#d59563"
      }
    ]
  },
  {
    "featureType": "poi",
    "elementType": "labels.text",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "poi",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#d59563"
      }
    ]
  },
  {
    "featureType": "poi.business",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "poi.park",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#263c3f"
      }
    ]
  },
  {
    "featureType": "poi.park",
    "elementType": "labels.text",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "poi.park",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#6b9a76"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#38414e"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "geometry.stroke",
    "stylers": [
      {
        "color": "#212a37"
      }
    ]
  },
  {
    "featureType": "road",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#9ca5b3"
      }
    ]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#746855"
      }
    ]
  },
  {
    "featureType": "road.highway",
    "elementType": "geometry.stroke",
    "stylers": [
      {
        "color": "#1f2835"
      }
    ]
  },
  {
    "featureType": "road.highway",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#f3d19c"
      }
    ]
  },
  {
    "featureType": "road.highway.controlled_access",
    "stylers": [
      {
        "visibility": "simplified"
      }
    ]
  },
  {
    "featureType": "road.highway.controlled_access",
    "elementType": "geometry",
    "stylers": [
      {
        "visibility": "simplified"
      }
    ]
  },
  {
    "featureType": "road.highway.controlled_access",
    "elementType": "labels",
    "stylers": [
      {
        "visibility": "on"
      }
    ]
  },
  {
    "featureType": "road.local",
    "elementType": "labels",
    "stylers": [
      {
        "visibility": "off"
      }
    ]
  },
  {
    "featureType": "transit",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#2f3948"
      }
    ]
  },
  {
    "featureType": "transit.station",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#d59563"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "geometry",
    "stylers": [
      {
        "color": "#17263c"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "labels.text.fill",
    "stylers": [
      {
        "color": "#515c6d"
      }
    ]
  },
  {
    "featureType": "water",
    "elementType": "labels.text.stroke",
    "stylers": [
      {
        "color": "#17263c"
      }
    ]
  }
]
    """.trimIndent()

}

@Preview
@Composable
fun Square(
    modifier: Modifier = Modifier
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            RectangleShape
        }
    }
}
