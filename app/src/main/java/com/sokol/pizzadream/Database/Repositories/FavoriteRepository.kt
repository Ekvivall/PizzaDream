package com.sokol.pizzadream.Database.Repositories

import com.sokol.pizzadream.Database.Entities.FavoriteItem
import com.sokol.pizzadream.Database.FavoriteDAO
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

class FavoriteRepository(private val favoriteDAO: FavoriteDAO) : FavoriteInterface {
    override fun addToFavorites(favoriteItem: FavoriteItem): Completable {
        return favoriteDAO.addToFavorites(favoriteItem)
    }

    override fun removeFromFavorites(foodId: String, uid:String): Completable {
        return favoriteDAO.removeFromFavorites(foodId, uid)
    }

    override fun isFavorite(foodId: String, uid:String): Single<Int> {
        return favoriteDAO.isFavorite(foodId, uid)
    }override fun getAllFavorites(uid: String): Flowable<List<FavoriteItem>> {
        return  favoriteDAO.getAllFavorites(uid)
    }
}