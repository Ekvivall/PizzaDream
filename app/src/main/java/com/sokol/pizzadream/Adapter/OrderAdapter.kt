package com.sokol.pizzadream.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sokol.pizzadream.Callback.IRecyclerItemClickListener
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.EventBus.ViewAddCommentClick
import com.sokol.pizzadream.Model.OrderModel
import com.sokol.pizzadream.R
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.Date

class OrderAdapter(val items: List<OrderModel>, val context: Context) :
    RecyclerView.Adapter<OrderAdapter.MyViewHolder>() {
    private var simpleDateFormat = SimpleDateFormat("dd MMM yyyy HH:mm")

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var status: TextView = view.findViewById(R.id.order_status)
        var id: TextView = view.findViewById(R.id.order_id)
        var date: TextView = view.findViewById(R.id.order_date)
        var address: TextView = view.findViewById(R.id.customer_address)
        var totalPrice: TextView = view.findViewById(R.id.total_price)
        var recyclerView: RecyclerView = view.findViewById(R.id.order_foods_recycler)
        var btnAddComment: Button = view.findViewById(R.id.btn_add_comment)
        var delivery: TextView = view.findViewById(R.id.delivery)
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
            LayoutInflater.from(parent.context).inflate(R.layout.layout_order_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val orderItem = items[position]
        holder.recyclerView.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        holder.recyclerView.layoutManager = layoutManager
        val adapter = OrderFoodAdapter(
            orderItem.cartItems!!, context
        )
        if (orderItem.status == Common.STATUSES[4]) {
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.green))
        } else {
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.red))
        }
        holder.recyclerView.adapter = adapter
        val date = Date(orderItem.orderedTime)
        holder.date.text = StringBuilder(simpleDateFormat.format(date))
        holder.id.text = StringBuilder("№ ").append(orderItem.orderId)
        holder.status.text = StringBuilder("Статус: ").append(orderItem.status)
        holder.totalPrice.text =
            StringBuilder("Всього: ").append(Common.formatPrice(orderItem.totalPrice))
        if (orderItem.isDeliveryAddress) {
            holder.delivery.visibility = View.VISIBLE
            holder.address.text = StringBuilder(orderItem.customerAddress.toString())
        } else {
            holder.delivery.visibility = View.GONE
            holder.address.text = StringBuilder("Pizza Dream: ").append(orderItem.customerAddress)
        }
        holder.setListener(object : IRecyclerItemClickListener {
            override fun onItemClick(view: View, pos: Int) {
                //Common.newsSelected = items[pos]
                //EventBus.getDefault().postSticky(NewsItemClick(true, items[pos]))
            }

        })

        if (orderItem.status == Common.STATUSES[4] && !orderItem.isComment) holder.btnAddComment.visibility =
            View.VISIBLE
        else holder.btnAddComment.visibility = View.GONE
        holder.btnAddComment.setOnClickListener {
            Common.orderSelected = orderItem
            EventBus.getDefault().postSticky(ViewAddCommentClick(true))
        }
    }
}