package com.sokol.pizzadream.Model

class CategoryModel {
    var foods: MutableMap<String, FoodModel>? = HashMap()
    var name: String? = null
    var image: String? = null

    constructor()
    constructor(name: String?, image: String?, foods: MutableMap<String, FoodModel>?) {
        this.name = name
        this.image = image
        this.foods = foods
    }

}