package com.sokol.pizzadream.Database.Repositories

import com.sokol.pizzadream.Database.FavoriteDAO
import io.reactivex.Completable
import io.reactivex.Single

class FavoriteRepository(private val favoriteDAO: FavoriteDAO) : FavoriteInterface {
    override fun addToFavorites(foodId: String): Completable {
        return favoriteDAO.addToFavorites(foodId)
    }

    override fun removeFromFavorites(foodId: String): Completable {
        return favoriteDAO.removeFromFavorites(foodId)
    }

    override fun isFavorite(foodId: String): Single<Int> {
        return favoriteDAO.isFavorite(foodId)
    }
}