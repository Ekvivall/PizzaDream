package com.sokol.pizzadream.Callback

import com.sokol.pizzadream.Model.AddonCategoryModel
import com.sokol.pizzadream.Model.CategoryModel

interface IAddonCategoryLoadCallback {
    fun onCategoryLoadSuccess(categoriesList: List<AddonCategoryModel>)
    fun onCategoryLoadFailed(message:String)
}