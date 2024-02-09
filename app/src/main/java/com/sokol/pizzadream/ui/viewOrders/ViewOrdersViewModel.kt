package com.sokol.pizzadream.ui.viewOrders

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sokol.pizzadream.Callback.IOrderLoadCallback
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Model.NewsModel
import com.sokol.pizzadream.Model.OrderModel

class ViewOrdersViewModel : ViewModel(), IOrderLoadCallback {
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
        val tempList = ArrayList<OrderModel>()
        val ordersRef =
            FirebaseDatabase.getInstance().getReference(Common.ORDER_REF).orderByChild("userId")
                .equalTo(Common.currentUser!!.uid)
        ordersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemsSnapshot in snapshot.children) {
                    val model = itemsSnapshot.getValue(OrderModel::class.java)
                    tempList.add(model!!)
                }
                val sortedList = tempList.sortedByDescending { it.orderedTime }
                ordersLoadCallbackListener.onOrderLoadSuccess(sortedList)
            }

            override fun onCancelled(error: DatabaseError) {
                ordersLoadCallbackListener.onOrderLoadFailed(error.message)
            }

        })
    }

    override fun onOrderLoadSuccess(orderList: List<OrderModel>) {
        ordersListMutableLiveData?.value = orderList
    }

    override fun onOrderLoadFailed(message: String) {
        messageError.value = message
    }
}