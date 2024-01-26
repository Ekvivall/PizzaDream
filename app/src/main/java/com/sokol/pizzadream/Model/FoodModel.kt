package com.sokol.pizzadream.Model

class FoodModel {
    var id:String? = null
    var name: String? = null
    var image: String? = null
    var description: String? = null
    var addon: String? = null
    var size: List<SizeModel> = ArrayList()
    var ratingValue: Double = 0.toDouble()
    var ratingCount: Int = 0
    var userSelectedAddon:MutableList<AddonModel>?=null
    var userSelectedSize:SizeModel?=null
}