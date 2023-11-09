package com.sokol.pizzadream.Callback

import com.sokol.pizzadream.Model.AddonCategoryModel

interface IAddonAddressLoadCallback {
    fun onCategoryLoadSuccess(addressList: List<String>)
    fun onCategoryLoadFailed(message:String)
}