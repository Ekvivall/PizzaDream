package com.sokol.pizzadream.Model

class FoodModel {
    var id:String? = null
    var categoryId:String? = null
    var name: String? = null
    var image: String? = null
    var description: String? = null
    var addon: String? = null
    var size: List<SizeModel> = ArrayList()
    var ratingSum: Long = 0L
    var ratingCount: Long = 0L
    var userSelectedAddon:MutableList<AddonModel>?=null
    var userSelectedSize:SizeModel?=null
}