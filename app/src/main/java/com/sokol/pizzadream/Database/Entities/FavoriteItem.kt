package com.sokol.pizzadream.Database.Entities

import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(tableName = "Favorites")
class FavoriteItem {
    @PrimaryKey()
    var foodId: String =""
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as FavoriteItem

        if (foodId != other.foodId) return false

        return true
    }

    override fun hashCode(): Int {
        return foodId?.hashCode() ?: 0
    }

}