package com.sokol.pizzadream.Database.Repositories

import com.sokol.pizzadream.Database.DAO.CartDAO
import com.sokol.pizzadream.Database.Entities.CartItemDB
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class CartRepository(private val cartDAO: CartDAO): CartInterface {
    override fun getAllCart(uid: String): Flowable<List<CartItemDB>> {
        return cartDAO.getAllCart(uid)
    }


    /*override fun sumPrice(uid: String): Single<Double> {
        return cartDAO.sumPrice(uid)
    }*/

    override fun getItemInCart(foodId: String, uid: String): Single<CartItemDB> {
        return cartDAO.getItemInCart(foodId, uid)
    }

    override fun insertOrReplaceAll(vararg cartItem: CartItemDB): Completable {
        return cartDAO.insertOrReplaceAll(*cartItem)
    }

    override fun updateCart(cart: CartItemDB): Single<Int> {
        return cartDAO.updateCart(cart)
    }

    override fun deleteCart(cart: CartItemDB): Single<Int> {
        return cartDAO.deleteCart(cart)
    }

    override fun cleanCart(uid: String): Single<Int> {
        return cartDAO.cleanCart(uid)
    }

    override fun getItemWithAllOptionsInCart(
        foodId: String,
        uid: String,
        foodSize: String,
        foodAddon: String
    ): Single<CartItemDB> {
        return cartDAO.getItemWithAllOptionsInCart(foodId, uid, foodSize, foodAddon)
    }
}