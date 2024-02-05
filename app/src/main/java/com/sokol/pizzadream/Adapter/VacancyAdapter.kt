package com.sokol.pizzadream.Adapter

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.EventBus.VacancyItemClick
import com.sokol.pizzadream.Model.VacancyModel
import com.sokol.pizzadream.R
import org.greenrobot.eventbus.EventBus

class VacancyAdapter(val items: List<VacancyModel>, val context: Context) :
    RecyclerView.Adapter<VacancyAdapter.MyViewHolder>() {
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var image: ImageView = view.findViewById(R.id.vacancy_image)
        var name: TextView = view.findViewById(R.id.vacancy_name)
        var shortDesc: TextView = view.findViewById(R.id.vacancy_short_desc)
        var btnDetails: Button = view.findViewById(R.id.btn_vacancy_details)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_vacancy_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val vacancyItem = items[position]
        Glide.with(context).load(vacancyItem.image).into(holder.image)
        holder.name.text = vacancyItem.name
        holder.shortDesc.text = Html.fromHtml(vacancyItem.shortDesc, Html.FROM_HTML_MODE_LEGACY)
        holder.btnDetails.setOnClickListener {
            Common.vacancySelected = vacancyItem
            EventBus.getDefault().postSticky(VacancyItemClick(true, vacancyItem))
        }
    }
}