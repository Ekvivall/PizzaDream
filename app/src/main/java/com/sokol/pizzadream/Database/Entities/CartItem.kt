package com.sokol.pizzadream.Database.Entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import org.jetbrains.annotations.NotNull

@Entity(tableName = "Cart"/*, primaryKeys = ["uid", "foodId", "foodSize", "foodAddon"]*/)
class CartItem {
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null

    @NotNull
    @ColumnInfo(name = "foodId")
    var foodId: String = ""

    @ColumnInfo(name = "foodName")
    var foodName: String? = null

    @ColumnInfo(name = "foodImage")
    var foodImage: String? = null

    @ColumnInfo(name = "foodPrice")
    var foodPrice: Double = 0.0

    @ColumnInfo(name = "foodQuantity")
    var foodQuantity: Int = 0

    @NotNull
    @ColumnInfo(name = "foodAddon")
    var foodAddon: String = ""

    @NotNull
    @ColumnInfo(name = "foodSize")
    var foodSize: String = ""

    @ColumnInfo(name = "userEmail")
    var userEmail: String? = ""

    /*@ColumnInfo(name = "foodExtraPrice")
    var foodExtraPrice: Double = 0.0*/

    @NotNull
    @ColumnInfo(name = "uid")
    var uid: String = ""
    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CartItem

        if (foodId != other.foodId) return false
        if (foodAddon != other.foodAddon) return false
        if (foodSize != other.foodSize) return false

        return true
    }

    override fun hashCode(): Int {
        var result = foodId.hashCode()
        result = 31 * result + foodAddon.hashCode()
        result = 31 * result + foodSize.hashCode()
        return result
    }

}