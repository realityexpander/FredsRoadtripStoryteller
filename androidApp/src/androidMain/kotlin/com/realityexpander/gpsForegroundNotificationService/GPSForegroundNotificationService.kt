package com.realityexpander.gpsForegroundNotificationService

import GPSLocationService
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.PendingIntent.FLAG_IMMUTABLE
import android.app.PendingIntent.FLAG_UPDATE_CURRENT
import android.app.PendingIntent.getBroadcast
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Canvas
import android.os.IBinder
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.realityexpander.MainActivity
import com.realityexpander.gpsForegroundNotificationService.GPSForegroundNotificationServiceNotificationBroadcastReceiver.Companion.GPS_FOREGROUND_SERVICE_NOTIFICATION_MuteAllTextToSpeech_ACTION
import com.realityexpander.gpsForegroundNotificationService.GPSForegroundNotificationServiceNotificationBroadcastReceiver.Companion.GPS_FOREGROUND_SERVICE_NOTIFICATION_StopSpeakingTextToSpeech_ACTION
import com.realityexpander.gpsForegroundNotificationService.GPSForegroundNotificationServiceNotificationBroadcastReceiver.Companion.kNotificationActionRequestCode
import data.appSettings
//import com.realityexpander.common.R  // uses the shared module R file
import com.realityexpander.R as AppR  // uses the AndroidMain module R file
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import presentation.maps.Location
import co.touchlab.kermit.Logger as Log


