import presentation.maps.Heading
import presentation.maps.Location

// Define a common class for location service
expect class GPSLocationService() {
    suspend fun getCurrentGPSLocationOneTime(): Location
    suspend fun onUpdatedGPSLocation(
        errorCallback: (String) -> Unit = {},
        locationCallback: (Location?) -> Unit
    )
    suspend fun currentHeading(callback: (Heading?) -> Unit)
    fun getLatestGPSLocation(): Location?

    fun allowBackgroundLocationUpdates()
    fun preventBackgroundLocationUpdates()
}
