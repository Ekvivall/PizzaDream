package com.sokol.pizzadream.ui.cart

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Database.PizzaDatabase
import com.sokol.pizzadream.Database.Entities.CartItem
import com.sokol.pizzadream.Database.Repositories.CartInterface
import com.sokol.pizzadream.Database.Repositories.CartRepository
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class CartViewModel : ViewModel() {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var cart: CartInterface? = null
    private var mutableLiveDataCartItem: MutableLiveData<List<CartItem>>? = null
    private fun getCartItems() {
        compositeDisposable.addAll(
            cart!!.getAllCart(Common.currentUser!!.uid!!).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ cartItems ->
                    mutableLiveDataCartItem!!.value = cartItems
                }, { t: Throwable? -> mutableLiveDataCartItem!!.value = null })
        )
    }

    fun getMutableLiveDataCartItem(): MutableLiveData<List<CartItem>> {
        if (mutableLiveDataCartItem == null) {
            mutableLiveDataCartItem = MutableLiveData()
        }
        getCartItems()
        return mutableLiveDataCartItem!!
    }

    fun initCartInterface(context: Context) {
        cart = CartRepository(PizzaDatabase.getInstance(context).getCartDAO())
    }

    fun onStop() {
        compositeDisposable.clear()
    }
}