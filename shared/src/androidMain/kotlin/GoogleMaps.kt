import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.FloatingActionButton
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
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
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.CameraUpdateFactory
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
import com.google.maps.android.compose.MarkerState
import com.google.maps.android.compose.Polyline
import com.google.maps.android.compose.TileOverlay
import com.google.maps.android.compose.clustering.Clustering
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberTileOverlayState
import com.google.maps.android.heatmaps.HeatmapTileProvider
import com.google.maps.android.heatmaps.WeightedLatLng
import components.SwitchWithLabel
import kotlinx.coroutines.launch
import loadMarkers.milesToMeters
import co.touchlab.kermit.Logger as Log

@OptIn(MapsComposeExperimentalApi::class)
@Composable
actual fun GoogleMaps(
    modifier: Modifier,
    isMapOptionSwitchesVisible: Boolean,
    isTrackingEnabled: Boolean,
    userLocation: LatLong?,
    markers: List<MapMarker>?,
    shouldRedrawMapMarkers: Boolean,
    cameraOnetimePosition: CameraPosition?, // Best for setting initial camera position bc zoom level is forced
    cameraLocationLatLong: LatLong?, // Best for tracking user location
    cameraLocationBounds: CameraLocationBounds?, // Best for showing a bunch of markers
    polyLine: List<LatLong>?,
    onMapClick: ((LatLong) -> Unit)?,
    onMapLongClick: ((LatLong) -> Unit)?,
    onMarkerClick: ((MapMarker) -> Unit)?,
    talkRadiusMiles: Double,
    cachedMarkersLastUpdatedLocation: Location?,
    onToggleIsTrackingEnabledClick: (() -> Unit)?,
    onFindMeButtonClick: (() -> Unit)?,
    isMarkersLastUpdatedLocationVisible: Boolean
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
                maxZoomPreference = 20f,
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
    LaunchedEffect(cameraOnetimePosition) {
        cameraOnetimePosition?.let { cameraPosition ->
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

        // Follow the camera position
        snapshotFlow { cameraPositionState.position }
            .collect { position ->
                // Log.d { "position = ${position.target.latitude}, ${position.target.longitude}" }
            }
    }

    // Set Camera to Bounds (zoom level is forced)
    LaunchedEffect(cameraLocationBounds) {
        cameraLocationBounds?.let { cameraPositionBounds ->
            // Log.d { "cameraLocationBounds = ${cameraPositionBounds.coordinates}"  }
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

    // Set Camera to LatLong position (doesn't change zoom level)
    // Note: only allowed to change the camera position once per change in cameraLocationLatLong.
    //       This is to prevent the screen from locking the location. By only allowing the camera
    //       to change once, the user can pan around the map without the camera jumping back to
    //       the cameraLocationLatLong position.
    var previousCameraLocationLatLong by remember { mutableStateOf<LatLong?>(null) }
    LaunchedEffect(cameraLocationLatLong) {
        cameraLocationLatLong?.let { cameraLocationLatLong ->
            if (previousCameraLocationLatLong == cameraLocationLatLong) {
                return@LaunchedEffect
            }

            previousCameraLocationLatLong = cameraLocationLatLong
            cameraPositionState.animate(
                CameraUpdateFactory.newLatLng(
                    LatLng(
                        cameraLocationLatLong.latitude,
                        cameraLocationLatLong.longitude
                    )
                )
            )
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

//    Box(modifier) {
    Box(modifier.fillMaxSize()) {

        val myMarkers = remember { mutableStateOf(listOf<MapMarker>()) }
        val coroutineScope = rememberCoroutineScope()
        val cachedMarkers = remember { mutableStateListOf<ClusterItem>() }
        var cachedTileProvider by remember {
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
//            modifier = Modifier.background(Color.Black, RectangleShape),
            modifier = Modifier.background(Color(0xFF1F44CC), RectangleShape), // todo use theme colors
            uiSettings = uiSettings,
            properties = properties,
            onMapClick = { latLng: LatLng ->
                onMapClick?.let { nativeFun ->
                    nativeFun(LatLong(latLng.latitude, latLng.longitude))
                }
            },
        ) {

            // Heat Map Overlay
            if (markers != null) {
                TileOverlay(
                    tileProvider = remember(shouldRedrawMapMarkers, markers) {
                        if (!shouldRedrawMapMarkers) {
                            // Log.d { "Using cached heatmap items, cachedHeatmap = $cachedTileProvider" }
                            return@remember cachedTileProvider
                        } else {
                            // check if the markers are different than the cached markers
                            if (markers.size == cachedMarkers.size) {
                                // Log.d { "Using cached heatmap items because list of markers has not changed, cachedHeatmap = $cachedTileProvider" }
                                return@remember cachedTileProvider
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
                            // Log.d("Recalculating heatmap items, markers.size= ${markers.size}, HeatmapTileProvider= $result")
                            cachedTileProvider = result
                            return@remember result
                        }
                    },
                    state = rememberTileOverlayState(),
                    visible = isHeatMapEnabled,
                    fadeIn = true,
                    transparency = 0.0f
                )
            }

            // temporary markers
            myMarkers.value.forEach { marker ->
                Marker(
                    state = MarkerState(
                        position = LatLng(marker.position.latitude, marker.position.longitude)
                    ),
                    alpha = marker.alpha,
                    title = marker.title,
                )
            }

            // Render the user's location "talk" circle
            userLocation?.let { myLocation ->
                Circle(
                    center = LatLng(myLocation.latitude, myLocation.longitude),
//                    radius = kTalkRadiusMiles.milesToMeters(),
                    radius = talkRadiusMiles.milesToMeters(),
                    fillColor = Color.Blue.copy(alpha = 0.4f),
                    strokeColor = Color.White.copy(alpha = 0.8f),
                    strokeWidth = 4.0f
                )

//                // If tracking, move the camera to the user's location
//                coroutineScope.launch {
//                    if (isTrackingEnabled) {
//                        cameraPositionState.animate(
//                            CameraUpdateFactory.newLatLng(
//                                LatLng(
//                                    myLocation.latitude,
//                                    myLocation.longitude
//                                )
//                            )
//                        )
//                    }
//                }
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

            // Show Last cache loaded location
            if(isMarkersLastUpdatedLocationVisible) {
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

                    //    // Show using a rectangle - todo distance estimates are a bit off
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
            }

            Clustering(
                items = remember(shouldRedrawMapMarkers, markers, isMarkersEnabled) {
                    if (!isMarkersEnabled) {
                        // Log.d { "Using empty cluster items" }
                        return@remember listOf<ClusterItem>()
                    }

                    if (!shouldRedrawMapMarkers) {
                        // Log.d { "Using cached cluster items, cachedMarkers.size = ${cachedMarkers.size}" }
                        return@remember cachedMarkers
                    } else {
                        // check if the markers are different than the cached markers
                        if (markers?.size == cachedMarkers.size) {
                            // Log.d { "Using cached cluster items because list of markers has not changed, cachedMarkers.size = ${cachedMarkers.size}" }
                            return@remember cachedMarkers
                        }

                        // Calculate the cluster items
                        val result = markers?.map { marker ->
                            object : ClusterItem {
                                override fun getTitle(): String = marker.title
                                override fun getSnippet(): String = marker.subtitle
                                override fun getPosition(): LatLng =
                                    LatLng(marker.position.latitude, marker.position.longitude)

                                override fun getZIndex(): Float = 1.0f
                            }
                        } ?: listOf<ClusterItem>()
                        // Log.d { "Recalculating cluster items, markers.size= ${markers?.size}, result.size= ${result.size}" }
                        cachedMarkers.clear()
                        cachedMarkers.addAll(result)

                        return@remember result
                    }
                },
                onClusterClick = { cluster ->
                    Log.d { "cluster clicked" }
                    true
                },
                onClusterItemClick = { clusterItem ->
                    Log.d { "cluster item clicked" }
                    coroutineScope.launch {
                        val selectedMarker = MapMarker(
                            key = clusterItem.position.toString(),
                            position = LatLong(
                                clusterItem.position.latitude,
                                clusterItem.position.longitude
                            ),
                            title = clusterItem.title ?: "",
                            alpha = 1.0f
                        )
                        onMarkerClick?.let { nativeFun ->
                            nativeFun(selectedMarker)
                        }

                        println("selectedMarker = ${selectedMarker.title}")
                    }
                    false
                },
//                clusterContent = { cluster ->
//                    Log.d { "clusterContent" }
//                    Marker(
//                        state = MarkerState(
//                            position = LatLng(cluster.position.latitude, cluster.position.longitude)
//                        ),
//                        alpha = 1.0f,
//                        title = cluster.items.size.toString()
//                    )
//                },
//                clusterItemContent = { clusterItem ->
//                    Box(
//                        modifier = Modifier
//                            .requiredHeight(50.dp)
//                            .requiredWidth(50.dp)
//                            .background(
//                                Color.Blue.copy(alpha = 0.5f)
//                            )
//                    ) {
//                        Text(
//                            text = clusterItem.title ?: "",
//                            color = Color.White
//                        )
//                    }
//                }
            )


            // LEAVE FOR REFERENCE
            // Raw markers (not clustered)
//            markers?.forEach { marker ->
////                Log.d { "marker = ${marker.key}: ${marker.position.latitude}, ${marker.position.longitude}" }
//                Marker(
////                    state = rememberMarkerState(
////                        key = marker.key,
////                        position = LatLng(marker.position.latitude, marker.position.longitude)
////                    ),
//                    state = MarkerState(
//                        position = LatLng(marker.position.latitude, marker.position.longitude)
//                    ),
//                    alpha = marker.alpha,
//                    title = marker.title
//                )
//            }

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
    }
}

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
        "elementType": "geometry.fill",
        "stylers": [
          {
            "lightness": -5
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
        "featureType": "road.arterial",
        "elementType": "labels",
        "stylers": [
          {
            "visibility": "off"
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
        "elementType": "labels",
        "stylers": [
          {
            "visibility": "off"
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
        "featureType": "road.local",
        "stylers": [
          {
            "visibility": "off"
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
