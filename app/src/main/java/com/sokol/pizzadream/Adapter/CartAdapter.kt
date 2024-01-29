package com.sokol.pizzadream.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Database.PizzaDatabase
import com.sokol.pizzadream.Database.Entities.CartItem
import com.sokol.pizzadream.Database.Repositories.CartInterface
import com.sokol.pizzadream.Database.Repositories.CartRepository
import com.sokol.pizzadream.EventBus.RemoveItemsInCart
import com.sokol.pizzadream.EventBus.UpdateItemsInCart
import com.sokol.pizzadream.Model.AddonModel
import com.sokol.pizzadream.R
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus


class CartAdapter(var items: List<CartItem>, val context: Context) :
    RecyclerView.Adapter<CartAdapter.MyViewHolder>() {
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var foodImgLayout: ConstraintLayout = view.findViewById(R.id.img_cart_layout)

        //var foodImg: ImageView = view.findViewById(R.id.img_cart)
        var foodName: TextView = view.findViewById(R.id.txt_food_name_cart)
        var foodRemove: ImageView = view.findViewById(R.id.img_food_remove_cart)
        var foodSize: TextView = view.findViewById(R.id.txt_food_size_cart)
        var foodAddon: TextView = view.findViewById(R.id.txt_food_addon_cart)
        var foodIncrease: ImageView = view.findViewById(R.id.img_food_increase_cart)
        var foodQuantity: TextView = view.findViewById(R.id.txt_food_quantity_cart)
        var foodDecrease: ImageView = view.findViewById(R.id.img_food_decrease_cart)
        var foodPrice: TextView = view.findViewById(R.id.txt_food_price_cart)
        var foodAddonTitle: TextView = view.findViewById(R.id.txt_food_addon_cart_title)
    }

    internal var compositeDisposable: CompositeDisposable = CompositeDisposable()
    internal var cart: CartInterface =
        CartRepository(PizzaDatabase.getInstance(context).getCartDAO())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_cart_item, parent, false)
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
        holder.foodQuantity.text = items[position].foodQuantity.toString()
        holder.foodPrice.text =
            StringBuilder("").append(Common.formatPrice(items[position].foodPrice * items[position].foodQuantity))
                .toString()
        holder.foodIncrease.setOnClickListener {
            items[position].foodQuantity++
            holder.foodQuantity.text = items[position].foodQuantity.toString()
            holder.foodPrice.text =
                StringBuilder("").append(Common.formatPrice(items[position].foodPrice * items[position].foodQuantity))
                    .toString()
            EventBus.getDefault().postSticky(UpdateItemsInCart(items[position]))
        }
        holder.foodDecrease.setOnClickListener {
            if (items[position].foodQuantity > 1) {
                items[position].foodQuantity--
                holder.foodQuantity.text = items[position].foodQuantity.toString()
                holder.foodPrice.text =
                    StringBuilder("").append(Common.formatPrice(items[position].foodPrice * items[position].foodQuantity))
                        .toString()
                EventBus.getDefault().postSticky(UpdateItemsInCart(items[position]))
            }
        }
        holder.foodRemove.setOnClickListener {
            cart.deleteCart(items[position]).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(object : SingleObserver<Int> {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onSuccess(t: Int) {
                        items = items.filterIndexed { index, _ -> index != position }
                        notifyDataSetChanged()
                        EventBus.getDefault().postSticky(RemoveItemsInCart(position))
                    }

                })
        }
    }
}