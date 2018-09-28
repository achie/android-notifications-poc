package com.yanka.poc.notifications.notif

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build

class NotificationConstants {

    companion object {
        const val NOTIF_CHANNEL_X = "channel_x_id"
    }

}

fun Context.initializeNotificationChannels() {
    // Create the NotificationChannel, but only on API 26+ because
    // the NotificationChannel class is new and not in the support library
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val channel = NotificationChannel(
                NotificationConstants.NOTIF_CHANNEL_X, "Channel X", NotificationManager.IMPORTANCE_DEFAULT)
                .apply {
                    description = "Channel X description"
                }
        // Register the channel with the system
        val notifManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notifManager.createNotificationChannel(channel)
    }
}