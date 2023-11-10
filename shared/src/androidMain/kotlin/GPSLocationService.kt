import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import androidx.compose.runtime.NoLiveLiterals
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import presentation.maps.Heading
import presentation.maps.Location
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import co.touchlab.kermit.Logger as Log

// Implement the LocationService in Android
@SuppressLint("MissingPermission") // Assuming location permission check is already handled
@NoLiveLiterals
actual class GPSLocationService  {

    // Define an atomic reference to store the latest location
    private val latestLocation = AtomicReference<Location?>(null)

    // Initialize the FusedLocationProviderClient source of location data
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(
            appContext
        )
    }

    private var errorCallback: ((String) -> Unit)? = null
    private var locationUpdateCallback: ((Location?) -> Unit)? = null
    private var internalLocationCallback: LocationCallback? = null

    private val locationRequest = LocationRequest.Builder(kUpdateInterval)
        .setIntervalMillis(kUpdateInterval)
        .setPriority(Priority.PRIORITY_LOW_POWER)
        .setMinUpdateDistanceMeters(1.0f)
        .setWaitForAccurateLocation(false)
        .build()

    // Gets location 1 time only. (useful for testing)
    // WARNING: Should NOT be used for continuous location updates or in conjunction with currentLocation()
    @SuppressLint("MissingPermission") // Assuming location permission check is already handled
    actual suspend fun getCurrentGPSLocationOneTime(): Location = suspendCoroutine { continuation ->

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let { androidOsLocation ->
                val updatedLocation = Location(androidOsLocation.latitude, androidOsLocation.longitude)
                latestLocation.set(updatedLocation)
                continuation.resume(updatedLocation)
            } ?: run {
                continuation.resumeWithException(Exception("Unable to get current location"))
            }
        }.addOnFailureListener { e ->
            continuation.resumeWithException(e)
        }
    }

    @SuppressLint("MissingPermission") // suppress missing permission check warning, we are checking permissions in the method.
    // actual suspend fun onUpdatedGPSLocation(callback: (Location?) -> Flow<Location>) {  // LEAVE FOR REFERENCE - emits a flow of locations
    actual suspend fun onUpdatedGPSLocation(
        errorCallback: (errorMessage: String) -> Unit,
        locationCallback: (newLocation: Location?) -> Unit
    ) {
        startGPSLocationUpdates(errorCallback, locationCallback) // keeps requesting location updates
    }

    @SuppressLint("MissingPermission") // suppress missing permission check warning, we are checking permissions in the method.
    private fun startGPSLocationUpdates(
        errorCallback: ((String) -> Unit)? = null,
        locationCallback: ((Location?) -> Unit)? = null
    ) {
        if (!checkLocationPermissions()) return
        if(locationCallback == this@GPSLocationService.locationUpdateCallback) return // already using same callback
        this@GPSLocationService.locationUpdateCallback = locationCallback
        this@GPSLocationService.errorCallback = errorCallback

        // Check if GPS and Network location is enabled
        val locationManager =
            appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if (!isGpsEnabled) {
            errorCallback?.let { errorCallback("GPS is disabled") }
            Log.d("GPS is disabled")
            return
        }
        if (!isNetworkEnabled) {
            errorCallback?.let { errorCallback("Network is disabled") }
            Log.d("Network is disabled")
            return
        }

        // Setup the location callback
        internalLocationCallback?.let { fusedLocationClient.removeLocationUpdates(it) }
        internalLocationCallback = object : LocationCallback() {
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)

                result.locations.lastOrNull()?.let { androidOsLocation: android.location.Location ->
                    //launch {  // For flow - leave for reference
                    //    send(androidOsLocation) // emits the androidOsLocation into the flow
                    //}
                    val updatedLocation = Location(androidOsLocation.latitude, androidOsLocation.longitude)
                    latestLocation.set(updatedLocation)

                    locationCallback?.let {
                        locationCallback(updatedLocation)
                    }
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                super.onLocationAvailability(availability)
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            internalLocationCallback!!,
            Looper.getMainLooper()
        )
    }

    private fun checkLocationPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                appContext,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }

        return false
    }

    actual suspend fun currentHeading(callback: (Heading?) -> Unit) {

    }

    actual fun getLatestGPSLocation(): Location? {
        return latestLocation.get()
    }

    actual fun allowBackgroundLocationUpdates() {
        CoroutineScope(Dispatchers.Main).launch {
            _intentFlow.emit(Intent(GPSLocationService.ACTION_START_BACKGROUND_UPDATES))
        }
    }
    actual fun preventBackgroundLocationUpdates() {
        CoroutineScope(Dispatchers.Main).launch {
            _intentFlow.emit(Intent(GPSLocationService.ACTION_STOP_BACKGROUND_UPDATES))
        }
    }

    companion object {
        const val ACTION_START_BACKGROUND_UPDATES = "ACTION_START_BACKGROUND_UPDATES"
        const val ACTION_STOP_BACKGROUND_UPDATES = "ACTION_STOP_BACKGROUND_UPDATES"

        const val kUpdateInterval = 1000L
    }
}
