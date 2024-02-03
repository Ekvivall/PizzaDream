package com.sokol.pizzadream.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.EventBus.AddonClick
import com.sokol.pizzadream.EventBus.UserAddonCountUpdate
import com.sokol.pizzadream.Model.AddonModel
import com.sokol.pizzadream.R
import org.greenrobot.eventbus.EventBus

class UserAddonAdapter(val items: List<AddonModel>, val context: Context) :
    RecyclerView.Adapter<UserAddonAdapter.MyViewHolder>() {
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var addonName: TextView = view.findViewById(R.id.user_addon_name)
        var addonPrice: TextView = view.findViewById(R.id.user_addon_price)
        var userAddonQuantity: TextView = view.findViewById(R.id.user_addon_quantity)
        var userAddonIncrease: ImageView = view.findViewById(R.id.user_addon_increase)
        var userAddonDecrease: ImageView = view.findViewById(R.id.user_addon_decrease)
        var userAddonRemove: ImageView = view.findViewById(R.id.user_addon_remove)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_user_addon_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.addonName.text = items[position].name
        val totalPrice = items[position].price.toDouble() * items[position].userCount
        val displayPrice = Math.round(totalPrice * 100.0) / 100.0
        holder.addonPrice.text =
            StringBuilder("").append(Common.formatPrice(displayPrice)).toString()
        holder.userAddonQuantity.text = items[position].userCount.toString()
        if (items[position].count == 1) {
            holder.userAddonIncrease.visibility = View.INVISIBLE
            holder.userAddonDecrease.visibility = View.INVISIBLE
        } else {
            holder.userAddonIncrease.setOnClickListener {
                val quantity = items[position].userCount
                if (quantity < items[position].count) {
                    items[position].userCount++
                    holder.userAddonQuantity.text = items[position].userCount.toString()
                    val totalPrice = items[position].price.toDouble() * items[position].userCount
                    val displayPrice = Math.round(totalPrice * 100.0) / 100.0
                    holder.addonPrice.text =
                        StringBuilder("").append(Common.formatPrice(displayPrice)).toString()
                    EventBus.getDefault().postSticky(UserAddonCountUpdate(false, items[position], 0))
                }
            }
            holder.userAddonDecrease.setOnClickListener {
                val quantity = items[position].userCount
                if (quantity > 1) {
                    items[position].userCount--
                    holder.userAddonQuantity.text = items[position].userCount.toString()
                    val totalPrice = items[position].price.toDouble() * items[position].userCount
                    val displayPrice = Math.round(totalPrice * 100.0) / 100.0
                    holder.addonPrice.text =
                        StringBuilder("").append(Common.formatPrice(displayPrice)).toString()
                    EventBus.getDefault().postSticky(UserAddonCountUpdate(false, items[position], 0))
                }
            }
        }
        holder.userAddonRemove.setOnClickListener {
            val t = items[position]
            items.drop(position)
            Common.foodSelected?.userSelectedAddon?.remove(t)
            //EventBus.getDefault().postSticky(AddonClick(false, t, position + 1))
            EventBus.getDefault().postSticky(UserAddonCountUpdate(true, t,position + 1))
            notifyDataSetChanged()
        }
    }

}