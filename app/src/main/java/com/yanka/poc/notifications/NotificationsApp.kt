package com.yanka.poc.notifications

import android.app.Application
import com.yanka.poc.notifications.notif.initializeNotificationChannels

class NotificationsApp: Application() {

    override fun onCreate() {
        super.onCreate()

        initializeNotificationChannels()
    }
}