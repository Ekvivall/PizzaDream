package com.sokol.pizzadream.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputLayout
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Database.Entities.CartItem
import com.sokol.pizzadream.Model.AddonModel
import com.sokol.pizzadream.Model.CommentModel
import com.sokol.pizzadream.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class AddCommentAdapter(val items: List<CartItem>, val context: Context) :
    RecyclerView.Adapter<AddCommentAdapter.MyViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var foodImgLayout: ConstraintLayout = view.findViewById(R.id.food_img_layout)
        var foodName: TextView = view.findViewById(R.id.txt_food_name)
        var foodSize: TextView = view.findViewById(R.id.txt_food_size)
        var foodAddon: TextView = view.findViewById(R.id.txt_food_addon)
        var foodPrice: TextView = view.findViewById(R.id.txt_food_price)
        var foodAddonTitle: TextView = view.findViewById(R.id.txt_food_addon_title)
        var foodQuantity: TextView = view.findViewById(R.id.txt_food_quantity)
        var tilComment: TextInputLayout = view.findViewById(R.id.til_comment)
        var edtComment: EditText = view.findViewById(R.id.edt_comment)
        var ratingBar: RatingBar = view.findViewById(R.id.rating_bar)
        var tilRatingBar: TextInputLayout = view.findViewById(R.id.til_rating_bar)
        var foodId = ""
        var categoryId = ""
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_add_comment_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.foodImgLayout.removeAllViews()
        val foodImg = ImageView(context)
        foodImg.id = View.generateViewId()
        foodImg.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        Glide.with(context).load(items[position].foodImage).into(foodImg)
        holder.foodImgLayout.addView(foodImg)
        holder.foodName.text = items[position].foodName.toString()
        holder.foodSize.text = items[position].foodSize
        val typeToken = object : TypeToken<List<AddonModel>>() {}.type
        val foodAddons = Gson().fromJson<List<AddonModel>>(items[position].foodAddon, typeToken)
        var res = ""
        holder.foodId = items[position].foodId
        holder.categoryId = items[position].categoryId
        if (foodAddons != null) {
            for (foodAddon in foodAddons) {
                if (foodAddon != foodAddons[0]) res += ", "
                res += foodAddon.name + " x" + foodAddon.userCount
                if (items[position].foodName.toString().contains("Конструктор")) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val foodImg = ImageView(context)
                        foodImg.id = View.generateViewId()
                        foodImg.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        val imageBitmap = withContext(Dispatchers.IO) {
                            Glide.with(context).asBitmap().load(foodAddon.imageFill).submit().get()
                        }
                        foodImg.setImageBitmap(imageBitmap)
                        holder.foodImgLayout.addView(foodImg)
                    }
                }
            }
        }
        holder.foodAddon.text = res
        if (items[position].foodAddon == "") {
            holder.foodAddonTitle.visibility = View.GONE
        } else {
            holder.foodAddonTitle.visibility = View.VISIBLE
        }
        holder.foodQuantity.text =
            StringBuilder(items[position].foodQuantity.toString()).append(" шт.")
        holder.foodPrice.text =
            StringBuilder("").append(Common.formatPrice(items[position].foodPrice * items[position].foodQuantity))
                .toString()
    }
}