package com.sokol.pizzadream.ui.addCommentFood

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Database.Entities.CartItem

class CommentFoodViewModel:ViewModel() {
    private var foodMutableLiveData:MutableLiveData<CartItem>?=null
    fun getFoodMutableLiveData():MutableLiveData<CartItem>{
        if(foodMutableLiveData == null){
            foodMutableLiveData = MutableLiveData()
        }
        foodMutableLiveData!!.value = Common.cartItemSelected
        return foodMutableLiveData!!
    }
}