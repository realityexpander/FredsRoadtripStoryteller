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
import com.google.android.gms.maps.MapsInitializer

class MainActivity : AppCompatActivity() {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Setup the permission launcher & callback
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            // After we have permissions, we can get start the service and show notifications
            // viewModel.loadWeatherInfo()

            Intent(applicationContext, LocationForegroundService::class.java).apply {
                action = LocationForegroundService.ACTION_START
                startService(this) // sends command to start service
            }
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
        };

        setContent {
            MainView()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        Intent(applicationContext, LocationForegroundService::class.java).apply {
            action = LocationForegroundService.ACTION_STOP
            startService(this) // sends command to stop service
        }
    }
}
