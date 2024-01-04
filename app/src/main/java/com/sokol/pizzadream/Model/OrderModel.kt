package com.sokol.pizzadream.Model

import com.sokol.pizzadream.Database.Entities.CartItem

class OrderModel {
    var totalPrice:Double = 0.toDouble()
    var userId:String?=null
    var customerName:String?=null
    var customerPhone:String?=null
    var customerEmail:String?=null
    var customerAddress:String?=null
    var isCod:Boolean = false
    var cartItems: List<CartItem>? = null
    //var lat: Double = 0.toDouble()
    //var lng: Double = 0.toDouble()
}