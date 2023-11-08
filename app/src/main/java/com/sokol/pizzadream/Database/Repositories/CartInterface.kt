package com.sokol.pizzadream.Database.Repositories

import com.sokol.pizzadream.Database.Entities.CartItem
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface CartInterface {
    fun getAllCart(uid:String): Flowable<List<CartItem>>
    fun countItemInCart(uid:String): Single<Int>
    fun sumPrice(uid:String): Single<Double>
    fun getItemInCart(foodId:String, uid:String): Single<CartItem>
    fun insertOrReplaceAll(vararg cartItem: CartItem): Completable
    fun updateCart(cart: CartItem): Single<Int>
    fun deleteCart(cart: CartItem): Single<Int>
    fun cleanCart(uid:String): Single<Int>
    fun getItemWithAllOptionsInCart(foodId:String, uid:String, foodSize:String, foodAddon:String):Single<CartItem>
}