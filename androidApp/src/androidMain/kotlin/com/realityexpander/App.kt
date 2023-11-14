package com.realityexpander

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import appContext
import buildNumber
import installAtEpochSeconds
import versionNumber

class App: Application() {

    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate() {
        super.onCreate()

        // Create the notification channel
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "location",
                "Location",
                NotificationManager.IMPORTANCE_LOW // IMPORTANCE_HIGH will make the notification sound
            )
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }

        appContext = applicationContext

        // get from gradle.properties
        val packageName = applicationContext.packageName
        versionNumber =
            packageManager.getPackageInfo(packageName, 0).versionName
        buildNumber =
            packageManager.getPackageInfo(packageName, 0).longVersionCode.toString()
        installAtEpochSeconds =
            packageManager.getPackageInfo(packageName, 0).firstInstallTime
    }
}
