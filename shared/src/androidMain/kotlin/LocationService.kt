import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
import android.os.Looper
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.LocationAvailability
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import java.util.concurrent.atomic.AtomicReference
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine
import co.touchlab.kermit.Logger as Log

// Implement the LocationService in Android
actual class LocationService  {

    // Define an atomic reference to store the latest location
    private val latestLocation = AtomicReference<Location?>(null)

    // Initialize the FusedLocationProviderClient
    private val fusedLocationClient by lazy {
        LocationServices.getFusedLocationProviderClient(
            appContext
        )
    }

    // Implement the getCurrentLocation() method
    @SuppressLint("MissingPermission") // Assuming location permission check is already handled
    actual suspend fun getCurrentLocation(): Location = suspendCoroutine { continuation ->
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            location?.let {
                continuation.resume(Location(it.latitude, it.longitude))
            } ?: run {
                continuation.resumeWithException(Exception("Unable to get current location"))
            }
        }.addOnFailureListener { e ->
            continuation.resumeWithException(e)
        }
    }

    @SuppressLint("MissingPermission") // suppress missing permission check warning, we are checking permissions in the method.
//    actual suspend fun currentLocation(callback: (Location?) -> Flow<Location>) {
    actual suspend fun currentLocation(
        errorCallback: (String) -> Unit,
        locationCallback: (Location?) -> Unit
    ) {
        if (!checkLocationPermissions()) return

        val locationManager = appContext.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)

        if(!isGpsEnabled) {
            errorCallback("GPS is disabled")
        }
        if(!isNetworkEnabled) {
            errorCallback("Network is disabled")
        }

        val locationRequest = LocationRequest.Builder(1000)
            .setIntervalMillis(1000)
            .setPriority(Priority.PRIORITY_LOW_POWER)
            .setMinUpdateDistanceMeters(1.0f)
            .setWaitForAccurateLocation(false)
            .build()

        val internalLocationCallback = object : LocationCallback() {

            // Sends the location into the flow for each update
            override fun onLocationResult(result: LocationResult) {
                super.onLocationResult(result)

                result.locations.lastOrNull()?.let { location: android.location.Location ->
//                    launch {
//                        send(location) // emits the location into the flow
//                    }
                    locationCallback(Location(location.latitude, location.longitude))
                }
            }

            override fun onLocationAvailability(availability: LocationAvailability) {
                super.onLocationAvailability(availability)
                Log.d( "onLocationAvailability: ${availability.isLocationAvailable}" )
            }
        }

        fusedLocationClient.requestLocationUpdates(
            locationRequest,
            internalLocationCallback,
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

    actual suspend fun getLatestLocation(): Location? {
        return latestLocation.get()
    }
}
