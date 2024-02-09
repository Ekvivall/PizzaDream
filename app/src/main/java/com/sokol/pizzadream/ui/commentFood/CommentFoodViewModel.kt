package com.sokol.pizzadream.ui.commentFood

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sokol.pizzadream.Callback.IAddressLoadCallback
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Database.Entities.CartItem
import com.sokol.pizzadream.Model.AddressModel

class CommentFoodViewModel:ViewModel() {
    private var foodMutableLiveData:MutableLiveData<List<CartItem>>?=null
    fun getFoodMutableLiveData():MutableLiveData<List<CartItem>>{
        if(foodMutableLiveData == null){
            foodMutableLiveData = MutableLiveData()
        }
        foodMutableLiveData!!.value = Common.orderSelected!!.cartItems
        return foodMutableLiveData!!
    }
}