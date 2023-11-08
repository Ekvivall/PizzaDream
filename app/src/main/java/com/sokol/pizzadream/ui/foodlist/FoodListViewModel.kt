package com.sokol.pizzadream.ui.foodlist

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sokol.pizzadream.Callback.ICategoryLoadCallback
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Model.CategoryModel
import com.sokol.pizzadream.Model.FoodModel

class FoodListViewModel:ViewModel(){
    private var foodListMutableLiveData: MutableLiveData<List<FoodModel>>? = null
    fun getFoodListMutableLiveData(): MutableLiveData<List<FoodModel>> {
        if (foodListMutableLiveData == null) {
            foodListMutableLiveData = MutableLiveData()
        }
        foodListMutableLiveData!!.value = Common.categorySelected!!.foods
        return foodListMutableLiveData!!
    }
}