package com.sokol.pizzadream.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sokol.pizzadream.Callback.IRecyclerItemClickListener
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.EventBus.CategoryClick
import com.sokol.pizzadream.Model.CategoryModel
import com.sokol.pizzadream.R
import org.greenrobot.eventbus.EventBus

class CategoryAdapter(val items: List<CategoryModel>, val context: Context) :
    RecyclerView.Adapter<CategoryAdapter.MyViewHolder>() {
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var categoryName: TextView = view.findViewById(R.id.category_name)
        var categoryImage: ImageView = view.findViewById(R.id.category_image)
        private var listener:IRecyclerItemClickListener? = null
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
            .inflate(R.layout.layout_category_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(items[position].image).into(holder.categoryImage)
        holder.categoryName.text = items[position].name
        holder.setListener(object: IRecyclerItemClickListener{
            override fun onItemClick(view: View, pos: Int){
                Common.categorySelected = items[pos]
                EventBus.getDefault().postSticky(CategoryClick(true))
            }
        })
    }

}