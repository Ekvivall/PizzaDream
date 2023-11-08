package com.sokol.pizzadream.Model

class CategoryModel {
    var foods:List<FoodModel>?=ArrayList()
    var name: String? = null
    var image: String? = null
    constructor()
    constructor(name: String?, image: String?, foods: List<FoodModel>?) {
        this.name = name
        this.image = image
        this.foods = foods
    }

}