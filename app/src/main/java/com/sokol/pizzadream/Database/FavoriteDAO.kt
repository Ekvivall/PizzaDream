package com.sokol.pizzadream.Database

import androidx.room.Dao
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Single

@Dao
interface FavoriteDAO {
    @Query("INSERT INTO Favorites (foodId) VALUES(:foodId)")
    fun addToFavorites(foodId: String): Completable

    @Query("DELETE FROM Favorites WHERE foodId=:foodId")
    fun removeFromFavorites(foodId: String): Completable

    @Query("SELECT COUNT(foodId) FROM Favorites WHERE foodId=:foodId")
    fun isFavorite(foodId: String): Single<Int>
}