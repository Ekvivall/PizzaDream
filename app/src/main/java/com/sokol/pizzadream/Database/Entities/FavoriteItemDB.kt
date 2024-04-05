package com.sokol.pizzadream.Database.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Favorites")
class FavoriteItemDB {
    @PrimaryKey()
    var foodId: String = ""

    @ColumnInfo(name = "uid")
    var uid: String = ""

    @ColumnInfo(name = "categoryId")
    var categoryId: String = ""
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FavoriteItemDB

        if (foodId != other.foodId) return false

        return true
    }

    override fun hashCode(): Int {
        return foodId.hashCode()
    }

}