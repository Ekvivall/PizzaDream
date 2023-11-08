package com.sokol.pizzadream.Model

class FoodModel {
    var id:String? = null
    var name: String? = null
    var image: String? = null
    var description: String? = null
    var addon: String? = null
    //var description: List<IngredientModel> = ArrayList<IngredientModel>()
    //var price: Long = 0
    //var addon: List<AddonModel> = ArrayList<AddonModel>()
    var size: List<SizeModel> = ArrayList()/*var ratingValue: Double = 0.toDouble()
    var ratingCount: Long = 0.toLong()
    var userSelectedAddon: MutableList<AddonModel>?=null
    var userSelectedSize: SizeModel?=null*/
    var ratingValue: Double = 0.toDouble()
    var ratingCount: Int = 0
    var userSelectedAddon:MutableList<AddonModel>?=null
    var userSelectedSize:SizeModel?=null
}