package com.sokol.pizzadream.Database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.sokol.pizzadream.Database.Entities.FavoriteItem
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface FavoriteDAO {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun addToFavorites(favoriteItem: FavoriteItem): Completable

    @Query("DELETE FROM Favorites WHERE foodId=:foodId")
    fun removeFromFavorites(foodId: String): Completable

    @Query("SELECT COUNT(foodId) FROM Favorites WHERE foodId=:foodId")
    fun isFavorite(foodId: String): Single<Int>
    @Query("SELECT * FROM Favorites WHERE uid=:uid")
    fun getAllFavorites(uid:String): Flowable<List<FavoriteItem>>
}