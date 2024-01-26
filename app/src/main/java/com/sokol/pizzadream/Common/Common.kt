package com.sokol.pizzadream.Common

import android.content.Context
import android.graphics.Typeface
import android.net.ConnectivityManager
import android.text.Spannable
import android.text.SpannableString
import android.text.SpannableStringBuilder
import android.text.style.StyleSpan
import android.widget.TextView
import com.sokol.pizzadream.Model.AddonCategoryModel
import com.sokol.pizzadream.Model.CategoryModel
import com.sokol.pizzadream.Model.FoodModel
import com.sokol.pizzadream.Model.UserModel
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

    val NEWS_REF: String = "News"
    val ORDER_REF: String = "Orders"
    var userSelectedAddress: String = ""
    val ADDON_ADDRESS_REF: String = "Addresses"
    var addonCategorySelected: AddonCategoryModel? = null
    val USER_REFERENCE: String = "Users"
    val CATEGORY_REF: String = "Categories"
    val ADDON_CATEGORY_REF: String = "Addon"
    var categorySelected: CategoryModel? = null
    var foodSelected: FoodModel? = null
    var currentUser: UserModel? = null
    var totalPrice: String = "Всього: 0 грн."
    val PERMISSIONS_REQUEST_LOCATION = 100
    val MIN_TIME_BETWEEN_UPDATES: Long = 1000
    val MIN_DISTANCE_CHANGE_FOR_UPDATES: Float = 10f
    var STATUSES: List<String> = listOf(
        "Очікує підтвердження",
        "Підготовка",
        "Готовий до доставки",
        "В дорозі",
        "Доставлено",
        "Скасовано"
    )
    fun isConnectedToInternet(context: Context): Boolean{
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo != null && networkInfo.isConnected) {
            // Доступне підключення до Інтернету
            return  true
        }
        // Немає підключення до Інтернету
        return false
    }

}