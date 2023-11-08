package com.sokol.pizzadream.Database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.sokol.pizzadream.Database.Entities.CartItem

@Database(version = 1, entities = [CartItem::class], exportSchema = false)
abstract class CartDatabase:RoomDatabase() {
    abstract fun getCartDAO():CartDAO
    companion object{
        private var instance:CartDatabase?=null
        private val LOCK = Any()
        fun getInstance(context: Context):CartDatabase{
            if(instance == null)
                instance = Room.databaseBuilder(context, CartDatabase::class.java, "PizzaDream2").build()
            return instance!!
        }
    }
}