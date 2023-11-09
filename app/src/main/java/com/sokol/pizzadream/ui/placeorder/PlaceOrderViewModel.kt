package com.sokol.pizzadream.ui.placeorder

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sokol.pizzadream.Callback.IAddonAddressLoadCallback
import com.sokol.pizzadream.Common.Common

class PlaceOrderViewModel : ViewModel(), IAddonAddressLoadCallback {
    private var addressListMutableLiveData: MutableLiveData<List<String>>? = null
    private lateinit var messageError: MutableLiveData<String>
    private var addressLoadCallbackListener: IAddonAddressLoadCallback = this
    fun getAddressListMutableLiveData(): MutableLiveData<List<String>> {
        if (addressListMutableLiveData == null) {
            addressListMutableLiveData = MutableLiveData()
        }
        loadAddresses()
        return addressListMutableLiveData!!
    }

    private fun loadAddresses() {
        val tempList = ArrayList<String>()
        val addressRef = FirebaseDatabase.getInstance().getReference(Common.ADDON_ADDRESS_REF)
        addressRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapshot in snapshot.children) {
                    val model = itemSnapshot.getValue(String()::class.java)
                    tempList.add(model!!)
                }
                addressLoadCallbackListener.onCategoryLoadSuccess(tempList)
            }

            override fun onCancelled(error: DatabaseError) {
                addressLoadCallbackListener.onCategoryLoadFailed(error.message)
            }

        })
    }

    override fun onCategoryLoadSuccess(addressList: List<String>) {
        addressListMutableLiveData?.value = addressList
    }

    override fun onCategoryLoadFailed(message: String) {
        messageError.value = message
    }
}