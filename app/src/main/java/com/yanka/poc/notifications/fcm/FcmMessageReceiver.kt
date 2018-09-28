package com.yanka.poc.notifications.fcm

import android.text.TextUtils
import android.util.Log
import com.google.firebase.messaging.RemoteMessage
import com.google.firebase.messaging.FirebaseMessagingService


class FcmMessageReceiver : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        // Not getting messages here? See why this may be: https://goo.gl/39bRNJ
        Log.d(TAG, "From: " + (remoteMessage.from ?: "Unknown"))

        // Show notifications only when there is a data object in message payload.
        if (remoteMessage.data?.isNotEmpty() == true) {
            FcmMessageHandler().handleFirebaseMessage(this, remoteMessage.data)
        } else {
            remoteMessage.notification?.let {
                FcmMessageHandler().handleFirebaseMessage(this, it)
            }
        }

        // The following is only for validation and debug purposes.
        checkAndLogValidationErrors(remoteMessage)
    }

    /**
     * This method is only for validation and debug purposes.
     * It checks for the presence of data and notification objects in the notification payload.
     * We only want the data object and never require notification object in the payload.
     * The reason why we require data only is because this enables the app to
     * always intercept the messages and show them or suppress them as required.
     *
     * @param remoteMessage
     */
    private fun checkAndLogValidationErrors(remoteMessage: RemoteMessage) {
        val validationMessageBuilder = StringBuilder()
        // Notifications should always have a non empty data json object in the payload.
        if (remoteMessage.data?.isEmpty() == true) {
            validationMessageBuilder.append("Push Notification received doesn't have data payload\n")
        }

        // Notifications should not have a notification json object in the payload.
        if (remoteMessage.notification != null) {
            validationMessageBuilder.append("We received a notification object with body")
            validationMessageBuilder.append("\nNotification object should not be present. Send data object only.\n")
            validationMessageBuilder.append(remoteMessage.notification?.body ?: "Unknown body")

            Log.e(TAG, "\nNotification object should not be present. Send data object only.\n")
            Log.e(TAG, "Message Notification Body: " + (remoteMessage.notification?.body ?: "Unknown body"))
        }

        val errorMessage = validationMessageBuilder.toString()
        if (!TextUtils.isEmpty(errorMessage)) {

            Log.d(TAG, "Error: $errorMessage")
        }
    }

    companion object {

        private val TAG = "FcmMessageReceiver"
    }

}
