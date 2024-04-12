package com.sokol.pizzadream.Database.DAO

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.sokol.pizzadream.Database.Entities.CartItemDB
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface CartDAO {
    @Query("SELECT * FROM Cart WHERE uid=:uid")
    fun getAllCart(uid:String):Flowable<List<CartItemDB>>
    @Query("SELECT * FROM Cart WHERE foodId=:foodId AND uid=:uid")
    fun getItemInCart(foodId:String, uid:String):Single<CartItemDB>
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertOrReplaceAll(vararg cartItem: CartItemDB):Completable
    @Update(onConflict = OnConflictStrategy.REPLACE)
    fun updateCart(cart: CartItemDB):Single<Int>
    @Delete
    fun deleteCart(cart: CartItemDB):Single<Int>
    @Query("DELETE FROM Cart WHERE uid=:uid")
    fun cleanCart(uid:String): Single<Int>
    @Query("SELECT * FROM Cart WHERE foodId=:foodId AND uid=:uid AND foodSize=:foodSize AND foodAddon=:foodAddon")
    fun getItemWithAllOptionsInCart(foodId:String, uid:String, foodSize:String, foodAddon:String):Single<CartItemDB>
}