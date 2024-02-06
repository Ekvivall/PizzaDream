package com.sokol.pizzadream.ui.viewOrders

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sokol.pizzadream.Model.OrderModel

class ViewOrdersViewModel:ViewModel() {
    private var ordersListMutableLiveData: MutableLiveData<List<OrderModel>>? = null
    fun setOrdersListMutableLiveData(orderList: List<OrderModel>){
        ordersListMutableLiveData?.value = orderList
    }
}