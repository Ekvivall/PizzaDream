package com.sokol.pizzadream.ui.reviewPizzeria

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sokol.pizzadream.Callback.IAddressLoadCallback
import com.sokol.pizzadream.Callback.ICartAddressLoadCallback
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Model.AddressModel

class ReviewPizzeriaViewModel:ViewModel(), IAddressLoadCallback {
    private var addressListMutableLiveData: MutableLiveData<List<AddressModel>>? = null
    private lateinit var messageError: MutableLiveData<String>
    private var addressLoadCallbackListener: IAddressLoadCallback = this
    fun getAddressListMutableLiveData(): MutableLiveData<List<AddressModel>> {
        if (addressListMutableLiveData == null) {
            addressListMutableLiveData = MutableLiveData()
        }
        loadAddresses()
        return addressListMutableLiveData!!
    }

    private fun loadAddresses() {
        val tempList = ArrayList<AddressModel>()
        val addressRef = FirebaseDatabase.getInstance().getReference(Common.ADDON_ADDRESS_REF)
        addressRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapshot in snapshot.children) {
                    val model = itemSnapshot.getValue(AddressModel::class.java)
                    tempList.add(model!!)
                }
                addressLoadCallbackListener.onAddressLoadSuccess(tempList)
            }

            override fun onCancelled(error: DatabaseError) {
                addressLoadCallbackListener.onAddressLoadFailed(error.message)
            }

        })
    }

    override fun onAddressLoadSuccess(addressList: List<AddressModel>) {
        addressListMutableLiveData?.value = addressList
    }

    override fun onAddressLoadFailed(message: String) {
        messageError.value = message
    }
}