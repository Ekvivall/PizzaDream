package com.sokol.pizzadream.Callback

interface ICartAddressLoadCallback {
    fun onAddressLoadSuccess(addressList: List<String>)
    fun onAddressLoadFailed(message:String)
}