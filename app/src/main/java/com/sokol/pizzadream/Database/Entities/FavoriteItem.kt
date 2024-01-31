package com.sokol.pizzadream.Database.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "Favorites")
class FavoriteItem {
    @PrimaryKey()
    var foodId: String = ""

    @ColumnInfo(name = "foodName")
    var foodName: String? = null

    @ColumnInfo(name = "foodImage")
    var foodImage: String? = null

    @ColumnInfo(name = "foodPrice")
    var foodPrice: Double = 0.0

    @ColumnInfo(name = "uid")
    var uid: String = ""
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FavoriteItem

        if (foodId != other.foodId) return false

        return true
    }

    override fun hashCode(): Int {
        return foodId.hashCode()
    }

}