// Used solely to update the notification (required for Android 8.0+)
// Must have this for background location updates on Android.
class GPSForegroundNotificationService: Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private lateinit var gpsLocationService: GPSLocationService
    private var lastLocation: Location? = null

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onCreate() {
        super.onCreate()
        gpsLocationService = GPSLocationService()

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

    private fun getBitmapFromVectorDrawable(context: Context?, drawableId: Int): Bitmap {
        val drawable = ContextCompat.getDrawable(context!!, drawableId)
        val bitmap = Bitmap.createBitmap(
            drawable!!.intrinsicWidth,
            drawable.intrinsicHeight, Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    private fun start() {
        val muteTextToSpeechAction = createStopSpeakingTextToSpeechAction()
        val cancelSpeakingMarkersAction = createMuteAllTextToSpeechAction()

        val notification = NotificationCompat.Builder(this, "location")
            .setContentTitle("Looking for Historical Markers...")
            .setContentText("Location: retrieving...")  // initial message before location is retrieved
            .setSubText("Tap to open app")
            .setSmallIcon(AppR.drawable.round_add_location_24) // uses the AndroidMain module R file
            .setLargeIcon(getBitmapFromVectorDrawable(this, AppR.drawable.round_add_location_24)) // uses the AndroidMain module R file
            .setOngoing(true)
            .setDefaults(NotificationCompat.VISIBILITY_PUBLIC)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .addAction(muteTextToSpeechAction)
            .addAction(cancelSpeakingMarkersAction)
            .setContentIntent(
                // tapping the notification opens the app
                PendingIntent.getActivity(this, 0,
                    Intent(this, MainActivity::class.java),
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            )

        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Update notification every second even when not moving
        serviceScope.launch {
            while(true) {
                delay(1000L)
                if(!serviceScope.isActive) {
                    return@launch
                }

                // Update the "Last seen marker" and "Last spoken marker" text (from UI usage)
                updateNotificationContent(notification, notificationManager, lastLocation)
            }
        }

        // Update the notification when the location changes
        serviceScope.launch {
            gpsLocationService.onUpdatedGPSLocation( // Uses a callback to update the notification
                errorCallback = { error ->
                    Log.w("com.realityexpander.LocationForegroundService, Error: $error" )

                }
            ) { newLocation ->

                if(!serviceScope.isActive) {
                    return@onUpdatedGPSLocation
                }
                lastLocation = newLocation

                // Update for last spoken marker (only when speaking unseen markers)
                updateNotificationContent(notification, notificationManager, newLocation)
            }
        }

        startForeground(kGPSLocationForegroundServiceNotificationId, notification.build())
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

    private fun updateNotificationContent(
        notification: NotificationCompat.Builder,
        notificationManager: NotificationManager,
        newLocation: Location?
    ) {
        if (appSettings.shouldSpeakWhenUnseenMarkerFound) {
            val lastSpokenMarker = appSettings.lastSpokenMarker
            if (lastSpokenMarker.id.isNotBlank()) {
                val title = "Last spoken marker: ${lastSpokenMarker.title}"
                val updatedNotification =
                    notification
                        .setContentTitle(title)
                        .setContentText(lastSpokenMarker.id)
                        .setStyle(
                            NotificationCompat.InboxStyle()
                                .addLine(lastSpokenMarker.title)
                                .addLine(lastSpokenMarker.id)
                        )

                notificationManager.notify(
                    kGPSLocationForegroundServiceNotificationId,
                    updatedNotification.build()
                )
                return
            }
        }

        // Update for last seen marker
        val lastSeenMarker = appSettings.uiRecentlySeenMarkersList.list.firstOrNull()
        lastSeenMarker?.let {
            val title = "Last seen marker: ${lastSeenMarker.title}"
            val updatedNotification =
                notification
                    .setContentTitle(title)
                    .setContentText(lastSeenMarker.id)
                    .setStyle(
                        NotificationCompat.InboxStyle()
                            .addLine(lastSeenMarker.title)
                            .addLine(lastSeenMarker.id)
                    )

            notificationManager.notify(
                kGPSLocationForegroundServiceNotificationId,
                updatedNotification.build()
            )
            return
        }

        // Default to Update for new location
        newLocation?.run {
            val lat = newLocation.latitude
            val long = newLocation.longitude
            val updatedNotification = notification.setContentText(
                "Location: (lat=${lat.trimTo5Decimals()})" +
                        ", (lon=${long.trimTo5Decimals()})"
            )

            notificationManager.notify(
                kGPSLocationForegroundServiceNotificationId,
                updatedNotification.build()
            )
        }
    }

    private fun stop() {
        serviceScope.cancel()
        removeNotification()
        stopForeground(Service.STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun removeNotification() {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(kGPSLocationForegroundServiceNotificationId)
    }

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
    }

    private fun createStopSpeakingTextToSpeechAction(): NotificationCompat.Action {
        // Setup action to create "Mute Speaking" button
        val muteTextToSpeechActionIntent =
            Intent(applicationContext, GPSForegroundNotificationServiceNotificationBroadcastReceiver::class.java).apply {
                action = GPS_FOREGROUND_SERVICE_NOTIFICATION_StopSpeakingTextToSpeech_ACTION
            }
        val muteTextToSpeechActionPendingIntent: PendingIntent =
            getBroadcast(
                applicationContext,
                kNotificationActionRequestCode,
                muteTextToSpeechActionIntent,
                FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
            )
        val muteTextToSpeechAction: NotificationCompat.Action =
            NotificationCompat.Action
                .Builder(
                    0, // doesn't appear anywhere afaik
                    applicationContext.getString(AppR.string.stop_speaking_text_to_speech_action_label), // uses the common module R file
                    muteTextToSpeechActionPendingIntent
                )
                .build()

        return muteTextToSpeechAction
    }

    private fun createMuteAllTextToSpeechAction(): NotificationCompat.Action {
        // Setup action to create "Cancel Speaking" button
        val cancelSpeakingMarkersActionIntent =
            Intent(applicationContext, GPSForegroundNotificationServiceNotificationBroadcastReceiver::class.java).apply {
                action = GPS_FOREGROUND_SERVICE_NOTIFICATION_MuteAllTextToSpeech_ACTION
            }
        val cancelSpeakingMarkersActionPendingIntent: PendingIntent =
            getBroadcast(
                applicationContext,
                kNotificationActionRequestCode,
                cancelSpeakingMarkersActionIntent,
                FLAG_UPDATE_CURRENT or FLAG_IMMUTABLE
            )
        val cancelSpeakingMarkersAction: NotificationCompat.Action =
            NotificationCompat.Action
                .Builder(
                    0, // doesn't appear anywhere afaik
                    applicationContext.getString(AppR.string.mute_all_text_to_speech_action_label), // uses the common module R file
                    cancelSpeakingMarkersActionPendingIntent
                )
                .build()

        return cancelSpeakingMarkersAction
    }

    private fun Double.trimTo5Decimals(): String {
        return toString()
            .substringBeforeLast(".") +
                "." +
                toString()
                    .substringAfterLast(".")
                    .take(5)
    }

    companion object {
        const val ACTION_START_NOTIFICATION_SERVICE = "ACTION_START"
        const val ACTION_STOP_NOTIFICATION_SERVICE = "ACTION_STOP"

        const val kGPSLocationForegroundServiceNotificationId = 1
    }
}
