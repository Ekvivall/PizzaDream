package com.sokol.pizzadream.FCM

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.SplashScreenActivity
import kotlin.random.Random

class MyFirebaseMessagingService : FirebaseMessagingService() {
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Common.updateToken(this, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        val dataRemoteMessage = message.data
        if (dataRemoteMessage[Common.NOTIFICATION_TITLE] == "Ваше замовлення оновлено") {
            if (Common.currentUser?.receiveOrderUpdates == true) {
                val intent = Intent(this, SplashScreenActivity::class.java)
                intent.putExtra(Common.IS_OPEN_ACTIVITY_ORDER, true)
                Common.showNotification(
                    this,
                    Random.nextInt(),
                    dataRemoteMessage[Common.NOTIFICATION_TITLE],
                    dataRemoteMessage[Common.NOTIFICATION_CONTENT],
                    intent
                )
            }
        } else if (dataRemoteMessage[Common.IMAGE_URL] != null) {
            if (Common.currentUser?.receiveNews == true) {
                val intent = Intent(this, SplashScreenActivity::class.java)
                intent.putExtra(Common.IS_OPEN_ACTIVITY_NEWS, true)
                Glide.with(this).asBitmap().load(dataRemoteMessage[Common.IMAGE_URL])
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap, transition: Transition<in Bitmap>?
                        ) {
                            Common.showNotification(
                                this@MyFirebaseMessagingService,
                                Random.nextInt(),
                                dataRemoteMessage[Common.NOTIFICATION_TITLE],
                                dataRemoteMessage[Common.NOTIFICATION_CONTENT],
                                resource,
                                intent
                            )
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {

                        }

                    })
            }
        } else {
            Common.showNotification(
                this,
                Random.nextInt(),
                dataRemoteMessage[Common.NOTIFICATION_TITLE],
                dataRemoteMessage[Common.NOTIFICATION_CONTENT],
                null
            )
        }
    }
}