package com.sokol.pizzadream.ui.cart

import android.os.Bundle
import android.os.Parcelable
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.sokol.pizzadream.Adapter.CartAdapter
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Database.CartDatabase
import com.sokol.pizzadream.Database.Repositories.CartInterface
import com.sokol.pizzadream.Database.Repositories.CartRepository
import com.sokol.pizzadream.EventBus.CountCartEvent
import com.sokol.pizzadream.EventBus.HideFABCart
import com.sokol.pizzadream.EventBus.MenuClick
import com.sokol.pizzadream.EventBus.PlaceOrderClick
import com.sokol.pizzadream.EventBus.RemoveItemsInCart
import com.sokol.pizzadream.EventBus.UpdateItemsInCart
import com.sokol.pizzadream.R
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class CartFragment : Fragment() {
    private lateinit var layoutEmptyCart: LinearLayout
    private lateinit var totalPrice: TextView
    private lateinit var recyclerCart: RecyclerView
    private lateinit var btnGoToMenu: Button
    private lateinit var btnOrder: Button
    private lateinit var cart: CartInterface
    private var compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var recyclerViewState: Parcelable? = null
    private lateinit var cartViewModel: CartViewModel
    private var adapter: CartAdapter? = null
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        EventBus.getDefault().postSticky(HideFABCart(true))
        cartViewModel = ViewModelProvider(this).get(CartViewModel::class.java)
        cartViewModel.initCartInterface(requireContext())
        val root: View = inflater.inflate(R.layout.fragment_cart, container, false)
        initView(root)
        cartViewModel.getMutableLiveDataCartItem().observe(viewLifecycleOwner, Observer {
            if (it == null || it.isEmpty()) {
                recyclerCart.visibility = View.GONE
                btnOrder.isEnabled = false
                layoutEmptyCart.visibility = View.VISIBLE
            } else {
                recyclerCart.visibility = View.VISIBLE
                btnOrder.isEnabled = true
                layoutEmptyCart.visibility = View.GONE
                if (recyclerCart.adapter == null) {
                    adapter = CartAdapter(it, requireContext())
                    recyclerCart.adapter = adapter
                    recyclerCart.layoutManager?.onRestoreInstanceState(recyclerViewState)
                }
            }
        })
        return root
    }

    override fun onResume() {
        super.onResume()
        calculateTotalPrice()
    }

    private fun initView(root: View) {
        setHasOptionsMenu(true)
        cart = CartRepository(CartDatabase.getInstance(requireContext()).getCartDAO())
        recyclerCart = root.findViewById(R.id.recycler_cart)
        recyclerCart.setHasFixedSize(true)
        val layoutManager = LinearLayoutManager(context)
        recyclerCart.layoutManager = layoutManager
        recyclerCart.addItemDecoration(DividerItemDecoration(context, layoutManager.orientation))
        layoutEmptyCart = root.findViewById(R.id.empty_cart)
        totalPrice = root.findViewById(R.id.txt_total_price)
        btnOrder = root.findViewById(R.id.btn_order)
        btnGoToMenu = root.findViewById(R.id.btn_go_to_menu)
        btnGoToMenu.setOnClickListener {
            EventBus.getDefault().postSticky(MenuClick(true))
        }
        btnOrder.setOnClickListener {
            EventBus.getDefault().postSticky(PlaceOrderClick(true))
        }
    }

    override fun onStop() {
        super.onStop()
        cartViewModel.onStop()
        compositeDisposable.clear()
        if (EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().unregister(this)
        EventBus.getDefault().postSticky(HideFABCart(false))
        EventBus.getDefault().unregister(this)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onUpdateItemInCart(event: UpdateItemsInCart) {
        recyclerViewState = recyclerCart.layoutManager?.onSaveInstanceState()
        cart.updateCart(event.cartItem)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Int> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onError(e: Throwable) {
                    Toast.makeText(context, "Оновлення кошика " + e.message, Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onSuccess(t: Int) {
                    calculateTotalPrice()
                }

            })
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onRemoveItemInCart(event: RemoveItemsInCart) {
        //recyclerViewState = recyclerCart.layoutManager?.onSaveInstanceState()
        //adapter?.notifyItemRemoved(event.position)
        /*adapter = CartAdapter(adapter!!.items, requireContext())
        recyclerCart.adapter = adapter
        recyclerCart.layoutManager?.onRestoreInstanceState(recyclerViewState)*/
        //recyclerCart.adapter?.notifyDataSetChanged()
        calculateTotalPrice()
    }

    private fun calculateTotalPrice() {
        cart.sumPrice(Common.currentUser!!.uid!!)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Double> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onError(e: Throwable) {
                    if (!e.message!!.contains("Query returned empty"))
                        Toast.makeText(context, "" + e.message!!, Toast.LENGTH_SHORT).show()
                    else
                        totalPrice.text = StringBuilder("Всього: ").append(Common.formatPrice(0.0))
                }

                override fun onSuccess(t: Double) {
                    totalPrice.text = StringBuilder("Всього: ").append(Common.formatPrice(t))
                }

            })
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this))
            EventBus.getDefault().register(this)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.cart_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.clear_cart) {
            cart.cleanCart(Common.currentUser?.uid.toString())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(object :SingleObserver<Int>{
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, "" + e.message, Toast.LENGTH_SHORT).show()
                    }

                    override fun onSuccess(t: Int) {
                        EventBus.getDefault().postSticky(CountCartEvent(true))
                        calculateTotalPrice()
                    }

                })
            return true
        }
        return super.onOptionsItemSelected(item)
    }
}