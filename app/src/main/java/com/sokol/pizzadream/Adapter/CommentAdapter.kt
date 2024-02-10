package com.sokol.pizzadream.Adapter

import android.content.Context
import android.text.format.DateUtils
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RatingBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sokol.pizzadream.Model.CommentModel
import com.sokol.pizzadream.R

class CommentAdapter(val items: List<CommentModel>, val context: Context) :
    RecyclerView.Adapter<CommentAdapter.MyViewHolder>() {
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var commentImage: ImageView = view.findViewById(R.id.comment_image)
        var commentName: TextView = view.findViewById(R.id.comment_name)
        var commentDate: TextView = view.findViewById(R.id.comment_date)
        var commentText: TextView = view.findViewById(R.id.comment_text)
        var ratingBar: RatingBar = view.findViewById(R.id.rating_bar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_comment_item, parent, false)
        return MyViewHolder(view)
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val comment = items[position]
        if (comment.avatar.isNotEmpty()) {
            Glide.with(context).load(comment.avatar).into(holder.commentImage)
        }
        holder.commentName.text = comment.name
        holder.commentDate.text = DateUtils.getRelativeTimeSpanString(comment.commentTimeStamp)
        holder.commentText.text = comment.comment
        holder.ratingBar.rating = comment.ratingValue.toFloat()
    }

    override fun getItemCount(): Int {
        return items.size
    }

}