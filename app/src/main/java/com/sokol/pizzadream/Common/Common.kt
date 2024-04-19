package com.sokol.pizzadream.Common

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.ConnectivityManager
import android.os.Build
import android.widget.Toast
import androidx.core.app.NotificationCompat
import com.google.firebase.database.FirebaseDatabase
import com.sokol.pizzadream.Database.Entities.CartItemDB
import com.sokol.pizzadream.Model.AddonCategoryModel
import com.sokol.pizzadream.Model.AddonModel
import com.sokol.pizzadream.Model.CartItem
import com.sokol.pizzadream.Model.CategoryModel
import com.sokol.pizzadream.Model.FoodModel
import com.sokol.pizzadream.Model.NewsModel
import com.sokol.pizzadream.Model.OrderModel
import com.sokol.pizzadream.Model.TokenModel
import com.sokol.pizzadream.Model.UserModel
import com.sokol.pizzadream.Model.VacancyModel
import com.sokol.pizzadream.R
import java.math.RoundingMode
import java.text.DecimalFormat
import java.util.Random

object Common {
    fun formatPrice(price: Double): String {
        if (price != 0.toDouble()) {
            val df = DecimalFormat("#,##0.00")
            df.roundingMode = RoundingMode.HALF_UP
            val finalPrice = StringBuilder(df.format(price).toString() + " грн.").toString()
            return finalPrice
        } else {
            return "0,00 грн."
        }
    }

    fun createOrderId(): String {
        val characters = "ABCDEFGHIJKLMONPQRSTUVWXYZ"
        return StringBuilder().append(characters[Random().nextInt(characters.length)])
            .append(System.currentTimeMillis())
            .append(characters[Random().nextInt(characters.length)]).toString()
    }

    val IS_OPEN_ACTIVITY_ORDER: String ="IsOpenActivityOrder"
    val NOTIFICATION_CONTENT: String = "content"
    val NOTIFICATION_TITLE = "title"
    val TOKEN_REF: String = "Tokens"
    val COMMENT_REF: String = "Comments"
    val REVIEW_REF: String = "ReviewsPizzeria"
    val RESUME_REF: String = "Resumes"
    val VACANCIES_REF: String = "Vacancies"
    var vacancySelected: VacancyModel? = null
    var authorizeToken: String? = null
    var newsSelected: NewsModel? = null
    var currentToken: String = ""
    val NEWS_REF: String = "News"
    val ORDER_REF: String = "Orders"
    var userSelectedAddress: String = ""
    var userSelectedAddon: MutableList<AddonModel>? = null
    val ADDON_ADDRESS_REF: String = "Addresses"
    var addonCategorySelected: AddonCategoryModel? = null
    val USER_REFERENCE: String = "Users"
    val CATEGORY_REF: String = "Categories"
    val ADDON_CATEGORY_REF: String = "Addon"
    var categorySelected: CategoryModel? = null
    var foodSelected: FoodModel? = null
    var cartItemSelected: CartItem? = null
    var orderSelected: OrderModel? = null
    var currentUser: UserModel? = null
    var totalPrice: String = "Всього: 0 грн."
    val customerPizzas = "customer_pizzas"
    val PERMISSIONS_REQUEST_LOCATION = 100
    val MIN_TIME_BETWEEN_UPDATES: Long = 1000
    val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 10f
    var STATUSES: List<String> = listOf(
        "Очікує підтвердження",
        "Підготовлено",
        "Готове до доставки",
        "В дорозі",
        "Доставлено",
        "Скасовано"
    )

    fun isConnectedToInternet(context: Context): Boolean {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            // Доступне підключення до Інтернету
            return true
        }
        // Немає підключення до Інтернету
        return false
    }

    fun buildToken(authorizeToken: String): String {
        return java.lang.StringBuilder("Bearer ").append(authorizeToken).toString()
    }

    fun updateToken(context: Context, token: String) {
        val tokenModel = TokenModel()
        tokenModel.email = currentUser!!.email
        tokenModel.token = token
        FirebaseDatabase.getInstance().getReference(TOKEN_REF).child(currentUser!!.uid!!)
            .setValue(tokenModel).addOnFailureListener { e ->
                Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
            }
    }

    fun showNotification(
        context: Context,
        id: Int,
        title: String?,
        content: String?,
        intent: Intent?
    ) {
        var pendingIntent: PendingIntent? = null
        if (intent != null) {
            // Створення PendingIntent для запуску заданого Intent при натисканні на сповіщення
            pendingIntent =
                PendingIntent.getActivity(context, id, intent, PendingIntent.FLAG_UPDATE_CURRENT)
        }
        val notificationChannelId = "sokol.pizzadream"
        val notificationManager =
            context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // Створення каналу сповіщень, якщо працюємо на Android 8.0 або вище
            val notificationChannel = NotificationChannel(
                notificationChannelId, "Pizza Dream", NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationChannel.description = "Pizza Dream"
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.lightColor = Color.RED
            notificationChannel.vibrationPattern = longArrayOf(0, 1000, 500, 1000)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val builder = NotificationCompat.Builder(context, notificationChannelId)
        builder.setContentTitle(title).setContentText(content).setAutoCancel(true)
            .setSmallIcon(R.drawable.icon)
        if (pendingIntent != null) {
            // Встановлення PendingIntent для сповіщення, яке виконується при натисканні на нього
            builder.setContentIntent(pendingIntent)
        }
        val notification = builder.build()
        // Відображення сповіщення
        notificationManager.notify(id, notification)
    }

    fun getNewOrderTopic(): String {
        return StringBuilder("/topics/new_order").toString()
    }

}