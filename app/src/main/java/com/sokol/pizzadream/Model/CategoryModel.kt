package com.sokol.pizzadream.Model

class CategoryModel {
    var foods:MutableList<FoodModel>?=ArrayList()
    var name: String? = null
    var image: String? = null
    constructor()
    constructor(name: String?, image: String?, foods: MutableList<FoodModel>?) {
        this.name = name
        this.image = image
        this.foods = foods
    }

}