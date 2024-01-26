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
import com.sokol.pizzadream.Model.NewsModel
import com.sokol.pizzadream.R

class NewsAdapter(val items: List<NewsModel>, val context: Context) :
    RecyclerView.Adapter<NewsAdapter.MyViewHolder>() {
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var image: ImageView = view.findViewById(R.id.news_image)
        var title: TextView = view.findViewById(R.id.news_title)
        var content: TextView = view.findViewById(R.id.news_content)
        var date: TextView = view.findViewById(R.id.news_date)
        private var listener: IRecyclerItemClickListener? = null

        init {
            itemView.setOnClickListener(this)
        }

        fun setListener(listener: IRecyclerItemClickListener) {
            this.listener = listener
        }

        override fun onClick(view: View) {
            listener?.onItemClick(view, adapterPosition)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_news_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val newsItem = items[position]
        Glide.with(context).load(newsItem.image).into(holder.image)
        holder.title.text = newsItem.title
        holder.content.text = newsItem.content
        holder.date.text = newsItem.date
        holder.setListener(object : IRecyclerItemClickListener {
            override fun onItemClick(view: View, pos: Int) {
                // Обробка події кліку для новини
                TODO("Not yet implemented")
            }

        })
    }
}