package com.realityexpander

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import appContext
import buildNumber
import co.touchlab.kermit.ExperimentalKermitApi
import co.touchlab.kermit.LogWriter
import co.touchlab.kermit.Logger
import co.touchlab.kermit.Severity
import co.touchlab.kermit.crashlytics.CrashlyticsLogWriter
import debugLog
import installAtEpochSeconds
import versionNumber
import java.time.Clock
import java.time.ZoneId

class App: Application() {

    @OptIn(ExperimentalKermitApi::class) // for crashlytics logger
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate() {
        super.onCreate()

        Logger.addLogWriter(object: LogWriter() {
            override fun log(severity: Severity, message: String, tag: String, throwable: Throwable?) {
                    debugLog.add(
                        "${Clock.system(ZoneId.systemDefault()).instant()}: " +
                        "$severity " +
                        "$tag: " +
                        message
                    )
                    if(debugLog.size > 10000) {
                        debugLog.removeAt(0)
                    }
                }
            }
        )
        Logger.addLogWriter(CrashlyticsLogWriter())

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
