package com.sokol.pizzadream.ui.cart

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Database.PizzaDatabase
import com.sokol.pizzadream.Database.Repositories.CartInterface
import com.sokol.pizzadream.Database.Repositories.CartRepository
import com.sokol.pizzadream.Model.AddonModel
import com.sokol.pizzadream.Model.CartItem
import com.sokol.pizzadream.Model.FoodModel
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
                    val tempList = ArrayList<CartItem>()
                    var counter = 0
                    for (item in cartItems) {
                        val foodRef =
                            FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF)
                                .child(item.categoryId).child("foods").child(item.foodId)
                        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val model = snapshot.getValue(FoodModel::class.java)
                                if (model != null) {
                                    val cartItem = CartItem()
                                    cartItem.foodId = model.id.toString()
                                    cartItem.foodName = model.name
                                    cartItem.foodImage = model.image
                                    var totalPrice =
                                        model.size.find { it.name == item.foodSize }!!.price.toDouble()
                                    if (item.foodAddon.isNotEmpty()) {
                                        val addonModels: List<AddonModel> = Gson().fromJson(
                                            item.foodAddon,
                                            object : TypeToken<List<AddonModel>>() {}.type
                                        )
                                        totalPrice += addonModels.sumOf { x -> x.price.toDouble() * x.userCount }
                                    }
                                    cartItem.foodPrice = totalPrice
                                    cartItem.foodQuantity = item.foodQuantity
                                    cartItem.foodAddon = item.foodAddon
                                    cartItem.foodSize = item.foodSize
                                    cartItem.categoryId = model.categoryId.toString()
                                    cartItem.userEmail = item.userEmail
                                    cartItem.uid = item.uid
                                    cartItem.id = item.id
                                    tempList.add(cartItem)
                                    counter++
                                    if (counter == cartItems.size) {
                                        mutableLiveDataCartItem!!.value = tempList
                                    }
                                } else {
                                    cart!!.deleteCart(item).subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread()).subscribe()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }

                        })
                    }
                }, { t: Throwable? -> mutableLiveDataCartItem!!.value = null })
        )
    }

    fun getMutableLiveDataCartItem(): MutableLiveData<List<CartItem>> {
        mutableLiveDataCartItem = MutableLiveData()
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