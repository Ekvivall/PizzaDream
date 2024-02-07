package com.sokol.pizzadream.Model

import com.sokol.pizzadream.Database.Entities.CartItem

class OrderModel {
    var totalPrice:Double = 0.toDouble()
    var userId:String?=null
    var customerName:String?=null
    var customerPhone:String?=null
    var customerEmail:String?=null
    var customerAddress:String?=null
    var isDeliveryAddress:Boolean = false
    var transactionId: String?=null
    var cartItems: List<CartItem>? = null
    var isComment:Boolean = false
    var status:String?=null
    var orderedTime:Long = 0
    //var lat: Double = 0.toDouble()
    //var lng: Double = 0.toDouble()
}