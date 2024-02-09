package com.sokol.pizzadream.ui.foodlist

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Model.FoodModel

class FoodListViewModel : ViewModel() {
    private var foodListMutableLiveData: MutableLiveData<List<FoodModel>>? = null
    fun getFoodListMutableLiveData(): MutableLiveData<List<FoodModel>> {
        if (foodListMutableLiveData == null) {
            foodListMutableLiveData = MutableLiveData()
        }
        val foodList = Common.categorySelected!!.foods?.values?.toList() ?: emptyList()
        foodListMutableLiveData!!.value = foodList
        return foodListMutableLiveData!!
    }
}