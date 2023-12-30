
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.interop.UIKitView
import androidx.compose.ui.unit.dp
import cocoapods.GoogleMaps.GMSCameraPosition
import cocoapods.GoogleMaps.GMSCameraUpdate
import cocoapods.GoogleMaps.GMSCircle
import cocoapods.GoogleMaps.GMSCoordinateBounds
import cocoapods.GoogleMaps.GMSMapStyle
import cocoapods.GoogleMaps.GMSMapView
import cocoapods.GoogleMaps.GMSMapViewDelegateProtocol
import cocoapods.GoogleMaps.GMSMarker
import cocoapods.GoogleMaps.GMSMarker.Companion.markerImageWithColor
import cocoapods.GoogleMaps.GMSMutablePath
import cocoapods.GoogleMaps.GMSPolyline
import cocoapods.GoogleMaps.animateWithCameraUpdate
import cocoapods.GoogleMaps.kGMSTypeNormal
import cocoapods.GoogleMaps.kGMSTypeSatellite
import data.loadMarkers.milesToMeters
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.MapKit.MKMapView
import platform.UIKit.UIColor
import platform.darwin.NSObject
import presentation.maps.CameraLocationBounds
import presentation.maps.CameraPosition
import presentation.maps.LatLong
import presentation.maps.Location
import presentation.maps.Marker
import presentation.uiComponents.SwitchWithLabel

