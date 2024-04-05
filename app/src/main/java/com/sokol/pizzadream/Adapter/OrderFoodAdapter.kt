package com.sokol.pizzadream.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sokol.pizzadream.Callback.IRecyclerItemClickListener
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Database.Entities.CartItemDB
import com.sokol.pizzadream.EventBus.FoodItemClick
import com.sokol.pizzadream.EventBus.ViewAddCommentClick
import com.sokol.pizzadream.Model.AddonModel
import com.sokol.pizzadream.Model.CartItem
import com.sokol.pizzadream.Model.FoodModel
import com.sokol.pizzadream.Model.OrderModel
import com.sokol.pizzadream.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus

class OrderFoodAdapter(val items: List<CartItem>, val context: Context, val order:OrderModel) :
    RecyclerView.Adapter<OrderFoodAdapter.MyViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var foodImgLayout: ConstraintLayout = view.findViewById(R.id.img_order_layout)
        var foodName: TextView = view.findViewById(R.id.txt_food_name_order)
        var foodSize: TextView = view.findViewById(R.id.txt_food_size_order)
        var foodAddon: TextView = view.findViewById(R.id.txt_food_addon_order)
        var foodPrice: TextView = view.findViewById(R.id.txt_food_price_order)
        var foodAddonTitle: TextView = view.findViewById(R.id.txt_food_addon_order_title)
        var foodQuantity: TextView = view.findViewById(R.id.txt_food_quantity_order)
        var btnAddComment: Button = view.findViewById(R.id.btn_add_comment)
        private var listener: IRecyclerItemClickListener? = null
        fun setListener(listener: IRecyclerItemClickListener) {
            this.listener = listener
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            listener!!.onItemClick(p0!!, adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_order_food_item, parent, false)
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
        holder.setListener(object : IRecyclerItemClickListener {
            override fun onItemClick(view: View, pos: Int) {
                findFoodItem(pos)
            }
        })
        if (order.status == Common.STATUSES[4] && !items[position].isComment) holder.btnAddComment.visibility =
            View.VISIBLE
        else holder.btnAddComment.visibility = View.GONE
        holder.btnAddComment.setOnClickListener {
            Common.orderSelected = order
            Common.cartItemSelected = items[position]
            EventBus.getDefault().postSticky(ViewAddCommentClick(true))
        }
    }

    fun findFoodItem(position: Int) {
        val categoryRef = FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF)
        categoryRef.child(items[position].categoryId).child("foods").child(items[position].foodId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val foodModel = snapshot.getValue(FoodModel::class.java)
                    Common.foodSelected = foodModel
                    EventBus.getDefault().postSticky(FoodItemClick(true, foodModel!!))

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }
}