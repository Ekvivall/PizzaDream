package com.sokol.pizzadream.FCM

import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sokol.pizzadream.Common.Common
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Common.updateToken(this, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val dataRemoteMessage = message.data
        Common.showNotification(
            this,
            Random.nextInt(),
            dataRemoteMessage[Common.NOTIFICATION_TITLE],
            dataRemoteMessage[Common.NOTIFICATION_CONTENT],
            null
        )
    }
}