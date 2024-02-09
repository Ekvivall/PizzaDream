package com.sokol.pizzadream.ui.viewOrders

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.view.animation.LayoutAnimationController
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sokol.pizzadream.Adapter.OrderAdapter
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.EventBus.MenuClick
import com.sokol.pizzadream.R
import org.greenrobot.eventbus.EventBus
import java.util.Collections

class ViewOrdersFragment : Fragment() {
    private lateinit var viewOrderViewModel: ViewOrdersViewModel
    private lateinit var layoutEmptyOrders: LinearLayout
    private lateinit var orderRecycler: RecyclerView
    private lateinit var btnGoToMenu: Button
    private lateinit var layoutAnimatorController: LayoutAnimationController
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewOrderViewModel = ViewModelProvider(this).get(ViewOrdersViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_view_orders, container, false)
        initView(root)
        viewOrderViewModel.orders.observe(viewLifecycleOwner, Observer {
            if (it == null || it.isEmpty()) {
                orderRecycler.visibility = View.GONE
                layoutEmptyOrders.visibility = View.VISIBLE
            } else {
                orderRecycler.visibility = View.VISIBLE
                layoutEmptyOrders.visibility = View.GONE
                val adapter = OrderAdapter(it, requireContext())
                orderRecycler.adapter = adapter
                orderRecycler.layoutAnimation = layoutAnimatorController
                /*if (orderRecycler.adapter == null) {
                    adapter = FavoriteAdapter(it, requireContext())
                    recyclerCart.adapter = adapter
                    recyclerCart.layoutManager?.onRestoreInstanceState(recyclerViewState)
                }*/
            }
        })
        return root
    }

    private fun initView(root: View) {
        layoutAnimatorController =
            AnimationUtils.loadLayoutAnimation(context, R.anim.layout_item_from_left)
        layoutEmptyOrders = root.findViewById(R.id.empty_orders)
        orderRecycler = root.findViewById(R.id.orders_recycler)
        orderRecycler.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(requireContext())
        orderRecycler.layoutManager = layoutManager
        btnGoToMenu = root.findViewById(R.id.btn_go_to_menu)
        btnGoToMenu.setOnClickListener {
            if (Common.isConnectedToInternet(requireContext())) {
                EventBus.getDefault().postSticky(MenuClick(true))
            } else {
                Toast.makeText(
                    requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
                ).show()
            }
        }
    }
}