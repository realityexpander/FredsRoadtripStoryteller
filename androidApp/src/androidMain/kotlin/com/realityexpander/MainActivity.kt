package com.realityexpander

import BillingCommand
import CommonBilling
import GPSLocationService
import MainView
import _errorMessageFlow
import android.Manifest
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.net.Uri
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
import com.android.billingclient.api.BillingResult
import com.android.billingclient.api.Purchase
import com.android.billingclient.api.PurchasesUpdatedListener
import com.google.android.gms.maps.MapsInitializer
import com.google.firebase.FirebaseApp
import com.google.firebase.analytics.FirebaseAnalytics
import com.realityexpander.gpsForegroundNotificationService.GPSForegroundNotificationService
import data.appSettings
import intentFlow
import isDebuggable
import isTemporarilyPreventPerformanceTuningActive
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import presentation.SplashScreenForPermissions
import presentation.uiComponents.AppTheme
import textToSpeech
import java.util.Locale
import co.touchlab.kermit.Logger as Log

class MainActivity : AppCompatActivity(),
    TextToSpeech.OnInitListener,
    PurchasesUpdatedListener
{
    private lateinit var permissionLauncher: ActivityResultLauncher<Array<String>>
    private val splashState = MutableStateFlow(false)

    private var isSendingUserToAndroidAppSettingsScreen = false

    private val commonBilling = CommonBilling()
    private lateinit var purchaseManager: PurchaseManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val firebaseAnalytics = FirebaseAnalytics.getInstance(this)
        isDebuggable = 0 != applicationInfo.flags and ApplicationInfo.FLAG_DEBUGGABLE
         textToSpeech = TextToSpeech(this, this)

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
                            @Suppress("RemoveRedundantQualifierName") // We are using the Android flavor of Uri
                            data = android.net.Uri.parse("package:$packageName")
                            startActivity(this)
                        }
                        isSendingUserToAndroidAppSettingsScreen = true
                    }
                    .setNegativeButton("Cancel") { _, _ ->

                        // enable logging:  adb shell setprop log.tag.FA VERBOSE
                        //                  adb shell setprop log.tag.FA-SVC VERBOSE
                        //                  adb logcat -v time -s FA FA-SVC
                        // disable logging: adb shell setprop debug.firebase.analytics.app .none.
                        firebaseAnalytics.logEvent(
                            "location_granted",
                            Bundle().apply {
                                putString("granted", "false")
                            }
                        )

                        finish()
                    }
                    .show()
            } else {
                // Dismiss the splash screen
                splashState.tryEmit(true)
                appSettings.isPermissionsGranted = true

                setContent {
                    MainView(commonBilling)
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

        // Collects the "command intent" flow from the common module for Android specific code
        // note: for some reason, this doesn't work in the common module, so we must collect the intent
        //       flow from the Android-specific code, and then emit the intent from this MainActivity. __/shrug\__
        val scope = CoroutineScope(Dispatchers.Main)
        scope.launch {
            intentFlow.collect { intent ->
                when (intent.action) {
                    GPSLocationService.ACTION_STOP_BACKGROUND_UPDATES -> {
                        stopBackgroundUpdates()
                    }
                    GPSLocationService.ACTION_START_BACKGROUND_UPDATES -> {
                        startBackgroundUpdates()
                    }
                    Intent.ACTION_VIEW -> { // open a web link
                        startActivity(intent)
                    }
                    Intent.ACTION_SEND -> { // send an email
                        try {
                            startActivity(Intent.createChooser(intent, "Send Email"))
                        } catch (e: Exception) {
                            _errorMessageFlow.emit("Send Email failed: ${e.localizedMessage}")
                            e.printStackTrace()
                            Log.e("MainActivity: onCreate: startActivity(Intent.createChooser(intent, \"Send Email\")) failed: $e")
                        }
                    }
                    "Navigation" -> { // open navigation
                        val lat = intent.getDoubleExtra("lat", 0.0)
                        val lng = intent.getDoubleExtra("lng", 0.0)
                        val markerTitle = intent.getStringExtra("markerTitle") ?: ""
                        startNavigation(lat, lng, markerTitle)
                    }
                }

//                // In-app purchase // todo use CommonBilling
//                if(intent.action == ProductPurchaseAction.PurchasePro.value) {
//                    // Guard
//                    purchaseManager.productName.value ?: run {
//                        _errorMessageFlow.emit("PurchasePro: productDetails not initialized")
//                        return@collect
//                    }
//                    purchaseManager.makePurchase()
//                }
//                if(intent.action == ProductPurchaseAction.ConsumePro.value) {
//                    // Guard
//                    purchaseManager.productName.value ?: run {
//                        _errorMessageFlow.emit("ConsumePro: productDetails not initialized")
//                        return@collect
//                    }
//                    purchaseManager.consumeProduct()
//                }
            }
        }

        // Collect the billing commands from the common billing module for Android specific libraries
        scope.launch {
            commonBilling.commandFlow().collect { billingCommand ->
                when(billingCommand) {
                    is BillingCommand.Purchase -> {
                        // Guard
                        purchaseManager.productName.value ?: run {
                            _errorMessageFlow.emit("PurchasePro: productDetails not initialized")
                            return@collect
                        }
                        purchaseManager.makePurchase()
                    }
                    is BillingCommand.Consume -> {
                        // Guard
                        purchaseManager.productName.value ?: run {
                            _errorMessageFlow.emit("ConsumePro: productDetails not initialized")
                            return@collect
                        }
                        purchaseManager.consumeProduct()
                    }
                }
            }
        }

        // Enable dev mode on your local device:
        // - adb shell setprop debug.firebase.appdistro.devmode true  // false to turn off
        FirebaseApp.initializeApp(this)

        // If permissions are not granted yet, show the splash screen for permissions.
        if(!appSettings.isPermissionsGranted) {
            setContent {
                AppTheme {
                    SplashScreenForPermissions(appSettings.isPermissionsGranted)
                }
            }
        } else {
            // Coming back from a suspended state
            setContent {
                MainView(commonBilling)
            }
        }

        // Setup the in-app purchase helper
        purchaseManager = PurchaseManager(
            this,
//            _billingMessageFlow,
//            _productPurchaseStateFlow
            commonBilling
        )
        purchaseManager.billingSetup()
    }

    // TextToSpeech.OnInitListener
    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            val result = textToSpeech!!.setLanguage(Locale.US)

            if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
                Log.e("TTS: The Language not supported!, result= $result")
                textToSpeech = null
            } else {
                Log.d("TTS: Initialization success!")
            }
        }
    }

    override fun onDestroy() {
        if (textToSpeech != null) {
            textToSpeech!!.stop()
            textToSpeech!!.shutdown()
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
        isTemporarilyPreventPerformanceTuningActive = true

        // Relaunch the permission dialog if the user was previously sent to the Android's "App Settings" screen
        if(isSendingUserToAndroidAppSettingsScreen) {
            isSendingUserToAndroidAppSettingsScreen = false
            permissionLauncher.launch(arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
            ))
        }
    }

    private fun startNavigation(
        latitude: Double,
        longitude: Double,
        markerTitle: String,
    ) {
        val uriWaze = Uri.parse("https://waze.com/ul?ll=$latitude,$longitude&navigate=yes")
        val intentWaze = Intent(Intent.ACTION_VIEW, uriWaze)
        intentWaze.setPackage("com.waze")

        val uriGoogle = "google.navigation:q=$latitude,$longitude"
        val intentGoogleNav = Intent(Intent.ACTION_VIEW, Uri.parse(uriGoogle))
        intentGoogleNav.setPackage("com.google.android.apps.maps")

        val title = "Choose nav for marker:\n$markerTitle"
        val chooserIntent = Intent.createChooser(intentGoogleNav, title)
        val arr = arrayOfNulls<Intent>(1)
        arr[0] = intentWaze
        chooserIntent.putExtra(Intent.EXTRA_INITIAL_INTENTS, arr)

        try {
            startActivity(chooserIntent)
        } catch (e: ActivityNotFoundException) {
            // If Waze is not installed, open it in Google Play:
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse("market://details?id=com.waze"))
            startActivity(intent)
        }
    }

    override fun onPurchasesUpdated(
        billingResult: BillingResult,
        purchases: MutableList<Purchase>?
    ) {
        Log.i("onPurchasesUpdated ${billingResult.responseCode}")
        if (purchases != null) {
            for (purchase in purchases) {
                Log.i("purchase: $purchase") // todo - update pro product here?
            }
        } else {
            Log.i("No purchases found from query")
        }
    }
}

@Preview
@Composable
fun Test() {
    Text("Hello World") // previews work here
}
