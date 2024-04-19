package com.sokol.pizzadream.ui.home

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

class HomeViewModel : ViewModel(), ICategoryLoadCallback {

    private var categoryListMutableLiveData: MutableLiveData<List<CategoryModel>>? = null
    private var foodListMutableLiveData: MutableLiveData<List<FoodModel>>? = null
    private lateinit var messageError: MutableLiveData<String>
    private var categoryLoadCallbackListener: ICategoryLoadCallback = this

    val categoryList: LiveData<List<CategoryModel>>
        get() {
            if (categoryListMutableLiveData == null) {
                categoryListMutableLiveData = MutableLiveData()
                messageError = MutableLiveData()
                loadCategoryList()
            }
            return categoryListMutableLiveData!!
        }

    fun getFoodListMutableLiveData(): MutableLiveData<List<FoodModel>> {
        if (foodListMutableLiveData == null) {
            foodListMutableLiveData = MutableLiveData()
            loadCategoryList()
        }
        return foodListMutableLiveData!!
    }

    private fun loadCategoryList() {
        val tempList = ArrayList<CategoryModel>()
        val categoryRef = FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF)
        categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapshot in snapshot.children) {
                    val model = itemSnapshot.getValue(CategoryModel::class.java)
                    tempList.add(model!!)
                }
                val allFoods = mutableListOf<FoodModel>()
                for (category in tempList) {
                    allFoods.addAll(category.foods!!.values.filter { it.image != "" }.toList())
                }
                foodListMutableLiveData!!.value = allFoods.sortedWith(compareByDescending {
                    if (it.ratingCount == 0L) {
                        0f
                    } else {
                        it.ratingSum.toFloat() / it.ratingCount
                    }
                })//.take(15)
                categoryLoadCallbackListener.onCategoryLoadSuccess(tempList)
            }

            override fun onCancelled(error: DatabaseError) {
                categoryLoadCallbackListener.onCategoryLoadFailed(error.message)
            }

        })
    }

    override fun onCategoryLoadSuccess(categoriesList: List<CategoryModel>) {
        categoryListMutableLiveData?.value = categoriesList
    }

    override fun onCategoryLoadFailed(message: String) {
        messageError.value = message
    }
}