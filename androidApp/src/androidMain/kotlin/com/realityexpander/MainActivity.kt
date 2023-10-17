package com.realityexpander

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

        setContent {
            MainView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        stopBackgroundUpdates()
    }

    private fun stopBackgroundUpdates() {
        Intent(applicationContext, GPSLocationForegroundNotificationService::class.java).apply {
            action = GPSLocationForegroundNotificationService.ACTION_STOP_NOTIFICATION_SERVICE
            appContext.startService(this) // sends command to stop service
        }
    }

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
