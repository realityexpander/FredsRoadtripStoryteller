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
import cocoapods.GoogleMaps.GMSMapView
import cocoapods.GoogleMaps.GMSMapViewDelegateProtocol
import cocoapods.GoogleMaps.GMSMarker
import cocoapods.GoogleMaps.GMSMarker.Companion.markerImageWithColor
import cocoapods.GoogleMaps.GMSMutablePath
import cocoapods.GoogleMaps.GMSPolyline
import cocoapods.GoogleMaps.animateWithCameraUpdate
import cocoapods.GoogleMaps.kGMSTypeNormal
import cocoapods.GoogleMaps.kGMSTypeSatellite
import kotlinx.cinterop.CValue
import kotlinx.cinterop.ExperimentalForeignApi
import loadMarkers.milesToMeters
import platform.CoreLocation.CLLocationCoordinate2D
import platform.CoreLocation.CLLocationCoordinate2DMake
import platform.UIKit.UIColor
import platform.darwin.NSObject


@OptIn(ExperimentalForeignApi::class)
@Composable
actual fun GoogleMaps(
    modifier: Modifier,
    isControlsVisible: Boolean,
    isTrackingEnabled: Boolean,
    userLocation: LatLong?,
    markers: List<MapMarker>?,
    shouldUpdateMapMarkers: Boolean,
    cameraOnetimePosition: CameraPosition?,  // best for tracking user location
    cameraLocationLatLong: LatLong?,  // best for showing a bunch of markers
    cameraLocationBounds: CameraLocationBounds?, // usually only used for initial camera position bc zoom level is forced
    polyLine: List<LatLong>?,
    onMapClick: ((LatLong) -> Unit)?,  // shows the user's location with a 100m radius circle
    onMapLongClick: ((LatLong) -> Unit)?,
    onMarkerClick: ((MapMarker) -> Unit)?
) {
    val googleMapView = remember { GMSMapView() }

    var isMapSetupCompleted by remember { mutableStateOf(false) }

    var gmsMapViewType by remember { mutableStateOf(kGMSTypeNormal) }
    var didMapTypeChange by remember { mutableStateOf(false) }

    var didCameraPositionLatLongBoundsChange by remember { mutableStateOf(false) }
    var didCameraPositionChange by remember { mutableStateOf(false) }
    var didCameraLocationLatLongChange by remember { mutableStateOf(false) }
    var isMapRedrawTriggered by remember { mutableStateOf(false) }

    // Local UI state
    var isMarkersEnabled by remember { mutableStateOf(true) }
    // var isHeatMapEnabled by remember { mutableStateOf(false) }  // reserved for future use
    var showSomething = remember { false } // leave for testing purposes

    LaunchedEffect(userLocation, markers) {
        if (userLocation != null) {
            isMapRedrawTriggered = true
        }
        if (markers != null) {
            isMapRedrawTriggered = true
        }
    }

    LaunchedEffect(cameraLocationBounds) {
        if (cameraLocationBounds != null) {
            didCameraPositionLatLongBoundsChange = true
        }
    }

    LaunchedEffect(cameraOnetimePosition) {
        if (cameraOnetimePosition != null) {
            didCameraPositionChange = true
        }
    }

    LaunchedEffect(cameraLocationLatLong) {
        if (cameraLocationLatLong != null) {
            didCameraLocationLatLongChange = true
        }
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
    Box(Modifier.fillMaxSize()) {
        // Google Maps
        UIKitView(
            modifier = modifier.fillMaxSize(),
            interactive = true,
            factory = {
//                // Does not work yet... :(
//                    googleMapView.delegate = object : NSObject(), GMSMapViewDelegateProtocol {
//                        override fun mapView(
//                            mapView: GMSMapView,
//                            didTapAtCoordinate: CValue<CLLocationCoordinate2D>
//                        ) {
//                            showSomething = !showSomething
//                        }
//
////                        override fun mapView(
////                            mapView: GMSMapView,
////                            didTapMarker: GMSMarker
////                        ): Boolean {
////                            val userData = didTapMarker.userData()
////                            println("map marker click ${userData}")
////                            return true
////                        }
//                    }

                googleMapView
            },
            update = { view ->
                if(isTrackingEnabled) {
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
                    if(!isMapSetupCompleted) { // Sets the camera once during setup, this allows the user to move the map around
                        cameraOnetimePosition?.let { cameraPosition ->
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
                if(!isMapSetupCompleted) {
                    view.settings.setAllGesturesEnabled(true)
                    view.settings.setScrollGestures(true)
                    view.settings.setZoomGestures(true)
                    view.settings.setCompassButton(false)

                    view.myLocationEnabled = true // show the users dot
                    view.settings.myLocationButton = false // we use our own location circle

                    isMapSetupCompleted = true
                }

                if(didMapTypeChange) {
                    didMapTypeChange = false
                    view.mapType = gmsMapViewType
                }

//                if(didMyLocationButtonVisibilityChange) {
//                    didMyLocationButtonVisibilityChange = false
//                    view.settings.myLocationButton = !isTrackingEnabled
//                }

                if(didCameraPositionChange) {
                    didCameraPositionChange = false
                    cameraOnetimePosition?.let { cameraPosition ->
                        view.setCamera(
                            GMSCameraPosition.cameraWithLatitude(
                                cameraPosition.target.latitude,
                                cameraPosition.target.longitude,
                                cameraPosition.zoom // Note Zoom level is forced here, which changes user's zoom level
                            )
                        )
                    }
                }

                if(didCameraLocationLatLongChange) {
                    didCameraLocationLatLongChange = false
                    cameraLocationLatLong?.let { cameraLocation ->
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

                if(didCameraPositionLatLongBoundsChange) {
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

                if(isMapRedrawTriggered) {
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
                            radius = kTalkRadiusMiles.milesToMeters()
                            fillColor = UIColor.blueColor().colorWithAlphaComponent(0.4)
                            strokeColor = UIColor.whiteColor().colorWithAlphaComponent(0.8)
                            strokeWidth = 4.0
                            map = view
                        }
                    }

                    // render the markers
                    if(isMarkersEnabled) {
                        markers?.forEach { marker ->
                            val tempMarker = GMSMarker().apply {
                                position = CLLocationCoordinate2DMake(
                                    marker.position.latitude,
                                    marker.position.longitude
                                )
                                title = marker.title
                                userData = marker.key
                                map = view
                                icon = markerImageWithColor(UIColor.blueColor())
                            }

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
            },
        )

        // Local Map Controls
        if(isControlsVisible) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .align(Alignment.BottomStart),
                horizontalAlignment = Alignment.Start
            ) {
                SwitchWithLabel(
                    label = "Markers",
                    state = isMarkersEnabled,
                    darkOnLightTextColor = gmsMapViewType == kGMSTypeSatellite
                ) {
                    isMarkersEnabled = !isMarkersEnabled
                    isMapRedrawTriggered = true
                }
                // LEAVE FOR FUTURE USE
//                SwitchWithLabel(
//                    label = "Heat Map",
//                    state = isHeatMapEnabled,
//                    darkOnLightTextColor = true //smMapViewType == kGMSTypeSatellite
//                ) {
//                    isHeatMapEnabled = !isHeatMapEnabled
//                }
                SwitchWithLabel(
                    label = "Satellite",
                    state = gmsMapViewType == kGMSTypeSatellite,
                    darkOnLightTextColor = gmsMapViewType == kGMSTypeSatellite
                ) { shouldUseSatellite ->
                    didMapTypeChange = true
                    gmsMapViewType = if (shouldUseSatellite) kGMSTypeSatellite else kGMSTypeNormal
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
    }
}
