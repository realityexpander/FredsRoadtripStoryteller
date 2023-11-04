package com.realityexpander.gpsForegroundNotificationService

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import data.appSettings
import tts

// Accepts intents for Actions from the GPS tracking Foreground-Service Notification
class NotificationActionBroadcastReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context?, intent: Intent?) {
        println("NotificationActionBroadcastReceiver: onReceive: intent: $intent")
        context ?: return
        intent ?: return

        if(intent.action == GPS_FOREGROUND_SERVICE_NOTIFICATION_StopSpeakingTextToSpeech_ACTION) {
            tts?.stop()
        }
        if(intent.action == GPS_FOREGROUND_SERVICE_NOTIFICATION_MuteAllTextToSpeech_ACTION) {
            tts?.stop()
            appSettings.shouldSpeakWhenUnseenMarkerFound = false
        }
    }

    companion object {
        const val GPS_FOREGROUND_SERVICE_NOTIFICATION_StopSpeakingTextToSpeech_ACTION =
            "GPS_FOREGROUND_SERVICE_NOTIFICATION_StopSpeakingTextToSpeech_ACTION"
        const val GPS_FOREGROUND_SERVICE_NOTIFICATION_MuteAllTextToSpeech_ACTION =
            "GPS_FOREGROUND_SERVICE_NOTIFICATION_MuteAllTextToSpeech_ACTION"

        const val kNotificationActionRequestCode = 1
    }
}