// iOS Google Maps implementation
@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun GoogleMaps(
    modifier: Modifier,
    isMapOptionSwitchesVisible: Boolean,
    isTrackingEnabled: Boolean,
    userLocation: LatLong?,
    markers: List<Marker>?,
    shouldCalcClusterItems: Boolean,
    onDidCalculateClusterItemList: () -> Unit,  // best for tracking user location
    shouldSetInitialCameraPosition: CameraPosition?,  // best for showing a bunch of markers
    shouldCenterCameraOnLatLong: LatLong?, // usually only used for initial camera position bc zoom level is forced
    onDidCenterCameraOnLatLong: () -> Unit,
    cameraLocationBounds: CameraLocationBounds?,  // Not yet implemented
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
    shouldZoomToLatLongZoom: LatLongZoom?,  // one-time zoom to a lat/long
    onDidZoomToLatLongZoom: () -> Unit,
    shouldAllowCacheReset: Boolean,
    onDidAllowCacheReset: () -> Unit,
) {

    var isMapSetupCompleted by remember { mutableStateOf(false) }

    var gmsMapViewType by remember { mutableStateOf(kGMSTypeNormal) }
    var didMapTypeChange by remember { mutableStateOf(false) }

    var didCameraPositionLatLongBoundsChange by remember { mutableStateOf(false) }
    var didCameraPositionChange by remember { mutableStateOf(false) }
    var didCameraLocationLatLongChange by remember { mutableStateOf(false) }
    var isMapRedrawTriggered by remember { mutableStateOf(true) }

    // Local UI state
    var isMarkersEnabled by remember { mutableStateOf(true) }
    // var isHeatMapEnabled by remember { mutableStateOf(false) }  // reserved for future use
    var showSomething = remember { false } // leave for testing purposes

    val googleMapView = remember(isMapRedrawTriggered) {
        GMSMapView().apply {
            setMyLocationEnabled(true)
            settings.myLocationButton = true
            settings.setMyLocationButton(true)
            settings.setScrollGestures(true)
            settings.setZoomGestures(true)
            settings.setCompassButton(false)
            this.setMapStyle(
                GMSMapStyle.styleWithJSONString(
                    mapStyle1(),
                    error = null
                )
            )
        }
    }
    val appleMapView = remember(isMapRedrawTriggered) { MKMapView() }

    LaunchedEffect(userLocation, markers) {
        if (userLocation != null) {
            isMapRedrawTriggered = true
        }
        if (markers != null) {
            isMapRedrawTriggered = true
        }
    }

    LaunchedEffect(isMarkersLastUpdatedLocationVisible) {
        isMapRedrawTriggered = true
    }

    LaunchedEffect(seenRadiusMiles) {
        isMapRedrawTriggered = true
    }

    LaunchedEffect(cameraLocationBounds) {
        if (cameraLocationBounds != null) {
            didCameraPositionLatLongBoundsChange = true
        }
    }

    LaunchedEffect(shouldSetInitialCameraPosition) {
        if (shouldSetInitialCameraPosition != null) {
            didCameraPositionChange = true
        }
    }

    LaunchedEffect(shouldCenterCameraOnLatLong) {
        if (shouldCenterCameraOnLatLong != null) {
            didCameraLocationLatLongChange = true
        }
    }

    // Only used to track selected marker
    val mapMarkers = remember(markers) { mutableStateMapOf<String,GMSMarker>() }
    var selectedMarker by remember(googleMapView.selectedMarker) { mutableStateOf(googleMapView.selectedMarker) }
    val delegate = remember { object : NSObject(), GMSMapViewDelegateProtocol {
//        override fun mapView(
//            mapView: GMSMapView,
//            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
//            didTapAtCoordinate: CValue<CLLocationCoordinate2D>
//        ) {
//            showSomething = !showSomething
//        }

        //    override fun mapView(
        //        mapView: GMSMapView,
        //        @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")
        //        didTapMarker: GMSMarker
        //    ): Boolean {
        //        val userData = didTapMarker.userData()
        //        println("map marker click ${userData}")
        //        return false
        //    }

        // Note: this shows an error, but it compiles and runs fine(!)
        override fun mapView(
            mapView: GMSMapView,
            @Suppress("PARAMETER_NAME_CHANGED_ON_OVERRIDE")  // found this hacky fix was found on jetbrains site
            didTapInfoWindowOfMarker: GMSMarker
        ) {
            val userData = didTapInfoWindowOfMarker.userData()
            onMarkerInfoClick?.let { onMarkerInfoClick ->
                val marker = markers?.find { it.id == userData }
                marker ?: return

                onMarkerInfoClick(marker)
            }
            //println("map marker click ${userData}")
        }


        //    override fun mapView(
        //        mapView: GMSMapView,
        //        didLongPressAtCoordinate: CValue<CLLocationCoordinate2D>
        //    ) {
        //        val userData = didLongPressAtCoordinate
        //        println("map marker click ${userData}")
        //        super.mapView(mapView, didLongPressAtCoordinate)
        //    }

    }}
    LaunchedEffect(shouldShowInfoMarker) {
        if (shouldShowInfoMarker != null) {
            onDidShowInfoMarker()
            selectedMarker = mapMarkers[shouldShowInfoMarker.id]
        }
    }

    Box(modifier.fillMaxSize()) {

        // Use MKMapView from Apple
        if (false) {
//            UIKitView(
//                modifier = Modifier,
//                interactive = true,
//                factory = {
//                    appleMapView.delegate = object : MKMapViewDelegateProtocol, NSObject() {
//                        override fun mapView(
//                            mapView: MKMapView,
//                            didSelectAnnotation: MKAnnotationProtocol
//                        ) {
//                            println("mapView didSelectAnnotation")
//                        }
//                    }
//
//                    appleMapView
//                },
//                update = { mapView ->
//
//                    // render the markers
//                    if (isMarkersEnabled) {
//                        mapView.removeAnnotations(mapView.annotations)
//                        markers?.forEach { marker ->
//                            val annotation1 = object : MKAnnotationProtocol, NSObject() {
//                                override fun coordinate(): CValue<CLLocationCoordinate2D> {
//                                    return CLLocationCoordinate2DMake(
//                                        latitude = marker.position.latitude,
//                                        longitude = marker.position.longitude
//                                    )
//                                }
//
//                                override fun title(): String {
//                                    return marker.title
//                                }
//
//                                override fun subtitle(): String {
//                                    return marker.subtitle
//                                }
//                            }
//                            mapView.addAnnotation(annotation1)
//
//                            val annotationView = MKMarkerAnnotationView(
//                                annotation = annotation1,
//                                reuseIdentifier = null
//                            ).apply {
//                                markerTintColor = UIColor.blueColor()
//                                //glyphImage = UIImage.imageNamed("map-marker")
//                                glyphTintColor = UIColor.whiteColor()
//                                canShowCallout = true
//                                //calloutOffset = CGPoint(x = -5.0, y = 5.0)
//                                rightCalloutAccessoryView = UIButton.buttonWithType(
//                                    buttonType = UIButtonTypeDetailDisclosure
//                                ).apply {
//                                    addTarget(
//                                        target = object : NSObject(), NSObjectProtocol {
//                                            override fun performSelector(
//                                                aSelector: CPointer<out CPointed>? /* SEL */,
//                                                withObject: Any?
//                                            ): Any? {
//                                                println("performSelector")
//                                                return null
//                                            }
//                                        },
//                                        action = NSSelectorFromString("performSelector:withObject:"),
//                                        forControlEvents = UIControlEventTouchUpInside
//                                    )
//                                }
//                            }
//                            mapView.setShowsUserLocation(true)
//                        }
//
//                        println("mapView.annotations=${mapView.annotations}, mapView.annotations.size=${mapView.annotations.size}, ${mapView.selectedAnnotations}")
//                    }
//                }
//            )
        }


        // Note: Why there so many `did_____Change` variables, and the `isRedrawMapTriggered` variable?
        // Implementing the `GoogleMaps` using UIKit inside a composable is a bit of a hack, as it's
        //       not really meant to be used inside a Composable. To work with this limitation we have to
        //       trigger independent updates of the map "parts" (ie: markers, heatmaps), and sometimes
        //       re-render the map elements, on a one-time basis. This is done by setting the
        //       `isRedrawMapTriggered` variable to `true` and then back to `false` after the map is
        //       re-rendered. This is done in the `update` block of the `UIKitView` below.
        // If it's not done like this, the UI for the map will not allow the user to move around
        //       using gestures.
        // Google Maps
        if (true) {
            UIKitView(
                modifier = Modifier,
                interactive = true,
                factory = {
                    googleMapView.apply {
                        setDelegate(delegate)
                        this.selectedMarker = selectedMarker
                        this.setMyLocationEnabled(true)
                    }

                    googleMapView
                },
                update = { view ->
                    println(
                        "view.selectedMarker=${view.selectedMarker}, " +
                        "view.selectedMarker.userData=${view.selectedMarker?.userData()}," +
                        "selectedMarker=${selectedMarker?.snippet}, " +
                        "selectedMarker.userData=${selectedMarker?.userData()}"
                    )

                    if (isTrackingEnabled) {
                        userLocation?.let { myLocation ->
                            view.animateWithCameraUpdate(
                                GMSCameraUpdate.setTarget(
                                    CLLocationCoordinate2DMake(
                                        latitude = myLocation.latitude,
                                        longitude = myLocation.longitude
                                    )
                                )
                            )
                        }
                    } else {
                        if (!isMapSetupCompleted) { // Sets the camera once during setup, this allows the user to move the map around
                            shouldSetInitialCameraPosition?.let { cameraPosition ->
                                view.animateWithCameraUpdate(
                                    GMSCameraUpdate.setTarget(
                                        CLLocationCoordinate2DMake(
                                            latitude = cameraPosition.target.latitude,
                                            longitude = cameraPosition.target.longitude
                                        )
                                    )
                                )
                            }
                        }
                    }

                    // set the map up only once, this allows the user to move the map around
                    if (!isMapSetupCompleted) {
                        view.settings.setAllGesturesEnabled(true)
                        view.settings.setScrollGestures(true)
                        view.settings.setZoomGestures(true)
                        view.settings.setCompassButton(false)

                        view.myLocationEnabled = true // show the users dot
                        view.settings.myLocationButton = false // we use our own location circle

                        isMapSetupCompleted = true
                    }

                    if (didMapTypeChange) {
                        didMapTypeChange = false
                        view.mapType = gmsMapViewType
                    }

                    if (didCameraPositionChange) {
                        didCameraPositionChange = false
                        shouldSetInitialCameraPosition?.let { cameraPosition ->
                            view.setCamera(
                                GMSCameraPosition.cameraWithLatitude(
                                    cameraPosition.target.latitude,
                                    cameraPosition.target.longitude,
                                    cameraPosition.zoom // Note Zoom level is forced here, which changes user's zoom level
                                )
                            )
                        }
                    }

                    if (didCameraLocationLatLongChange) {
                        didCameraLocationLatLongChange = false
                        shouldCenterCameraOnLatLong?.let { cameraLocation ->
                            view.animateWithCameraUpdate(
                                GMSCameraUpdate.setTarget(
                                    CLLocationCoordinate2DMake(
                                        latitude = cameraLocation.latitude,
                                        longitude = cameraLocation.longitude
                                    )
                                )
                            )
                        }
                    }

                    if (didCameraPositionLatLongBoundsChange) {
                        didCameraPositionLatLongBoundsChange = false
                        cameraLocationBounds?.let { cameraPositionLatLongBounds ->
                            var bounds = GMSCoordinateBounds()

                            cameraPositionLatLongBounds.coordinates.forEach { latLong ->
                                bounds = bounds.includingCoordinate(
                                    CLLocationCoordinate2DMake(
                                        latitude = latLong.latitude,
                                        longitude = latLong.longitude
                                    )
                                )
                            }
                            view.animateWithCameraUpdate(
                                GMSCameraUpdate.fitBounds(
                                    bounds,
                                    cameraPositionLatLongBounds.padding.toDouble()
                                )
                            )
                        }
                    }

                    if (isMapRedrawTriggered) {
                        // reset the markers & polylines, selected marker, etc.
                        val oldSelectedMarker = view.selectedMarker
                        var curSelectedMarker: GMSMarker? = null
                        val curSelectedMarkerId = view.selectedMarker?.userData as? String
                        view.clear()

                        // render the user's location "talk" circle
                        userLocation?.let {
                            GMSCircle().apply {
                                position = CLLocationCoordinate2DMake(
                                    userLocation.latitude,
                                    userLocation.longitude
                                )
                                radius = seenRadiusMiles.milesToMeters()
                                fillColor = UIColor.blueColor().colorWithAlphaComponent(0.4)
                                strokeColor = UIColor.whiteColor().colorWithAlphaComponent(0.8)
                                strokeWidth = 2.0
                                map = view
                            }
                        }

                        // render the "lastMarkerCacheUpdateLocation" circle
                        // Show Last cache loaded location
                        if (isMarkersLastUpdatedLocationVisible) {
                            cachedMarkersLastUpdatedLocation?.let { cachedMarkersLastUpdatedLocation ->
                                GMSCircle().apply {
                                    position = CLLocationCoordinate2DMake(
                                        cachedMarkersLastUpdatedLocation.latitude,
                                        cachedMarkersLastUpdatedLocation.longitude
                                    )
                                    radius = kMaxReloadRadiusMiles.milesToMeters()
                                    fillColor = UIColor.yellowColor().colorWithAlphaComponent(0.1)
                                    strokeColor = UIColor.whiteColor().colorWithAlphaComponent(0.3)
                                    strokeWidth = 2.0
                                    map = view
                                }
                            }
                        }

                        // render the markers
                        if (isMarkersEnabled) {
                            mapMarkers.clear()
                            markers?.forEach { marker ->
                                val tempMarker = GMSMarker().apply {
                                    position = CLLocationCoordinate2DMake(
                                        marker.position.latitude,
                                        marker.position.longitude
                                    )
                                    title = marker.title
                                    userData = marker.id
                                    icon = if(marker.isSeen)
                                            markerImageWithColor(UIColor.grayColor())
                                        else
                                            markerImageWithColor(UIColor.redColor())
                                    map = view
                                    snippet = marker.id
                                }
                                mapMarkers[marker.id] = tempMarker

                                if (tempMarker.userData as String == curSelectedMarkerId) {
                                    curSelectedMarker = tempMarker
                                }
                            }
                        }

                        // render the polyline
                        polyLine?.let { polyLine ->
                            val points = polyLine.map {
                                CLLocationCoordinate2DMake(it.latitude, it.longitude)
                            }
                            val path = GMSMutablePath().apply {
                                points.forEach { point ->
                                    addCoordinate(point)
                                }
                            }

                            GMSPolyline().apply {
                                this.path = path
                                this.map = view
                            }
                        }

                        // re-select the marker (if it was selected before)
                        oldSelectedMarker?.let { _ ->
                            view.selectedMarker = curSelectedMarker
                        }

                        isMapRedrawTriggered = false
                    }

                    selectedMarker?.let { selectedMarker ->
                        view.setSelectedMarker(selectedMarker)
                    }
                },
            )
        }

        // Local Map Controls
        AnimatedVisibility(
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
                Spacer(modifier = Modifier.weight(1f))
                SwitchWithLabel(
                    label = "Markers",
                    state = isMarkersEnabled,
                    darkOnLightTextColor = gmsMapViewType == kGMSTypeSatellite
                ) {
                    isMarkersEnabled = !isMarkersEnabled
                    isMapRedrawTriggered = true
                }
                // LEAVE FOR FUTURE USE
                //    SwitchWithLabel(
                //        label = "Heat Map",
                //        state = isHeatMapEnabled,
                //        darkOnLightTextColor = true //smMapViewType == kGMSTypeSatellite
                //    ) {
                //        isHeatMapEnabled = !isHeatMapEnabled
                //    }
                SwitchWithLabel(
                    label = "Satellite",
                    state = gmsMapViewType == kGMSTypeSatellite,
                    darkOnLightTextColor = gmsMapViewType == kGMSTypeSatellite
                ) { shouldUseSatellite ->
                    didMapTypeChange = true
                    gmsMapViewType =
                        if (shouldUseSatellite) kGMSTypeSatellite else kGMSTypeNormal
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
            if (onToggleIsTrackingEnabledClick != null) {
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
            if (onFindMeButtonClick != null) {
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
fun mapStyle1(): String {
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
