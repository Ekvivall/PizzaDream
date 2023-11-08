package com.sokol.pizzadream.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sokol.pizzadream.Callback.IRecyclerItemClickListener
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.EventBus.AddonCategoryClick
import com.sokol.pizzadream.Model.AddonCategoryModel
import com.sokol.pizzadream.R
import org.greenrobot.eventbus.EventBus

class AddonCategoryAdapter (val items: List<AddonCategoryModel>, val context: Context) :
    RecyclerView.Adapter<AddonCategoryAdapter.MyViewHolder>() {
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var categoryName: TextView = view.findViewById(R.id.addon_category_text)
        private var listener: IRecyclerItemClickListener? = null
        fun setListener(listener: IRecyclerItemClickListener){
            this.listener = listener
            itemView.setOnClickListener(this)
        }
        override fun onClick(p0: View?) {
            listener!!.onItemClick(p0!!, adapterPosition)
        }

    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_addon_category_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        holder.categoryName.text = items[position].name
        if(Common.addonCategorySelected?.name ==items[position].name){
            holder.categoryName.setTextColor(context.resources.getColor(R.color.red))
        }
        else{
            holder.categoryName.setTextColor(context.resources.getColor(R.color.black))
        }
        holder.setListener(object: IRecyclerItemClickListener {
            override fun onItemClick(view: View, pos: Int){
                Common.addonCategorySelected = items[pos]
                EventBus.getDefault().postSticky(AddonCategoryClick(true, items[pos]))
                notifyDataSetChanged()
            }
        })
    }

}