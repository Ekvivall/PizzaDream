package com.sokol.pizzadream.Database.Repositories

import com.sokol.pizzadream.Database.Entities.FavoriteItem
import com.sokol.pizzadream.Model.FoodModel
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

interface FavoriteInterface {
    fun addToFavorites(favoriteItem: FavoriteItem): Completable
    fun removeFromFavorites(foodId: String, uid:String): Completable
    fun isFavorite(foodId: String, uid:String): Single<Int>
    fun getAllFavorites(uid:String): Flowable<List<FavoriteItem>>
}