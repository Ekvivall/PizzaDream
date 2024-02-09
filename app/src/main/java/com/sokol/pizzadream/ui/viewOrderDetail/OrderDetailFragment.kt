package com.sokol.pizzadream.ui.viewOrderDetail

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sokol.pizzadream.Adapter.OrderFoodAdapter
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.EventBus.ViewAddCommentClick
import com.sokol.pizzadream.Model.OrderModel
import com.sokol.pizzadream.R
import org.greenrobot.eventbus.EventBus
import java.text.SimpleDateFormat
import java.util.Date

class OrderDetailFragment : Fragment() {
    private lateinit var orderId: TextView
    private lateinit var orderDate: TextView
    private var simpleDateFormat = SimpleDateFormat("dd MMM yyyy HH:mm")
    private lateinit var orderStatus: TextView
    private lateinit var customerName: TextView
    private lateinit var customerPhone: TextView
    private lateinit var customerEmail: TextView
    private lateinit var customerAddress: TextView
    private lateinit var orderFoodsRecycler: RecyclerView
    private lateinit var paymentType: TextView
    private lateinit var priceFood: TextView
    private lateinit var priceDelivery: TextView
    private lateinit var totalPrice: TextView
    private lateinit var btnAddComment: Button
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        val orderDetailViewModel = ViewModelProvider(this).get(OrderDetailViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_order_detail, container, false)
        initView(root)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        if (Common.isConnectedToInternet(requireContext())) {
            orderDetailViewModel.getOrderDetailMutableLiveData().observe(viewLifecycleOwner) {
                displayInfo(it)
                actionBar?.title = StringBuilder("Замовлення № ").append(it.orderId)
            }
        } else {
            Toast.makeText(
                requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
            ).show()
        }
        return root
    }

    private fun displayInfo(order: OrderModel) {
        orderId.text = order.orderId
        val date = Date(order.orderedTime)
        orderDate.text = StringBuilder(simpleDateFormat.format(date))
        orderStatus.text = order.status
        if (order.status == Common.STATUSES[4]) {
            orderStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.green))
        } else {
            orderStatus.setTextColor(ContextCompat.getColor(requireContext(), R.color.red))
        }
        customerName.text = order.customerName
        customerPhone.text = order.customerPhone
        customerEmail.text = order.customerEmail
        if (order.isDeliveryAddress) {
            customerAddress.text = StringBuilder(order.customerAddress.toString())
            priceFood.text = Common.formatPrice(order.totalPrice - 60)
            priceDelivery.text = Common.formatPrice(60.0)
        } else {
            customerAddress.text = StringBuilder("Pizza Dream: ").append(order.customerAddress)
            priceFood.text = Common.formatPrice(order.totalPrice)
            priceDelivery.text = Common.formatPrice(0.0)
        }
        val adapter = OrderFoodAdapter(
            order.cartItems!!, requireContext()
        )
        orderFoodsRecycler.adapter = adapter
        paymentType.text = when (order.transactionId) {
            "Оплата при отриманні" -> "При отриманні"
            else -> "Онлайн оплата"
        }
        totalPrice.text = Common.formatPrice(order.totalPrice)
        if (order.status == Common.STATUSES[4] && !order.isComment) btnAddComment.visibility =
            View.VISIBLE
        else btnAddComment.visibility = View.GONE
        btnAddComment.setOnClickListener {
            Common.orderSelected = order
            EventBus.getDefault().postSticky(ViewAddCommentClick(true))
        }
    }

    private fun initView(root: View) {
        orderId = root.findViewById(R.id.order_id)
        orderDate = root.findViewById(R.id.order_date)
        orderStatus = root.findViewById(R.id.order_status)
        customerName = root.findViewById(R.id.customer_name)
        customerPhone = root.findViewById(R.id.customer_phone)
        customerEmail = root.findViewById(R.id.customer_email)
        customerAddress = root.findViewById(R.id.customer_address)
        orderFoodsRecycler = root.findViewById(R.id.order_foods_recycler)
        orderFoodsRecycler.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        orderFoodsRecycler.layoutManager = layoutManager
        paymentType = root.findViewById(R.id.payment_type)
        priceFood = root.findViewById(R.id.price_food)
        priceDelivery = root.findViewById(R.id.price_delivery)
        totalPrice = root.findViewById(R.id.total_price)
        btnAddComment = root.findViewById(R.id.btn_add_comment)
    }
}