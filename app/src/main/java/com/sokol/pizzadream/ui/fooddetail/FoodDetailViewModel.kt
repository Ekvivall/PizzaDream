package com.sokol.pizzadream.ui.fooddetail

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sokol.pizzadream.Callback.IAddonCategoryLoadCallback
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Model.AddonCategoryModel
import com.sokol.pizzadream.Model.AddonModel
import com.sokol.pizzadream.Model.FoodModel

class FoodDetailViewModel : ViewModel(), IAddonCategoryLoadCallback {

    private var foodDetailMutableLiveData: MutableLiveData<FoodModel>? = null
    private var categoryListMutableLiveData: MutableLiveData<List<AddonCategoryModel>>? = null
    private lateinit var messageError: MutableLiveData<String>
    private var categoryLoadCallbackListener: IAddonCategoryLoadCallback = this
    fun getFoodDetailMutableLiveData(): MutableLiveData<FoodModel> {
        if (foodDetailMutableLiveData == null) {
            foodDetailMutableLiveData = MutableLiveData()
        }
        foodDetailMutableLiveData!!.value = Common.foodSelected
        return foodDetailMutableLiveData!!
    }
    val categoryList: LiveData<List<AddonCategoryModel>>
        get() {
            if (categoryListMutableLiveData == null) {
                categoryListMutableLiveData = MutableLiveData()
                messageError = MutableLiveData()
                loadCategoryList()
            }
            return categoryListMutableLiveData!!
        }

    private fun loadCategoryList() {
        val tempList = ArrayList<AddonCategoryModel>()
        val categoryRef = FirebaseDatabase.getInstance().getReference(Common.ADDON_CATEGORY_REF)
        categoryRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemSnapshot in snapshot.children) {
                    if (itemSnapshot.key == Common.foodSelected?.addon) {
                        for (item in itemSnapshot.children) {
                            val model = item.getValue(AddonCategoryModel::class.java)
                            tempList.add(model!!)
                        }
                    }
                }
                categoryLoadCallbackListener.onCategoryLoadSuccess(tempList)
            }

            override fun onCancelled(error: DatabaseError) {
                categoryLoadCallbackListener.onCategoryLoadFailed(error.message)
            }

        })
    }

    override fun onCategoryLoadSuccess(categoriesList: List<AddonCategoryModel>) {
        categoryListMutableLiveData?.value = categoriesList
    }

    override fun onCategoryLoadFailed(message: String) {
        messageError.value = message
    }
}