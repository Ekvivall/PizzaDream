package com.sokol.pizzadream.Adapter

import android.content.Context
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.FirebaseDatabase
import com.sokol.pizzadream.Callback.IRecyclerItemClickListener
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Database.Entities.CartItemDB
import com.sokol.pizzadream.Database.PizzaDatabase
import com.sokol.pizzadream.Database.Repositories.CartInterface
import com.sokol.pizzadream.Database.Repositories.CartRepository
import com.sokol.pizzadream.EventBus.OrderDetailClick
import com.sokol.pizzadream.Model.OrderModel
import com.sokol.pizzadream.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.Date

class OrderAdapter(val items: List<OrderModel>, val context: Context) :
    RecyclerView.Adapter<OrderAdapter.MyViewHolder>() {
    private var simpleDateFormat = SimpleDateFormat("dd MMM yyyy HH:mm")
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var cart: CartInterface =
        CartRepository(PizzaDatabase.getInstance(context).getCartDAO())

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var status: TextView = view.findViewById(R.id.order_status)
        var id: TextView = view.findViewById(R.id.order_id)
        var date: TextView = view.findViewById(R.id.order_date)
        var address: TextView = view.findViewById(R.id.customer_address)
        var totalPrice: TextView = view.findViewById(R.id.total_price)
        var recyclerView: RecyclerView = view.findViewById(R.id.order_foods_recycler)
        var delivery: TextView = view.findViewById(R.id.delivery)
        var btnCancelOrder: Button = view.findViewById(R.id.btn_cancel_order)
        var btnAddToCart: Button = view.findViewById(R.id.btn_add_to_cart)
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
            orderItem.cartItems!!, context, orderItem
        )
        if (orderItem.status == Common.STATUSES[4]) {
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.green))
            holder.btnCancelOrder.visibility = View.GONE
            holder.btnAddToCart.visibility = View.VISIBLE
        } else {
            if (orderItem.status == Common.STATUSES[5]) {
                holder.btnCancelOrder.visibility = View.GONE
                holder.btnAddToCart.visibility = View.VISIBLE
            } else {
                holder.btnCancelOrder.visibility = View.VISIBLE
                holder.btnAddToCart.visibility = View.GONE
            }
            holder.status.setTextColor(ContextCompat.getColor(context, R.color.red))
        }
        holder.btnAddToCart.setOnClickListener {
            val cartItems = orderItem.cartItems!!.toTypedArray()
            val cartItemsDB = cartItems.map { CartItemDB(it) }.toTypedArray()
            compositeDisposable.add(
                cart.insertOrReplaceAll(*cartItemsDB).subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread()).subscribe{
                        Toast.makeText(
                            context,
                            "Успішно додано всі товари до кошика",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            )
        }
        holder.btnCancelOrder.setOnClickListener {
            val builder = androidx.appcompat.app.AlertDialog.Builder(
                context, R.style.CustomAlertDialog
            )
            builder.setTitle("Відмінити замовлення")
                .setMessage("Ви дійсно хочете відмінити замовлення?")
                .setNegativeButton("Відміна") { dialogInterface, _ -> dialogInterface.dismiss() }
                .setPositiveButton("Так") { dialogInterface, _ ->
                    val updateData = HashMap<String, Any>()
                    updateData["status"] = Common.STATUSES[5]
                    FirebaseDatabase.getInstance().getReference(Common.ORDER_REF)
                        .child(orderItem.orderId!!).updateChildren(updateData)
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                context, e.message, Toast.LENGTH_SHORT
                            ).show()
                        }.addOnSuccessListener {
                            orderItem.status = Common.STATUSES[5]
                            notifyItemChanged(position)
                        }
                }
            val dialog = builder.create()
            dialog.show()
            val positiveButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE)
            positiveButton.setTextColor(
                ContextCompat.getColor(
                    context, R.color.red
                )
            )
            val negativeButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE)
            negativeButton.setTextColor(
                ContextCompat.getColor(
                    context, R.color.black
                )
            )

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
                Common.orderSelected = items[pos]
                EventBus.getDefault().postSticky(OrderDetailClick(true))
            }

        })

    }

}