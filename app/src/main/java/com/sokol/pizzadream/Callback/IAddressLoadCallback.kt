package com.sokol.pizzadream.Callback

import com.sokol.pizzadream.Model.AddressModel

interface IAddressLoadCallback {
    fun onAddressLoadSuccess(addressList: List<AddressModel>)
    fun onAddressLoadFailed(message:String)
}