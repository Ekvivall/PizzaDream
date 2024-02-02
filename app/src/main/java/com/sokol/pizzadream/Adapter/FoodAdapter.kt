package com.sokol.pizzadream.Adapter

import android.content.Context
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sokol.pizzadream.Callback.IRecyclerItemClickListener
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Database.Entities.CartItem
import com.sokol.pizzadream.Database.Entities.FavoriteItem
import com.sokol.pizzadream.Database.PizzaDatabase
import com.sokol.pizzadream.Database.Repositories.CartInterface
import com.sokol.pizzadream.Database.Repositories.CartRepository
import com.sokol.pizzadream.Database.Repositories.FavoriteInterface
import com.sokol.pizzadream.Database.Repositories.FavoriteRepository
import com.sokol.pizzadream.EventBus.FoodItemClick
import com.sokol.pizzadream.Model.FoodModel
import com.sokol.pizzadream.R
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus

class FoodAdapter(val items: List<FoodModel>, val context: Context) :
    RecyclerView.Adapter<FoodAdapter.MyViewHolder>() {
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private val cartInterface: CartInterface =
        CartRepository(PizzaDatabase.getInstance(context).getCartDAO())
    private val favoriteInterface: FavoriteInterface =
        FavoriteRepository(PizzaDatabase.getInstance(context).getFavoriteDAO())

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var foodName: TextView = view.findViewById(R.id.food_name)
        var foodImage: ImageView = view.findViewById(R.id.food_img)
        var foodDesc: TextView = view.findViewById(R.id.food_desc)
        var radioGroupSize: RadioGroup = view.findViewById(R.id.radio_group_size)

        var foodPrice: TextView = view.findViewById(R.id.food_price)
        var foodCart: Button = view.findViewById(R.id.btn_add_to_cart)
        var favImage: ImageView = view.findViewById(R.id.food_fav)
        private var listener: IRecyclerItemClickListener? = null
        fun setListener(listener: IRecyclerItemClickListener) {
            this.listener = listener
            itemView.setOnClickListener(this)
        }

        override fun onClick(p0: View?) {
            listener!!.onItemClick(p0!!, adapterPosition)
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_product_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(items[position].image).into(holder.foodImage)
        holder.foodName.text = items[position].name
        holder.foodDesc.text =
            Html.fromHtml(items[position].description, Html.FROM_HTML_MODE_LEGACY)
        holder.radioGroupSize.removeAllViews()
        for (sizeModel in items[position].size) {
            val radioButton = RadioButton(context)
            radioButton.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    items[position].userSelectedSize = sizeModel
                }
                var totalPrice = items[position].userSelectedSize?.price?.toDouble()
                var displayPrice = Math.round(totalPrice!! * 100.0) / 100.0
                holder.foodPrice.text =
                    StringBuilder("").append(Common.formatPrice(displayPrice)).toString()
            }
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
            radioButton.layoutParams = params
            radioButton.text = sizeModel.name
            radioButton.tag = sizeModel.price
            holder.radioGroupSize.addView(radioButton)
        }
        if (holder.radioGroupSize.childCount > 0) {
            val radioButton = holder.radioGroupSize.getChildAt(0) as RadioButton
            radioButton.isChecked = true
        }
        holder.setListener(object : IRecyclerItemClickListener {
            override fun onItemClick(view: View, pos: Int) {
                Common.foodSelected = items[pos]
                EventBus.getDefault().postSticky(FoodItemClick(true, items[pos]))
            }
        })
        // Перевірка, чи елемент вже є в обраному
        favoriteInterface.isFavorite(
            items[position].id.toString(),
            Common.currentUser?.uid.toString()
        ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Int> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onError(e: Throwable) {
                    Toast.makeText(context, "" + e.message!!, Toast.LENGTH_SHORT).show()
                }

                override fun onSuccess(t: Int) {
                    if (t > 0) {
                        // Встановлення обраної іконки
                        holder.favImage.setImageResource(R.drawable.ic_favorite_24)
                    } else {
                        holder.favImage.setImageResource(R.drawable.ic_favorite_border_24)
                    }
                }

            })
        // Встановлення слухача кліків для іконки Favorite
        holder.favImage.setOnClickListener {
            favoriteInterface.isFavorite(
                items[position].id.toString(),
                Common.currentUser?.uid.toString()
            ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<Int> {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, "" + e.message!!, Toast.LENGTH_SHORT).show()
                    }

                    override fun onSuccess(t: Int) {
                        if (t > 0) {
                            // Видалення елемента з обраного
                            compositeDisposable.add(
                                favoriteInterface.removeFromFavorites(
                                    items[position].id.toString(),
                                    Common.currentUser?.uid.toString()
                                ).subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread()).subscribe({
                                        Toast.makeText(
                                            context,
                                            items[position].name + " видалено з обраних",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        holder.favImage.setImageResource(R.drawable.ic_favorite_border_24)
                                    }, { err: Throwable? ->
                                        Toast.makeText(
                                            context,
                                            "Помилка видалення товару з обраного" + err!!.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    })
                            )
                        } else {
                            // Додавання елемента до обраного
                            val favorite = FavoriteItem()
                            favorite.foodId = items[position].id.toString()
                            favorite.uid = Common.currentUser?.uid.toString()
                            favorite.foodName = items[position].name
                            favorite.foodImage = items[position].image
                            favorite.foodPrice = items[position].size[0].price.toDouble()
                            compositeDisposable.add(
                                favoriteInterface.addToFavorites(favorite)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread()).subscribe({
                                        Toast.makeText(
                                            context,
                                            items[position].name + " додано до обраного",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        holder.favImage.setImageResource(R.drawable.ic_favorite_24)
                                    }, { err: Throwable? ->
                                        Toast.makeText(
                                            context,
                                            "Помилка додавання товару до обраного" + err!!.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    })
                            )
                        }
                    }

                })
        }

        holder.foodCart.setOnClickListener {
            val cartItem = CartItem()
            cartItem.uid = Common.currentUser?.uid.toString()
            cartItem.userEmail = Common.currentUser?.email
            cartItem.foodId = items[position].id.toString()
            cartItem.foodName = items[position].name
            cartItem.foodImage = items[position].image
            cartItem.foodPrice = items[position].userSelectedSize?.price?.toDouble()!!
            cartItem.foodQuantity = 1
            //cartItem.foodExtraPrice = 0.0
            cartItem.foodAddon = ""
            cartItem.foodSize = items[position].userSelectedSize?.name.toString()
            cartInterface.getItemWithAllOptionsInCart(
                cartItem.foodId, cartItem.uid, cartItem.foodSize, cartItem.foodAddon
            ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<CartItem> {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onError(e: Throwable) {
                        if (e.message?.contains("empty") == true) {
                            compositeDisposable.add(
                                cartInterface.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread()).subscribe({
                                        Toast.makeText(
                                            context,
                                            cartItem.foodName + " додано до кошика",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }, { t: Throwable? ->
                                        Toast.makeText(
                                            context,
                                            "Помилка додавання товару до кошика" + t!!.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    })
                            )
                        } else Toast.makeText(
                            context,
                            "Помилка додавання товару в кошик" + e.message,
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                    override fun onSuccess(t: CartItem) {
                        if (t.equals(cartItem)) {
                            //t.foodExtraPrice = cartItem.foodExtraPrice
                            t.foodAddon = cartItem.foodAddon
                            t.foodSize = cartItem.foodSize
                            t.foodQuantity += cartItem.foodQuantity
                            cartInterface.updateCart(t).subscribeOn(Schedulers.io())
                                .observeOn(AndroidSchedulers.mainThread())
                                .subscribe(object : SingleObserver<Int> {
                                    override fun onSubscribe(d: Disposable) {

                                    }

                                    override fun onError(e: Throwable) {
                                        Toast.makeText(
                                            context,
                                            "Помилка при оноленні кошика",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                    override fun onSuccess(t: Int) {
                                        Toast.makeText(
                                            context,
                                            cartItem.foodName + " додано до кошика",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                                })
                        } else {
                            compositeDisposable.add(
                                cartInterface.insertOrReplaceAll(cartItem)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread()).subscribe({
                                        Toast.makeText(
                                            context,
                                            cartItem.foodName + " додано до кошика",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }, { t: Throwable? ->
                                        Toast.makeText(
                                            context,
                                            "Помилка додавання товару до кошика" + t!!.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    })
                            )
                        }
                    }

                })
        }
    }

    fun onStop() {
        if (compositeDisposable != null) compositeDisposable.clear()
    }
}