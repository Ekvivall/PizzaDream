package com.sokol.pizzadream.ui.createCustomerPizza

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Model.FoodModel

class CreateCustomerPizzaViewModel:ViewModel() {
    private var foodDetailMutableLiveData: MutableLiveData<FoodModel>? = null
    fun getFoodDetailMutableLiveData(): MutableLiveData<FoodModel> {
        if (foodDetailMutableLiveData == null) {
            foodDetailMutableLiveData = MutableLiveData()
        }
        foodDetailMutableLiveData!!.value = Common.foodSelected
        return foodDetailMutableLiveData!!
    }
}