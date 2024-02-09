package com.sokol.pizzadream.ui.viewOrderDetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Model.OrderModel
import com.sokol.pizzadream.Model.VacancyModel

class OrderDetailViewModel:ViewModel() {
    private var orderMutableLiveData: MutableLiveData<OrderModel>? = null
    fun getOrderDetailMutableLiveData(): MutableLiveData<OrderModel> {
        if (orderMutableLiveData == null) {
            orderMutableLiveData = MutableLiveData()
        }
        orderMutableLiveData!!.value = Common.orderSelected
        return orderMutableLiveData!!
    }
}