package com.yanka.poc.notifications.fcm

import android.annotation.SuppressLint
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.Intent
import android.graphics.drawable.BitmapDrawable
import android.support.v4.app.NotificationCompat
import android.support.v4.app.TaskStackBuilder
import android.support.v4.content.ContextCompat
import com.google.firebase.messaging.RemoteMessage.Notification
import com.yanka.poc.notifications.ui.HomeActivity
import com.yanka.poc.notifications.notif.NotificationConstants
import com.yanka.poc.notifications.R
import com.yanka.poc.notifications.ui.NotificationsLandingActivity

class FcmMessageHandler {

    enum class NotificationType {
        SIMPLE, INBOX, BIG_TEXT
    }

    fun handleFirebaseMessage(context: Context, notification: Notification) {
        if (notification.title.isNullOrBlank() && notification.body.isNullOrBlank()) return

        val notifBuilder =
                NotificationCompat.Builder(context, NotificationConstants.NOTIF_CHANNEL_X)
                        .apply {
                            setSmallIcon(R.drawable.ic_notification_small)
                            notification.title?.let { setContentTitle(it) }
                            notification.body?.let { setContentText(it) }
                        }

        showSimpleNotifications(context, notification.body ?: "", notifBuilder)
    }

    fun handleFirebaseMessage(context: Context, data: Map<String, String>) {
        val notifBuilder =
                NotificationCompat.Builder(context, NotificationConstants.NOTIF_CHANNEL_X)
                        .setSmallIcon(R.drawable.ic_notification_small)

        val notificationType = if (data.isEmpty()) NotificationType.SIMPLE else NotificationType.BIG_TEXT

        when (notificationType) {
            FcmMessageHandler.NotificationType.BIG_TEXT ->
                showBigTextStyleNotifications(context, data, notifBuilder)
            else ->
                showSimpleNotifications(context, "", notifBuilder)
        }
    }

    private fun showSimpleNotifications(context: Context,
                                        message: String,
                                        notifBuilder: NotificationCompat.Builder) {
        val notifId = getAndIncrementNotifId(context, LAST_SIMPLE_NOTIF_ID)

        // Send a new notification with unique id so that the previous notification is not overwritten
        showNotification(context, notifId, message, notifBuilder)
    }

    @SuppressLint("ApplySharedPref")
    private fun getAndIncrementNotifId(context: Context, notifIdKey: String): Int {
        val preferences = context.getSharedPreferences(PREF_FILE, MODE_PRIVATE)
        val notifId = preferences.getInt(notifIdKey, 0) + 1
        preferences.edit().putInt(notifIdKey, notifId).commit()
        return notifId
    }

    private fun showBigTextStyleNotifications(context: Context,
                                              data: Map<String, String>,
                                              notifBuilder: NotificationCompat.Builder) {
        val drawable = ContextCompat.getDrawable(context, R.drawable.ic_notification_large)
        val largeNotificationIcon = (drawable as BitmapDrawable).bitmap
        notifBuilder.setLargeIcon(largeNotificationIcon)
        val style = NotificationCompat.BigTextStyle()

        val keySet = data.keys
        if (keySet.contains("title")) {
            val title = data["title"]
            notifBuilder.setContentTitle(title)
        }

        // Show data in the expanded layout
        val builder = StringBuilder()
        var message = ""
        if (keySet.contains("subtitle")) {
            message = data["subtitle"] ?: ""
            builder.append(message)
        }

        style.bigText(builder.toString())
        // Moves the expanded layout object into the notification object.
        notifBuilder.setStyle(style)

        val notifId = getAndIncrementNotifId(context, LAST_BIG_TEXT_NOTIF_ID)

        // Send a new notification with unique id so that the previous notification is not overwritten
        showNotification(context, notifId, message, notifBuilder)
    }

    private fun showNotification(context: Context,
                                 notificationId: Int,
                                 message: String,
                                 notifBuilder: NotificationCompat.Builder) {
        val resultPendingIntent = createPendingIntent(context, notificationId, message)
        notifBuilder.setContentIntent(resultPendingIntent)

        val notifManager =
                context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        // id allows you to update the notification later on.
        notifManager.notify(notificationId, notifBuilder.build())
    }

    private fun createPendingIntent(
            context: Context, notificationId: Int, message: String): PendingIntent? {

        val resultIntent = Intent(context, NotificationsLandingActivity::class.java)
        resultIntent.putExtra(KEY_NOTIFICATION_ID, notificationId)
        resultIntent.putExtra(KEY_MESSAGE, message)

        // The stack builder object will contain an artificial back stack for the started Activity.
        // This ensures that navigating backward from the Activity leads out of your application to the Home screen.
        val stackBuilder = TaskStackBuilder.create(context)

        // Adds the back stack for the Intent (but not the Intent itself)
        stackBuilder.addParentStack(HomeActivity::class.java)

        // Adds the Intent that starts the Activity to the top of the stack
        stackBuilder.addNextIntent(resultIntent)

        return stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT)
    }

    companion object {
        private val TAG = "#### Notifications:"

        const val KEY_TEXT_REPLY = "key_text_reply"
        const val KEY_NOTIFICATION_ID = "key_notification_id"
        const val KEY_MESSAGE = "key_message"

        private const val PREF_FILE = "firebase_notif.pref"
        private const val LAST_SIMPLE_NOTIF_ID = "last_simple_notif_id"
        private const val LAST_BIG_TEXT_NOTIF_ID = "last_big_text_notif_id"
    }

}
