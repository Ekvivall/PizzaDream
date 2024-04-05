package com.sokol.pizzadream.Database.Repositories

import com.sokol.pizzadream.Database.Entities.CartItemDB
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface CartInterface {
    fun getAllCart(uid:String): Flowable<List<CartItemDB>>
    //fun sumPrice(uid:String): Single<Double>
    fun getItemInCart(foodId:String, uid:String): Single<CartItemDB>
    fun insertOrReplaceAll(vararg cartItem: CartItemDB): Completable
    fun updateCart(cart: CartItemDB): Single<Int>
    fun deleteCart(cart: CartItemDB): Single<Int>
    fun cleanCart(uid:String): Single<Int>
    fun getItemWithAllOptionsInCart(foodId:String, uid:String, foodSize:String, foodAddon:String):Single<CartItemDB>
}