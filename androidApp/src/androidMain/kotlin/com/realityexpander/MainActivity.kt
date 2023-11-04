package com.realityexpander

import GPSLocationService
import MainView
import android.Manifest
import android.content.Intent
import android.os.Bundle
import android.speech.tts.TextToSpeech
import androidx.activity.compose.setContent
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import appContext
import com.google.android.gms.maps.MapsInitializer
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.ktx.logEvent
import data.appSettings
import intentFlow
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import presentation.SplashScreenForPermissions
import presentation.app.AppTheme
import tts
import java.util.Locale
import co.touchlab.kermit.Logger as Log

class MainActivity : AppCompatActivity(), TextToSpeech.OnInitListener {

    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private val splashState = MutableStateFlow(false)

    private var isSendingUserToAppSettingsScreen = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        tts = TextToSpeech(this, this)

        // https://proandroiddev.com/implementing-core-splashscreen-api-e62f0e690f74
        installSplashScreen().apply {
            setKeepOnScreenCondition {
                splashState.asStateFlow().value
            }
        }

        // Setup the permission launcher & callback
        permissionLauncher = registerForActivityResult(
            ActivityResultContracts.RequestMultiplePermissions()
        ) {
            // Check if permissions were granted
            if(it[Manifest.permission.ACCESS_FINE_LOCATION] == false ||
                it[Manifest.permission.ACCESS_COARSE_LOCATION] == false
            ) {
                appSettings.isPermissionsGranted = false
                AlertDialog.Builder(this)
                    .setTitle("Location Permissions Required")
                    .setMessage("This app requires location permissions to function. " +
                            "Please enable location permissions in the app settings.")
                    .setPositiveButton("App Settings") { _, _ ->

                        // Intent to open the App Settings
                        Intent(android.provider.Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                            data = android.net.Uri.parse("package:$packageName")
                            startActivity(this)
                        }
                        isSendingUserToAppSettingsScreen = true
                    }
                    .setNegativeButton("Cancel") { _, _ ->

                        // enable logging:  adb shell setprop log.tag.FA VERBOSE
                        //                  adb shell setprop log.tag.FA-SVC VERBOSE
                        //                  adb logcat -v time -s FA FA-SVC
                        // disable logging: adb shell setprop debug.firebase.analytics.app .none.
                        firebaseAnalytics.logEvent("location_granted") {
                            param("granted", "false")
                        }

                        finish()
                    }
                    .show()
            } else {
                // Dismiss the splash screen
                splashState.tryEmit(true)
                appSettings.isPermissionsGranted = true

                setContent {
                    MainView()
                }
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
            Log.i("onMapsSdkInitialized: initialized Google Maps SDK, version: ${it.name}")
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

        // Enable dev mode on your local device:
        // - adb shell setprop debug.firebase.appdistro.devmode true  // false to turn off
        FirebaseApp.initializeApp(this)

        // If permissions are not granted yet, show the splash screen for permissions.
        setContent {
            AppTheme {
                SplashScreenForPermissions(appSettings.isPermissionsGranted)
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = tts!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS: The Language not supported!, result= $result")
                tts = null
            } else {
                Log.d("TTS: Initialization success!")
            }
        }
    }

    override fun onDestroy() {
        if (tts != null) {
            tts!!.stop()
            tts!!.shutdown()
        }
        stopBackgroundUpdates()

        super.onDestroy()
    }

    // Turn off the notification service for the GPS service, which prevents background location updates
    private fun stopBackgroundUpdates() {
        Intent(applicationContext, GPSForegroundNotificationService::class.java).apply {
            action = GPSForegroundNotificationService.ACTION_STOP_NOTIFICATION_SERVICE
            appContext.startService(this) // sends command to stop service
        }
    }

    // Turn on the notification service for the GPS service, which allows background location updates
    private fun startBackgroundUpdates() {
        Intent(applicationContext, GPSForegroundNotificationService::class.java).apply {
            action = GPSForegroundNotificationService.ACTION_START_NOTIFICATION_SERVICE
            appContext.startService(this) // sends command to start service
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        println("onNewIntent: intent: $intent")

        if(intent?.action == GPSForegroundNotificationService.ACTION_STOP_NOTIFICATION_SERVICE) {
            stopBackgroundUpdates()
        }
    }

//    // Capture result from permission request - LEAVE FOR REFERENCE
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String>,
//        grantResults: IntArray
//    ) {
//        super.onRequestPermissionsResult(
//            requestCode,
//            permissions,
//            grantResults
//        )
//        println("onRequestPermissionsResult: requestCode: $requestCode, permissions: $permissions, grantResults: $grantResults")
//    }

    override fun onResume() {
        super.onResume()
        println("onResume: intent: $intent")

        // Relaunch the permission dialog if the user was sent to the app settings screen
        if(isSendingUserToAppSettingsScreen) {
            isSendingUserToAppSettingsScreen = false
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ))
        }
    }
}

@Preview
@Composable
fun Test() {
    Text("Hello World") // previews work here
}
