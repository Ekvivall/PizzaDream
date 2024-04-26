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
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sokol.pizzadream.Callback.IRecyclerItemClickListener
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Database.Entities.CartItemDB
import com.sokol.pizzadream.Database.Entities.FavoriteItemDB
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
        var ratingBar: RatingBar = view.findViewById(R.id.ratingBar)
        var rating: TextView = view.findViewById(R.id.rating)
        var creator: TextView = view.findViewById(R.id.food_creator)
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
        val food = items[position]
        Glide.with(context).load(food.image).into(holder.foodImage)
        if (food.createdUserName.isNullOrEmpty()) {
            holder.creator.visibility = View.GONE
        } else {
            holder.creator.visibility = View.VISIBLE
            holder.creator.text =
                StringBuilder("Створено користувачем: ").append(food.createdUserName)
        }
        holder.foodName.text = food.name
        holder.foodDesc.text = Html.fromHtml(food.description, Html.FROM_HTML_MODE_LEGACY)
        holder.radioGroupSize.removeAllViews()
        val ratingAverage = food.ratingSum.toFloat() / food.ratingCount
        holder.ratingBar.rating = ratingAverage
        holder.rating.text =
            if (food.ratingCount == 0L) "0" else String.format("%.1f", ratingAverage)
        for (sizeModel in food.size) {
            val radioButton = RadioButton(context)
            radioButton.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    food.userSelectedSize = sizeModel
                }
                val totalPrice = food.userSelectedSize?.price?.toDouble()
                val displayPrice = Math.round(totalPrice!! * 100.0) / 100.0
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
            food.id.toString(), Common.currentUser?.uid.toString()
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
                food.id.toString(), Common.currentUser?.uid.toString()
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
                                    food.id.toString(), Common.currentUser?.uid.toString()
                                ).subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread()).subscribe({
                                        Toast.makeText(
                                            context,
                                            food.name + " видалено з обраних",
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
                            val favorite = FavoriteItemDB()
                            favorite.foodId = food.id.toString()
                            favorite.uid = Common.currentUser?.uid.toString()
                            favorite.categoryId = food.categoryId.toString()
                            compositeDisposable.add(
                                favoriteInterface.addToFavorites(favorite)
                                    .subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread()).subscribe({
                                        Toast.makeText(
                                            context,
                                            food.name + " додано до обраного",
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
            val cartItem = CartItemDB()
            cartItem.uid = Common.currentUser?.uid.toString()
            cartItem.userEmail = Common.currentUser?.email
            cartItem.foodId = food.id.toString()
            cartItem.foodQuantity = 1
            //cartItem.foodExtraPrice = 0.0
            cartItem.foodAddon = ""
            cartItem.foodSize = food.userSelectedSize?.name.toString()
            cartItem.categoryId = food.categoryId.toString()
            cartInterface.getItemWithAllOptionsInCart(
                cartItem.foodId, cartItem.uid, cartItem.foodSize, cartItem.foodAddon
            ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<CartItemDB> {
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
                                            food.name + " додано до кошика",
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

                    override fun onSuccess(t: CartItemDB) {
                        if (t.equals(cartItem)) {
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
                                            food.name + " додано до кошика",
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
                                            food.name + " додано до кошика",
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
        compositeDisposable.clear()
    }
}