package com.sokol.pizzadream.Callback

import com.sokol.pizzadream.Model.CategoryModel

interface ICategoryLoadCallback {
    fun onCategoryLoadSuccess(categoriesList: List<CategoryModel>)
    fun onCategoryLoadFailed(message:String)
}