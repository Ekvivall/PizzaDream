package com.sokol.pizzadream.Common

import android.graphics.Typeface
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

    fun setWelcomeString(s: String, uid: String?, txtUser: TextView?) {
        val builder = SpannableStringBuilder()
        builder.append(s)
        val txtSpannable = SpannableString(uid)
        val boldSpan = StyleSpan(Typeface.BOLD)
        txtSpannable.setSpan(boldSpan, 0, uid!!.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
        builder.append(txtSpannable)
        txtUser?.setText(builder, TextView.BufferType.SPANNABLE)
    }

    fun createOrderId(): String {
        val characters = "ABCDEFGHIJKLMONPQRSTUVWXYZ"
        return StringBuilder().append(characters[Random().nextInt(characters.length)])
            .append(System.currentTimeMillis())
            .append(characters[Random().nextInt(characters.length)]).toString()
    }

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
}