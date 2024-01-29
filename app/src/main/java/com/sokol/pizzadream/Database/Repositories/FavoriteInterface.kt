package com.sokol.pizzadream.Database.Repositories

import io.reactivex.Completable
import io.reactivex.Single

interface FavoriteInterface {
    fun addToFavorites(foodId: String): Completable
    fun removeFromFavorites(foodId: String): Completable
    fun isFavorite(foodId: String): Single<Int>
}