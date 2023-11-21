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
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableDoubleStateOf
import androidx.compose.runtime.mutableFloatStateOf
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
import kotlinx.coroutines.yield
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

private var loopFrameRenderEndTime = Clock.System.now()
private var frameRenderCount = 0
private var restrictedClusterRadiusPhase = 0
private var frameIsRestrictedClusterRadiusActive = false
private const val kMaxRestrictedClusterRadiusPhase = 3

val blankMarkerBitmap =
    bitmapDescriptorFromVector(
        context = appContext,
        vectorResId = R.drawable.invisible_map_icon_24 // invisible icon, 48x48, spacer for the infoWindow
    )

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
    shouldCalcClusterItems: Boolean,
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
    shouldZoomToLatLongZoom: LatLongZoom?,
    onDidZoomToLatLongZoom: () -> Unit,
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
                rotationGesturesEnabled = false,
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
    var localShouldShowInfoMarker by remember(shouldShowInfoMarker) { mutableStateOf<Marker?>(shouldShowInfoMarker) }
    val rememberBlankMarkerBitmap = remember { blankMarkerBitmap }

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

    // Set Camera to `shouldCenterCameraOnLatLong` position (doesn't change zoom level)
    // Note: ONLY allowed to change the camera position ONCE per change in `shouldCenterCameraOnLatLong`.
    //       This is to prevent google maps from locking to position:
    //       ie: By only allowing the camera to change once, the user can pan around the map without
    //       the camera jumping back to the `shouldCenterCameraOnLatLong` position.
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

    // Zoom to `shouldZoomToLatLng` position
    var previousCameraLocationLatLongZoom by remember { mutableStateOf<LatLongZoom?>(null) }
    LaunchedEffect(shouldZoomToLatLongZoom) {
        if (previousCameraLocationLatLongZoom == shouldZoomToLatLongZoom) {
            return@LaunchedEffect
        }

        previousCameraLocationLatLongZoom = shouldZoomToLatLongZoom
        shouldZoomToLatLongZoom?.let { cameraLocationLatLongZoom ->
            println("💿 GoogleMaps-Android 👾: LaunchedEffect(shouldZoomToLatLongZoom), shouldZoomToLatLongZoom = $shouldZoomToLatLongZoom")
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        cameraLocationLatLongZoom.latLong.latitude,
                        cameraLocationLatLongZoom.latLong.longitude
                    ),
                    cameraLocationLatLongZoom.zoom
                )
            )
            onDidZoomToLatLongZoom()
        }
    }

    // Set Camera to User Location (ie: Tracking) (Allows user to control zoom level)
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

    // Marker Bitmaps
    val lightGrayMarkerPainterResource = painterResource("marker_lightgray.png")
    val redMarkerPainterResource = painterResource("marker_red.png")

    // Information marker - visible after user clicks "find marker" button in details panel
    var infoMarker by remember { mutableStateOf<Marker?>(null) }
    var infoMarkerState = rememberMarkerState()
    var infoMarkerInfoWindowOpenPhase by remember { mutableIntStateOf(0) }

    // ClusterItem with `isSeen` property added
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

    // Calculate Cluster items & Restricted Cluster items (to improve frame rate)
    val cachedMarkerIdToSeeableClusterItemMap =
        remember { mutableStateMapOf<MarkerIdStr, SeeableClusterItem>() }
    val cachedClusterItems = remember { mutableStateListOf<SeeableClusterItem>() }
    var finalRenderClusterItems by remember { mutableStateOf<List<SeeableClusterItem>>(listOf()) }
    var isCalculatingClusterItems by remember { mutableStateOf(false) }
    var didUpdateClusterItems by remember { mutableStateOf(false) }
    var isRestrictedClusterRadiusActive by remember { mutableStateOf(false) } // to improve frame rate
    var shouldCalcRestrictedClusterItems by remember(shouldCalcClusterItems) { mutableStateOf(false) }
    var previousRestrictedClusterCenterLatLng by remember { mutableStateOf(LatLng(0.0, 0.0)) }
    var previousRestrictedClusterRadiusMeters by remember { mutableDoubleStateOf(
        calcRestrictedClusterRadiusMetersForCameraZoomLevel(
            cameraPositionState.position.zoom,
            isRestrictedClusterRadiusActive = true,  // use the restricted cluster radius
            restrictedClusterRadiusPhase = 1,        // use size for second largest circle (phase 1)
            seenRadiusMiles
        )
    ) }
    var previousCameraZoomLevel by remember { mutableFloatStateOf(cameraPositionState.position.zoom) }
    val ioCoroutineScope = rememberCoroutineScope { Dispatchers.IO }
    LaunchedEffect(
        shouldCalcClusterItems,
        shouldCalcRestrictedClusterItems,
        cameraPositionState.position.target,
        cameraPositionState.position.zoom,
        LocalConfiguration.current.orientation,
        isRestrictedClusterRadiusActive
    ) {
        delay(250) // debounce the cluster item calculation
        frameRenderCount = 0 // reset frame count

        var isClusterCalcNeeded = false
        if(shouldCalcRestrictedClusterItems) {
            shouldCalcRestrictedClusterItems = false  // reset
            isClusterCalcNeeded = true
        }
        if(previousCameraZoomLevel != cameraPositionState.position.zoom) {
            println("📛 Zoom Level Change: previousCameraZoomLevel = $previousCameraZoomLevel, cameraPositionState.position.zoom = ${cameraPositionState.position.zoom}")
            isRestrictedClusterRadiusActive = true
            previousCameraZoomLevel = cameraPositionState.position.zoom
            restrictedClusterRadiusPhase = 0 // reset
            isClusterCalcNeeded = true
        }

        if(!isClusterCalcNeeded) {
            if (!shouldCalcClusterItems) {
                infoMarkerState.showInfoWindow()
                infoMarkerInfoWindowOpenPhase = 0
                Log.d("💿 ♦️LaunchedEffect(shouldCalculateClusterItemList): " +
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
                Log.d("💿 ♥️ LaunchedEffect(shouldCalculateClusterItemList): Using cachedMarkerIdToSeeableClusterItemMap because no isSeen in the list of markers has changed, cachedMarkerIdToSeeableClusterItemMap.size = ${cachedMarkerIdToSeeableClusterItemMap.size}")
                onDidCalculateClusterItemList()
                return@LaunchedEffect
            }
        }

        // Guard against multiple coroutines calculating the cluster items at the same time
        if(isCalculatingClusterItems) {
            Log.d("💿 🕗 LaunchedEffect(shouldCalculateClusterItemList): isCalculatingClusterItems=true, Using cachedMarkerIdToSeeableClusterItemMap items, cachedMarkerIdToSeeableClusterItemMap.size = ${cachedMarkerIdToSeeableClusterItemMap.size}")
            return@LaunchedEffect
        }

        // Calculate the cluster items - must update to change the isSeen property
        //Log.d("💿 ⚝⚝⚝ 🔧 LaunchedEffect(shouldCalculateClusterItemList): START calculating new cluster items...")
        isCalculatingClusterItems = true
        val startTime = Clock.System.now()
        ioCoroutineScope.launch {
            val localCachedClusterItemList =
                mutableListOf<SeeableClusterItem>()

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
            val clusterRadiusMeters =
                calcRestrictedClusterRadiusMetersForCameraZoomLevel(
                    cameraPositionState.position.zoom,
                    isRestrictedClusterRadiusActive,
                    restrictedClusterRadiusPhase,
                    seenRadiusMiles
                )
            val clusterRadiusMiles = clusterRadiusMeters.metersToMiles()

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

            // Collect the cluster items in the radius
            markers?.forEachIndexed { idx, marker ->
                if(marker.isMarkerWithinRadiusMilesOfLatLng(clusterRadiusMiles, clusterCenterLatLng)) {
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

            // Add to internal cached list of cluster items
            cachedMarkerIdToSeeableClusterItemMap.clear()
            localCachedClusterItemList.forEach { clusterItem ->
                cachedMarkerIdToSeeableClusterItemMap[clusterItem.id] = clusterItem
            }

            previousRestrictedClusterCenterLatLng = clusterCenterLatLng
            previousRestrictedClusterRadiusMeters =
                calcRestrictedClusterRadiusMetersForCameraZoomLevel(
                    cameraPositionState.position.zoom,
                    isRestrictedClusterRadiusActive = true,  // use the restricted cluster radius for slop
                    restrictedClusterRadiusPhase = 2, // use size for second largest circle (phase 2)
                    seenRadiusMiles
                )

            // Check for changes
            if(finalRenderClusterItems.size == localCachedClusterItemList.size) {
                //Log.d("💿 ⚝⚝⚝ 🌟 🔧  LaunchedEffect(shouldCalculateClusterItemList): No change in cluster items size, cachedClusterItemList.size = ${localCachedClusterItemList.size}")
                isCalculatingClusterItems = false
                didUpdateClusterItems = false
                onDidCalculateClusterItemList()
                return@launch
            }

            finalRenderClusterItems = localCachedClusterItemList
            //Log.d(
            //    "💿 ⚝⚝⚝ 🟨 🔧 LaunchedEffect(shouldCalculateClusterItemList): Recalculated updatedClusterItemList: \n" +
            //            "  ⎣ cachedClusterItemList.size= ${cachedClusterItems.size}\n" +
            //            "  ⎣ radius= $clusterRadiusMiles\n" +
            //            "  ⎣ time= ${Clock.System.now() - startTime}"
            //)
            isCalculatingClusterItems = false
            didUpdateClusterItems = true
            onDidCalculateClusterItemList()
        }
    }


    SideEffect {
        // Attempt Increase the `restricted cluster` radius until all ClusterItems for the view are in view (maxRestrictedClusterRadiusPhase)
        if (isRestrictedClusterRadiusActive
            && frameRenderCount > 0  // allow at least 1 frame to render before increasing the radius
        ) {
            ioCoroutineScope.launch {
                delay(150) // allow animated "radar echo" to finish

                if (restrictedClusterRadiusPhase < kMaxRestrictedClusterRadiusPhase) {
                    // Log.d("💠 🔧 LaunchedEffect(isRestrictedClusterRadiusActive): Increasing restrictedClusterRadiusPhase, restrictedClusterRadiusPhase = $restrictedClusterRadiusPhase")
                    restrictedClusterRadiusPhase++
                    shouldCalcRestrictedClusterItems = true
                }
                if (restrictedClusterRadiusPhase == kMaxRestrictedClusterRadiusPhase) {
                    // Log.d("💠 🔧 LaunchedEffect(isRestrictedClusterRadiusActive): Disabling isRestrictedClusterRadiusActive, restrictedClusterRadiusPhase = $restrictedClusterRadiusPhase")
                    isRestrictedClusterRadiusActive = false
                    shouldCalcRestrictedClusterItems = true
                }
            }
        }
    }

    Box(modifier.fillMaxSize()) {
        val frameStartTime = Clock.System.now()
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

        GoogleMap(
            cameraPositionState = cameraPositionState,
            modifier = Modifier.background(MaterialTheme.colors.background, RectangleShape),
            uiSettings = uiSettings,
            properties = properties,
            onMapClick = {
                infoMarkerState.hideInfoWindow()
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
                    tileProvider = remember(shouldCalcClusterItems, markers) {
                        if (!shouldCalcClusterItems) {
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

                //    // If tracking, move the camera to the user's location // LEAVE FOR REFERENCE
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

            // Show cluster marker constraint radius (scanner-radar echo cluster-radius indicator)
            if(frameIsRestrictedClusterRadiusActive) {
                Circle(
                    center = LatLng(
                        cameraPositionState.position.target.latitude,
                        cameraPositionState.position.target.longitude
                    ),
                    radius = calcRestrictedClusterRadiusMetersForCameraZoomLevel(
                        cameraPositionState.position.zoom,
                        isRestrictedClusterRadiusActive,
                        restrictedClusterRadiusPhase,
                        seenRadiusMiles
                    ),
                    fillColor = Color.Green.copy(alpha = 0.03f),
                    strokeColor = Color.White.copy(alpha = 0.2f),
                    strokePattern =
                        listOf(
                            Gap(20f),
                            Dot(),
                            Gap(20f),
                        )
                     ,
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
                }

                // Show the "slop" radius for ClusterItem calculation initiation
                Circle( // Outer Radius
                    center = previousRestrictedClusterCenterLatLng,
                    radius = previousRestrictedClusterRadiusMeters,
                    fillColor = Color.Cyan.copy(alpha = 0.2f),
                    strokeColor = Color.Red.copy(alpha = 0.7f),
                    strokePattern = listOf(
                        Gap(20f),
                        Dash(20f),
                    ),
                    strokeWidth = 3.0f
                )
                Circle( // Inner Radius
                    center = previousRestrictedClusterCenterLatLng,
                    radius = previousRestrictedClusterRadiusMeters / 2.0,
                    fillColor = Color.Cyan.copy(alpha = 0.2f),
                    strokeColor = Color.Red.copy(alpha = 0.7f),
                    strokePattern = listOf(
                        Gap(20f),
                        Dash(10f),
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
                    strokeWidth = 40.0f
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

                    //Log.d("💿 ⚝⚝⚝ 🟨 🔧 Clustering(), RENDERING Clustering(items), " +
                    //        "isMarkersEnabled = $isMarkersEnabled, " +
                    //        "didUpdateClusterItems = $didUpdateClusterItems"
                    //)
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
                    // Hide the current infoMarker infoWindow (if any)
                    infoMarkerState.hideInfoWindow()

                    // Set the new info marker
                    localShouldShowInfoMarker = Marker(
                        id = it.snippet.toString(),
                        position = LatLong(
                            it.position.latitude,
                            it.position.longitude
                        ),
                        title = it.title ?: "",
                        alpha = 1.0f,
                    )

                    // Center the camera on the marker
                    coroutineScope.launch {
                        yield() // wait for the infoMarker to be cleared

                        // move camera to marker
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLng(
                                LatLng(
                                    it.position.latitude,
                                    it.position.longitude
                                )
                            )
                        )
                    }

                    true // true = DID completely handle the click
                },
                clusterContent = { cluster ->
                    val isAllSeen = cluster.items.all { clusterItem ->
                        cachedMarkerIdToSeeableClusterItemMap[clusterItem.snippet]?.isSeen == true
                    }
                    Box(
                        modifier = Modifier
                            .requiredHeight(40.dp)
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
            localShouldShowInfoMarker?.let { marker ->
                // Clear the previous InfoMarker
                infoMarker = null
                localShouldShowInfoMarker = null // reset

                // Show the new info marker
                coroutineScope.launch {
                    yield() // wait for the infoMarker to be cleared
                    infoMarker = marker
                    infoMarkerInfoWindowOpenPhase = 0
                    shouldShowInfoMarker?.run{ onDidShowInfoMarker() } // reset
                }
            }
            infoMarker?.let { marker ->
                //println("🔷🔷 infoMarker: ${infoMarker?.title}, infoMarkerInfoWindowOpenPhase = $infoMarkerInfoWindowOpenPhase")
                // Render the info marker (yes, it requires it to be rendered twice, IDK WHY DAMMIT)
                if(infoMarkerInfoWindowOpenPhase < 2) {
                    infoMarkerState = rememberMarkerState(
                        key = marker.id,
                        position = LatLng(
                            marker.position.latitude,
                            marker.position.longitude
                        )
                    )
                    .also { infoMarkerState ->
                        if(infoMarkerInfoWindowOpenPhase >= 2) {
                            infoMarkerState.showInfoWindow()  // necessary to show the infoWindow (ugh)
                        }
                        infoMarkerInfoWindowOpenPhase++
                    }
                }

                Marker(
                    state = infoMarkerState
                        .also {
                           it.showInfoWindow() // necessary to show the infoWindow
                        },
                    alpha = infoMarker?.alpha ?: 0f,
                    title = infoMarker?.title ?: "",
                    tag = infoMarker?.id ?: "",
                    snippet = infoMarker?.id ?: "",
                    icon = rememberBlankMarkerBitmap,
                    // infoWindowAnchor = Offset(0.5f, -.20f), // LEAVE FOR REFERENCE
                    visible = true, //infoMarker != null,
                    onInfoWindowClick = {
                        onMarkerInfoClick?.run {
                            onMarkerInfoClick(infoMarker ?: return@run)
                        }
                    },
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

        // FAB's
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

        //////////////////////////////////////////////////////////////
        // Tune Cluster Radius for improved Render time Performance //
        //////////////////////////////////////////////////////////////

        frameRenderCount++
        val fullLoopFrameRenderTime = Clock.System.now() - loopFrameRenderEndTime
        if(fullLoopFrameRenderTime > 20.milliseconds) {
            Log.d(
                "💿 GoogleMaps-Android 👾: END GoogleMap(), " +
                "fullLoopFrameRenderTime= $fullLoopFrameRenderTime, " +
                "compose render Time = ${Clock.System.now() - frameStartTime}, " +
                "frameRenderCount = $frameRenderCount"
            )
        }

        fun startRestrictedClusterRadius() {
            isRestrictedClusterRadiusActive = true // start restricting
            restrictedClusterRadiusPhase = 0 // reset to start

            infoMarkerInfoWindowOpenPhase = 0 // reset infoMarker to re-open infoWindow
        }

        // Need to improve performance? (by restricting the radius of the cluster items)
        if(!isRestrictedClusterRadiusActive
            && frameRenderCount > 1 // skip the first frame after the cluster items are calculated
        ) {
            // If camera location is outside OUTER `restricted cluster` slop radius, then start restricting & recalculate cluster items.
            if(isLatLngOutsideRadiusMiles(
                cameraPositionState.position.target,
                centerLatLng = previousRestrictedClusterCenterLatLng,
                previousRestrictedClusterRadiusMeters.metersToMiles()  // OUTER radius
            )) {
                //println("💿 GoogleMaps-Android 👾🐌: SLOW FRAME RATE - OUTER, frame time= $fullLoopFrameRenderTime")
                startRestrictedClusterRadius()
            }

            // If frameTime over 200ms, and outside INNER `restricted cluster` radius, then start restricting & recalculate cluster items.
            if(fullLoopFrameRenderTime > 150.milliseconds  // todo tune
                && isLatLngOutsideRadiusMiles(
                    cameraPositionState.position.target,
                    centerLatLng = previousRestrictedClusterCenterLatLng,
                    previousRestrictedClusterRadiusMeters.metersToMiles() / 2.0 // INNER radius
                )
            ) {
                //println("💿 GoogleMaps-Android 👾🐌: SLOW FRAME RATE - INNER, frame time= $fullLoopFrameRenderTime")
                startRestrictedClusterRadius()
            }
        }
        // Reset phase to start if camera is moved (only after first phase)
        if(isRestrictedClusterRadiusActive
            && restrictedClusterRadiusPhase >= 1
        ) {
            // Did the camera move outside `restricted cluster` radius? Then reset the restriction phase.
            if(
                isLatLngOutsideRadiusMiles(
                    cameraPositionState.position.target,
                    centerLatLng = previousRestrictedClusterCenterLatLng,
                    previousRestrictedClusterRadiusMeters.metersToMiles()
                )
            ) {
                startRestrictedClusterRadius()
            }
        }
        loopFrameRenderEndTime = Clock.System.now()
        frameIsRestrictedClusterRadiusActive = isRestrictedClusterRadiusActive
    }
}

@NoLiveLiterals
private fun isLatLngOutsideRadiusMiles(
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
private fun calcRestrictedClusterRadiusMetersForCameraZoomLevel(
    cameraZoomLevel: Float,
    isRestrictedClusterRadiusActive: Boolean = false,
    restrictedClusterRadiusPhase: Int = 0,
    minimumRadiusMiles: Double = 0.0 // minimum radius
) =
    max(
        (2.0).pow(13 - cameraZoomLevel.toDouble()) *
            if(isRestrictedClusterRadiusActive)
                kClusterItemIncludeRadiusMeters /
                    ( (2.0).pow(
                        kMaxRestrictedClusterRadiusPhase -
                                restrictedClusterRadiusPhase.toDouble())
                    )
            else
                kClusterItemIncludeRadiusMeters,
        1000.0 //minimumRadiusMiles.milesToMeters() // never smaller than the `seen radius`
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
