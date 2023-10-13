package com.realityexpander

import LocationService
import android.app.NotificationManager
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.session.PlaybackState.ACTION_STOP
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat.stopForeground
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import co.touchlab.kermit.Logger as Log

class LocationForegroundService: Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
//    private lateinit var locationClient: LocationClient
    private lateinit var locationClient: LocationService

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
//        locationClient = LocationClientImpl(
//            applicationContext,
//            LocationServices.getFusedLocationProviderClient(applicationContext)
//        )
        locationClient = LocationService()
    }

    // Should be called "onReceiveCommand" instead of "onStartCommand"
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when(intent?.action) {
            ACTION_START -> start()
            ACTION_STOP -> stop()
        }
        return super.onStartCommand(intent, flags, startId)
    }

    private fun start() {
        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Tracking location...")
            .setContentText("Location: retrieving...")
            .setSmallIcon(R.drawable.ic_launcher_background)
            .setOngoing(true)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Consumes a flow of locations and updates the notification
//        locationClient
//            .getLocationUpdates(10000L)
//            .catch { e -> e.printStackTrace() }
//            .onEach { location ->
//                val lat = location.latitude.toString()
//                val long = location.longitude.toString()
//                val updatedNotification = notification.setContentText(
//                    "Location: (lat=$lat, lon=$long)"
//                )
//                notificationManager.notify(1, updatedNotification.build())
//            }
//            .launchIn(serviceScope)

        // Uses a callback to update the notification
        serviceScope.launch {
            locationClient.currentLocation(
                errorCallback = { error ->
                    Log.i("LocationForegroundService, Error: $error" )
                }
            ) { location ->
                location?.run {
                    val lat = location.latitude.toString()
                    val long = location.longitude.toString()
                    val updatedNotification = notification.setContentText(
                        "Location: (lat=$lat, lon=$long)"
                    )
                    notificationManager.notify(1, updatedNotification.build())
                }
            }
        }

        startForeground(1, notification.build())
    }

    private fun stop() {
//        stopForeground(true) // deprecated
        stopForeground(Service.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    companion object {
        const val ACTION_START = "ACTION_START"
        const val ACTION_STOP = "ACTION_STOP"
    }
}
