package com.sokol.pizzadream.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sokol.pizzadream.Model.AddressModel
import com.sokol.pizzadream.R

class AddressAdapter(private val items: List<AddressModel>, private val context: Context) :
    RecyclerView.Adapter<AddressAdapter.MyViewHolder>() {

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var textAddress: TextView = view.findViewById(R.id.text_address)
        var textWorkingHours: TextView = view.findViewById(R.id.text_working_hours)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_address_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val addressItem = items[position]
        holder.textAddress.text = addressItem.name
        holder.textWorkingHours.text =
            StringBuilder("Час роботи: ").append(addressItem.scheduleWork)
    }
}