

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
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
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.yield

@OptIn(MapsComposeExperimentalApi::class)
@Composable
actual fun GoogleMaps(
    modifier: Modifier,
    isControlsVisible: Boolean,
    onMarkerClick: ((MapMarker) -> Unit)?,
    onMapClick: ((LatLong) -> Unit)?,
    onMapLongClick: ((LatLong) -> Unit)?,
    markers: List<MapMarker>?,
    cameraLocationLatLong: LatLong?, // best for tracking user location
    cameraLocationBounds: CameraLocationBounds?, // best for showing a bunch of markers
    cameraPosition: CameraPosition?, // usually only used for initial camera position bc zoom level is forced
    polyLine: List<LatLong>?,
    myLocation: LatLong?
) {

    val cameraPositionState = rememberCameraPositionState()
    var uiSettings by remember {
        mutableStateOf(MapUiSettings(
            myLocationButtonEnabled = true,
            compassEnabled = false,
            mapToolbarEnabled = false,
            zoomControlsEnabled = true,
            scrollGesturesEnabled = true
        )) }
    var properties by remember {
        mutableStateOf(MapProperties(
                isMyLocationEnabled = true,  // always show the dot
                minZoomPreference = 1f,
                maxZoomPreference = 20f,
//                mapStyleOptions = MapStyleOptions(
//                    mapStyle()
//                )
            )
        )
    }

    var isTrackingEnabled by remember { mutableStateOf(false) }
    var isUpdating by remember { mutableStateOf(false) }

    var showSomething = remember { false } // leave for testing purposes

    LaunchedEffect(Unit) {
        isUpdating = true
    }

    // Usually used to setup the initial camera position (not tracking due to forcing zoom level)
    LaunchedEffect(cameraPosition) {
        cameraPosition?.let { cameraPosition ->
            println("cameraPosition = ${cameraPosition.target.latitude}, ${cameraPosition.target.longitude}")
            cameraPositionState.move(
                CameraUpdateFactory.newLatLngZoom(
                    LatLng(
                        cameraPosition.target.latitude,
                        cameraPosition.target.longitude
                    ),
                    cameraPosition.zoom
//                    cameraPositionState.position.zoom // allows users to zoom in and out while maintaining the same center, why does this work?
                )
            )
        }

        snapshotFlow { cameraPositionState.position }
            .collect { position ->
//                println("position = ${position.target.latitude}, ${position.target.longitude}")
            }
    }

    LaunchedEffect(cameraLocationBounds) {
        cameraLocationBounds?.let { cameraPositionBounds ->
            println("cameraLocationBounds = ${cameraPositionBounds.coordinates}")
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

    LaunchedEffect(cameraLocationLatLong) {
        cameraLocationLatLong?.let { cameraLocationLatLong ->
            println("cameraLocationLatLong = ${cameraLocationLatLong.latitude}, ${cameraLocationLatLong.longitude}")
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

    LaunchedEffect(isTrackingEnabled) {
        if(isTrackingEnabled) {
            myLocation?.let { myLocation ->
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

    LaunchedEffect(myLocation) {
        myLocation?.let { myLocation ->
            if(isTrackingEnabled) {
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

    Box(Modifier.fillMaxSize()) {

        val myMarkers = remember { mutableStateOf(listOf<MapMarker>()) }
        val coroutineScope = rememberCoroutineScope()

        GoogleMap(
            cameraPositionState = cameraPositionState,
            modifier = modifier,
            uiSettings = uiSettings,
            properties = properties,
            onMapClick = { latLng: LatLng ->
                     onMapClick?.let { nativeFun ->
                         nativeFun(LatLong(latLng.latitude, latLng.longitude))
                     }
                },
//            googleMapOptionsFactory = {
//                GoogleMapOptions().apply {
////                    mapType(1) // -1 = unspecified, 0 = normal, 1 = satellite, 2 = terrain, 3 = hybrid
////                    compassEnabled(false)
////                    zoomControlsEnabled(true)
////                    rotateGesturesEnabled(false)
////                    tiltGesturesEnabled(false)
////                    scrollGesturesEnabled(true)
////                    zoomGesturesEnabled(true)
//                    ambientEnabled(true)
//                }
//            }
        ) {

            // Raw markers (not clustered)
//            markers?.forEach { marker ->
////                println("marker = ${marker.key}: ${marker.position.latitude}, ${marker.position.longitude}")
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

            // Heat Map Overlay
            TileOverlay(
                tileProvider = remember {
                    HeatmapTileProvider.Builder()
                        .weightedData(markers?.map { marker ->
                            WeightedLatLng(
                                LatLng(marker.position.latitude, marker.position.longitude),
                                1.0
                            )
                        } ?: listOf())
                        .build()
                },
                state = rememberTileOverlayState(),
                visible = true,
                fadeIn = true,
                transparency = 0.1f
            )

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

            // render the user's location "talk" circle
            myLocation?.let { myLocation ->
                Circle(
                    center = LatLng(myLocation.latitude, myLocation.longitude),
                    radius = 1000.0,
                    fillColor = Color.Blue.copy(alpha = 0.2f),
                    strokeColor = Color.Blue.copy(alpha = 0.5f),
                    strokeWidth = 2.0f
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


            Clustering(
                items = remember {
                    markers?.map { marker ->
                        object : ClusterItem {
                            override fun getTitle(): String = marker.title
                            override fun getSnippet(): String = marker.subtitle
                            override fun getPosition(): LatLng =
                                LatLng(marker.position.latitude, marker.position.longitude)

                            override fun getZIndex(): Float = 1.0f
                        }
                    } ?: listOf<ClusterItem>()
                },
                onClusterClick = { cluster ->
                    println("cluster clicked")
                    true
                },
//                onClusterItemClick = { clusterItem ->
//                    println("cluster item clicked")
////                    coroutineScope.launch {
////                        myMarkers.value = myMarkers.value + MapMarker(
////                            key = clusterItem.position.toString(),
////                            position = LatLong(
////                                clusterItem.position.latitude,
////                                clusterItem.position.longitude
////                            ),
////                            title = clusterItem.title ?: "",
////                            alpha = 1.0f
////                        )
////                    }
//                    true
//                },
//                clusterContent = { cluster ->
//                    println("clusterContent")
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


        }

        if(isControlsVisible) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomStart),
                horizontalAlignment = Alignment.Start
            ) {
                if (showSomething) {  // leave for testing purposes
                    Text(
                        "SOMETHING",
                        modifier = Modifier
                            .background(color = Color.Red)
                    )
                }

                SwitchWithLabel(
                    label = "Track My location",
                    state = !uiSettings.myLocationButtonEnabled,
                    darkOnLightTextColor = properties.mapType == MapType.SATELLITE
                ) {
                    uiSettings =
                        uiSettings.copy(myLocationButtonEnabled = !uiSettings.myLocationButtonEnabled)
//                    properties =
//                        properties.copy(isMyLocationEnabled = !properties.isMyLocationEnabled)
                    isTrackingEnabled = !isTrackingEnabled
                }
                SwitchWithLabel(
                    label = "Satellite",
                    state = properties.mapType == MapType.SATELLITE,
                    darkOnLightTextColor = properties.mapType == MapType.SATELLITE
                ) {
                    properties = properties.copy(
                        mapType = if (properties.mapType == MapType.SATELLITE)
                            MapType.NORMAL
                        else
                            MapType.SATELLITE,
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
