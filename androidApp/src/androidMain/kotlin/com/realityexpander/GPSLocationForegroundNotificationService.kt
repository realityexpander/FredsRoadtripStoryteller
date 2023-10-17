package com.realityexpander

import GPSLocationService
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.IBinder
import androidx.core.app.NotificationCompat
//import com.realityexpander.common.R  // uses the common module R file
import com.realityexpander.R as AppR  // uses the androidApp module R file
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import co.touchlab.kermit.Logger as Log

// Used solely to update the notification (required for Android 8.0+)
// Must have this for background location updates on Android.
class GPSLocationForegroundNotificationService: Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
//    private lateinit var locationClient: LocationClient
    private lateinit var gpsLocationClient: GPSLocationService

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        gpsLocationClient = GPSLocationService()

        // for flow - leave for reference
        //    locationClient = LocationClientImpl(
        //        applicationContext,
        //        LocationServices.getFusedLocationProviderClient(applicationContext)
        //    )
    }

    // Should be called "onReceiveCommand" instead of "onStartCommand"
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTION_START_NOTIFICATION_SERVICE -> start()
            ACTION_STOP_NOTIFICATION_SERVICE -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Looking for Historical Markers...")
            .setContentText("Location: retrieving...")
            // .setSmallIcon(AppR.drawable.ic_notification_icon) // uses the common module R file
            .setSmallIcon(AppR.drawable.ic_launcher_background)  // uses the androidApp module R file
            .setOngoing(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        // Uses a callback to update the notification
        serviceScope.launch {
            gpsLocationClient.onUpdatedGPSLocation(
                errorCallback = { error ->
                    Log.w("com.realityexpander.LocationForegroundService, Error: $error" )
                }
            ) { newLocation ->

                fun Double.trimTo5Decimals(): String {
                    return  toString()
                                .substringBeforeLast(".") +
                        "." +
                            toString()
                                .substringAfterLast(".")
                                .take(5)
                }

                // Update the notification with the new location
                newLocation?.run {
                    val lat = newLocation.latitude
                    val long = newLocation.longitude
                    val updatedNotification = notification.setContentText(
                        "Location: (lat=${lat.trimTo5Decimals()})" +
                                ", (lon=${long.trimTo5Decimals()})"
                    )
                    notificationManager.notify(1, updatedNotification.build())
                }
            }
        }

        startForeground(1, notification.build())
        Log.d { "com.realityexpander.GPSLocationForegroundNotificationService: start complete" }

        // for flow - leave for reference
        //    // Consumes a flow of locations and updates the notification
        //    locationClient
        //        .getLocationUpdates(10000L)
        //        .catch { e -> e.printStackTrace() }
        //        .onEach { location ->
        //            val lat = location.latitude.toString()
        //            val long = location.longitude.toString()
        //            val updatedNotification = notification.setContentText(
        //                "Location: (lat=$lat, lon=$long)"
        //            )
        //            notificationManager.notify(1, updatedNotification.build())
        //        }
        //        .launchIn(serviceScope)
    }

    private fun stop() {
        serviceScope.launch {
            removeNotification()

            stopForeground(Service.STOP_FOREGROUND_REMOVE)
            serviceScope.cancel()
            stopSelf()
        }
    }

    private fun removeNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(1)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START_NOTIFICATION_SERVICE = "ACTION_START"
        const val ACTION_STOP_NOTIFICATION_SERVICE = "ACTION_STOP"
    }
}
