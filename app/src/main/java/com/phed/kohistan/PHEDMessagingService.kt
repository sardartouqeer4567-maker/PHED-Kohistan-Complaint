package com.phed.kohistan

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class PHEDMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        Log.d(
            "PHED",
            message.notification?.title ?: "PHED Notification"
        )
    }
}
