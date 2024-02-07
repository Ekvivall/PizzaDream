package com.sokol.pizzadream.Callback

import com.sokol.pizzadream.Model.OrderModel

interface IOrderLoadCallback {
    fun onOrderLoadSuccess(orderList: List<OrderModel>)
    fun onOrderLoadFailed(message:String)
}