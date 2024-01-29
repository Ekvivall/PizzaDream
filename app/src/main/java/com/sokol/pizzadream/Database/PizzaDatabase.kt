package com.sokol.pizzadream.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sokol.pizzadream.Database.Entities.CartItem
import com.sokol.pizzadream.Database.Entities.FavoriteItem

@Database(version = 1, entities = [CartItem::class, FavoriteItem::class], exportSchema = false)
abstract class PizzaDatabase:RoomDatabase() {
    abstract fun getCartDAO():CartDAO
    abstract fun getFavoriteDAO(): FavoriteDAO
    companion object{
        private var instance: PizzaDatabase?=null
        private val LOCK = Any()
        fun getInstance(context: Context): PizzaDatabase {
            if(instance == null)
                instance = Room.databaseBuilder(context, PizzaDatabase::class.java, "PizzaDream2").build()
            return instance!!
        }
    }
}