package com.phed.kohistan
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import android.util.Log
class PHEDMessagingService : FirebaseMessagingService() { override fun onMessageReceived(message: RemoteMessage) { Log.d("PHED", message.notification?.title ?: "notification") } }
