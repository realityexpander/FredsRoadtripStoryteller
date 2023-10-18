package com.realityexpander

import GPSLocationService
import MainView
import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import appContext
import com.google.android.gms.maps.MapsInitializer
import intentFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup the permission launcher & callback
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            // Default to background location updates off for now
            //    // After we have permissions, we can get start the foreground service and show notification
            //    Intent(applicationContext, GPSLocationForegroundNotificationService::class.java).apply {
            //        action = GPSLocationForegroundNotificationService.ACTION_START
            //        startService(this) // sends command to start service
            //    }
        }

        // Get permissions to access location (opens dialog)
        permissionLauncher.launch(arrayOf(
            Manifest.permission.ACCESS_FINE_LOCATION,
            Manifest.permission.ACCESS_COARSE_LOCATION,
        ))

        // Initialize Google Maps SDK
        // See https://issuetracker.google.com/issues/228091313
        MapsInitializer.initialize(this, MapsInitializer.Renderer.LATEST) {
            Log.d("TAG", "onMapsSdkInitialized: initialized Google Maps SDK, version: ${it.name}")
        }


        // Collects the intent flow from the common module Android specific code
        // note: for some reason, this doesn't work in the common module, so we must collect assert(true)
        //       flow from the Android specific code, and then emit the intent from this MainActivity.
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            intentFlow.collect { intent ->
                if(intent.action == GPSLocationService.ACTION_STOP_BACKGROUND_UPDATES) {
                    stopBackgroundUpdates()
                }
                if(intent.action == GPSLocationService.ACTION_START_BACKGROUND_UPDATES) {
                    startBackgroundUpdates()
                }
            }
        }

        // todo - trigger from settings
        // Enable dev mode on your local device:
        // - adb shell setprop debug.firebase.appdistro.devmode true  // false to turn off
//        FirebaseApp.initializeApp(this)
//        // Show the Firebase feedback notification
//        Firebase.appDistribution.showFeedbackNotification(
//            // Text providing notice to your testers about collection and
//            // processing of their feedback data
//            "Please let us know your thoughts about this app!",
//            // The level of interruption for the notification
//            InterruptionLevel.HIGH)
////        Firebase.appDistribution.startFeedback("feedback")

        setContent {
            MainView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBackgroundUpdates()
    }

    // Turn off the notification service for the GPS service, which prevents background location updates
    private fun stopBackgroundUpdates() {
        Intent(applicationContext, GPSLocationForegroundNotificationService::class.java).apply {
            action = GPSLocationForegroundNotificationService.ACTION_STOP_NOTIFICATION_SERVICE
            appContext.startService(this) // sends command to stop service
        }
    }

    // Turn on the notification service for the GPS service, which allows background location updates
    private fun startBackgroundUpdates() {
        Intent(applicationContext, GPSLocationForegroundNotificationService::class.java).apply {
            action = GPSLocationForegroundNotificationService.ACTION_START_NOTIFICATION_SERVICE
            appContext.startService(this) // sends command to start service
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if(intent?.action == GPSLocationForegroundNotificationService.ACTION_STOP_NOTIFICATION_SERVICE) {
            stopBackgroundUpdates()
        }
    }
}
