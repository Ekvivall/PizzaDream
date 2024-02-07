package com.sokol.pizzadream.ui.viewOrders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sokol.pizzadream.Callback.IOrderLoadCallback
import com.sokol.pizzadream.Callback.IVacanciesLoadCallback
import com.sokol.pizzadream.Model.OrderModel
import com.sokol.pizzadream.Model.VacancyModel

class ViewOrdersViewModel:ViewModel(), IOrderLoadCallback {
    private var ordersListMutableLiveData: MutableLiveData<List<OrderModel>>? = null
    private lateinit var messageError: MutableLiveData<String>
    private var ordersLoadCallbackListener: IOrderLoadCallback = this
    val orders: LiveData<List<OrderModel>>
        get() {
            if (ordersListMutableLiveData == null) {
                ordersListMutableLiveData = MutableLiveData()
                messageError = MutableLiveData()
                loadOrdersList()
            }
            return ordersListMutableLiveData!!
        }

    private fun loadOrdersList() {
        TODO("Not yet implemented")
    }

    override fun onOrderLoadSuccess(orderList: List<OrderModel>) {
        TODO("Not yet implemented")
    }

    override fun onOrderLoadFailed(message: String) {
        messageError.value = message
    }
}