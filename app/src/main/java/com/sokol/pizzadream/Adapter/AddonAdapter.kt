package com.sokol.pizzadream.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sokol.pizzadream.Callback.IRecyclerItemClickListener
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.EventBus.AddonClick
import com.sokol.pizzadream.Model.AddonModel
import com.sokol.pizzadream.R
import org.greenrobot.eventbus.EventBus

class AddonAdapter(var items: List<AddonModel>, val context: Context) :
    RecyclerView.Adapter<AddonAdapter.MyViewHolder>() {
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var addonName: TextView = view.findViewById(R.id.addon_name)
        var addonImage: ImageView = view.findViewById(R.id.addon_image)
        var addonPrice: TextView = view.findViewById(R.id.addon_price)
        var addonWeight: TextView = view.findViewById(R.id.addon_weight)
        var addonAdd: ImageView = view.findViewById(R.id.addon_add)
        var addonLayout: LinearLayout = view.findViewById(R.id.addon_layout)
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
            .inflate(R.layout.layout_addon_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(items[position].image).into(holder.addonImage)
        holder.addonName.text = items[position].name
        holder.addonPrice.text = items[position].price.toString() + " грн."
        if (items[position].weight != 0) {
            holder.addonWeight.text = items[position].weight.toString() + " г"
        }
        if (Common.foodSelected?.userSelectedAddon?.any { it.name == items[position].name } == true) {
            holder.addonLayout.setBackgroundResource(R.drawable.rectangle_green_background)
            holder.addonAdd.visibility = View.VISIBLE
        } else {
            holder.addonLayout.setBackgroundResource(R.drawable.oval_background)
            holder.addonAdd.visibility = View.INVISIBLE
        }
        holder.setListener(object : IRecyclerItemClickListener {
            override fun onItemClick(view: View, pos: Int) {
                if (Common.foodSelected?.userSelectedAddon?.any { it.name == items[position].name } == true) {
                    val index =
                        Common.foodSelected?.userSelectedAddon?.indexOfFirst { it.name == items[pos].name }
                            ?: -1
                    Common.foodSelected?.userSelectedAddon?.removeAt(index)
                    EventBus.getDefault().postSticky(AddonClick(false, items[pos], index + 1))
                } else {
                    items[pos].userCount = 1
                    if (items[pos].count == 1) {
                        Common.foodSelected!!.userSelectedAddon?.add(0, items[pos])
                        EventBus.getDefault().postSticky(AddonClick(true, items[pos], 1))
                    } else {
                        Common.foodSelected!!.userSelectedAddon?.add(items[pos])
                        EventBus.getDefault().postSticky(
                            AddonClick(
                                true,
                                items[pos],
                                Common.foodSelected!!.userSelectedAddon!!.size
                            )
                        )
                    }
                }
                notifyDataSetChanged()
                /*Common.foodSelected!!.userSelectedAddon?.add(items[pos])
                EventBus.getDefault().postSticky(AddonCategoryClick(true, items[pos]))
                notifyDataSetChanged()*/
            }
        })
    }
}