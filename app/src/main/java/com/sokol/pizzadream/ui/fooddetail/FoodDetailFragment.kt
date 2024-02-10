package com.sokol.pizzadream.ui.fooddetail

import android.os.Bundle
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
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.gson.Gson
import com.sokol.pizzadream.Adapter.AddonAdapter
import com.sokol.pizzadream.Adapter.AddonCategoryAdapter
import com.sokol.pizzadream.Adapter.UserAddonAdapter
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Common.SpaceItemDecoration
import com.sokol.pizzadream.Database.Entities.CartItem
import com.sokol.pizzadream.Database.Entities.FavoriteItem
import com.sokol.pizzadream.Database.PizzaDatabase
import com.sokol.pizzadream.Database.Repositories.CartInterface
import com.sokol.pizzadream.Database.Repositories.CartRepository
import com.sokol.pizzadream.Database.Repositories.FavoriteInterface
import com.sokol.pizzadream.Database.Repositories.FavoriteRepository
import com.sokol.pizzadream.EventBus.AddonCategoryClick
import com.sokol.pizzadream.EventBus.AddonClick
import com.sokol.pizzadream.EventBus.CommentsClick
import com.sokol.pizzadream.EventBus.FoodItemClick
import com.sokol.pizzadream.EventBus.UserAddonCountUpdate
import com.sokol.pizzadream.Model.AddonModel
import com.sokol.pizzadream.Model.FoodModel
import com.sokol.pizzadream.R
import io.reactivex.SingleObserver
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

class FoodDetailFragment : Fragment() {
    private lateinit var categoryRecycler: RecyclerView
    private lateinit var addonRecycler: RecyclerView
    private lateinit var userAddonRecycler: RecyclerView
    private lateinit var foodName: TextView
    private lateinit var foodImgLayout: ConstraintLayout
    private lateinit var btnCart: Button
    private lateinit var foodDesc: TextView
    private lateinit var foodPrice: TextView
    private lateinit var btnDecrease: ImageView
    private lateinit var foodQuantity: TextView
    private lateinit var btnIncrease: ImageView
    private lateinit var ratingBar: RatingBar
    private lateinit var btnShowComment: Button
    private lateinit var radioGroupSize: RadioGroup
    private lateinit var cart: CartInterface
    private val compositeDisposable = CompositeDisposable()
    private lateinit var favImage: ImageView
    private lateinit var favorite: FavoriteInterface
    private lateinit var rating: TextView
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        val foodDetailViewModel = ViewModelProvider(this).get(FoodDetailViewModel::class.java)
        val root = inflater.inflate(R.layout.fragment_food_detail, container, false)
        initView(root)
        val actionBar = (activity as AppCompatActivity).supportActionBar
        foodDetailViewModel.getFoodDetailMutableLiveData().observe(viewLifecycleOwner) {
            displayInfo(it)
            actionBar?.title = it.name ?: getString(R.string.menu_food_detail)
        }
        if (Common.isConnectedToInternet(requireContext())) {
            foodDetailViewModel.categoryList.observe(viewLifecycleOwner, Observer {
                val listData = it
                if (it.isEmpty()) {
                    root.findViewById<View>(R.id.view_before_addon).visibility = View.GONE
                    root.findViewById<TextView>(R.id.addon_text).visibility = View.GONE
                } else {
                    val adapter = AddonCategoryAdapter(listData, requireContext())
                    categoryRecycler.adapter = adapter
                    Common.addonCategorySelected = it[0]
                    Common.foodSelected!!.userSelectedAddon = ArrayList()
                    EventBus.getDefault().postSticky(AddonCategoryClick(true, it[0]))
                }
            })
        } else {
            Toast.makeText(
                requireContext(), "Будь ласка, перевірте своє з'єднання!", Toast.LENGTH_SHORT
            ).show()
        }
        return root
    }

    private fun displayInfo(it: FoodModel) {
        //drawableList.add(it.image)
        //Glide.with(requireContext()).load(it.image).into(foodImg)
        val foodImg = ImageView(requireContext())
        foodImg.id = View.generateViewId()
        foodImg.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        Glide.with(requireContext()).load(it.image).into(foodImg)
        foodImgLayout.addView(foodImg)
        foodName.text = it.name!!
        foodDesc.text = Html.fromHtml(it.description!!, Html.FROM_HTML_MODE_LEGACY)
        val ratingAverage = it.ratingSum.toFloat() / it.ratingCount
        ratingBar.rating = ratingAverage
        rating.text = if (it.ratingCount == 0L) "0" else String.format("%.1f", ratingAverage)
        for (sizeModel in it.size) {
            val radioButton = RadioButton(context)
            radioButton.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    Common.foodSelected?.userSelectedSize = sizeModel
                }
                calculateTotalPrice()
            }
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
            radioButton.layoutParams = params
            radioButton.text = sizeModel.name
            radioButton.tag = sizeModel.price
            radioGroupSize.addView(radioButton)
        }
        if (radioGroupSize.childCount > 0) {
            val radioButton = radioGroupSize.getChildAt(0) as RadioButton
            radioButton.isChecked = true
        }
    }

    private fun initView(root: View) {
        cart = CartRepository(PizzaDatabase.getInstance(requireContext()).getCartDAO())
        favorite = FavoriteRepository(PizzaDatabase.getInstance(requireContext()).getFavoriteDAO())
        categoryRecycler = root.findViewById(R.id.addon_category_recycler)
        categoryRecycler.setHasFixedSize(true)
        categoryRecycler.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        addonRecycler = root.findViewById(R.id.addon_recycler)
        addonRecycler.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(context, 2)
        layoutManager.orientation = RecyclerView.VERTICAL
        addonRecycler.layoutManager = layoutManager
        addonRecycler.addItemDecoration(SpaceItemDecoration(8))
        userAddonRecycler = root.findViewById(R.id.user_addon_recycler)
        userAddonRecycler.setHasFixedSize(true)
        userAddonRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        btnCart = root.findViewById(R.id.btn_add_to_cart)
        foodImgLayout = root.findViewById(R.id.food_img)
        foodName = root.findViewById(R.id.food_name)
        foodDesc = root.findViewById(R.id.food_desc)
        foodPrice = root.findViewById(R.id.food_price)
        btnDecrease = root.findViewById(R.id.img_decrease)
        foodQuantity = root.findViewById(R.id.food_quantity)
        btnIncrease = root.findViewById(R.id.img_increase)
        ratingBar = root.findViewById(R.id.ratingBar)
        btnShowComment = root.findViewById(R.id.btnShowComment)
        btnShowComment.setOnClickListener {
            EventBus.getDefault().postSticky(CommentsClick(true))
        }
        radioGroupSize = root.findViewById(R.id.radio_group_size)
        favImage = root.findViewById(R.id.food_fav)
        rating = root.findViewById(R.id.rating)
        btnDecrease.setOnClickListener {
            val quantity = foodQuantity.text.toString().toInt()
            if (quantity > 1) {
                foodQuantity.text = (quantity - 1).toString()
                calculateTotalPrice()
            }
        }
        btnIncrease.setOnClickListener {
            val quantity = foodQuantity.text.toString().toInt()
            foodQuantity.text = (quantity + 1).toString()
            calculateTotalPrice()
        }
        // Перевірка, чи елемент вже є в обраному
        favorite.isFavorite(Common.foodSelected?.id.toString(), Common.currentUser?.uid.toString())
            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
            .subscribe(object : SingleObserver<Int> {
                override fun onSubscribe(d: Disposable) {
                }

                override fun onError(e: Throwable) {
                    Toast.makeText(requireContext(), "" + e.message!!, Toast.LENGTH_SHORT).show()
                }

                override fun onSuccess(t: Int) {
                    if (t > 0) {
                        // Встановлення обраної іконки
                        favImage.setImageResource(R.drawable.ic_favorite_24)
                    }
                }

            })
        // Встановлення слухача кліків для іконки Favorite
        favImage.setOnClickListener {
            favorite.isFavorite(
                Common.foodSelected?.id.toString(), Common.currentUser?.uid.toString()
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
                                favorite.removeFromFavorites(
                                    Common.foodSelected?.id.toString(),
                                    Common.currentUser?.uid.toString()
                                ).subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread()).subscribe({
                                        Toast.makeText(
                                            requireContext(),
                                            Common.foodSelected?.name + " видалено з обраних",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        favImage.setImageResource(R.drawable.ic_favorite_border_24)
                                    }, { err: Throwable? ->
                                        Toast.makeText(
                                            requireContext(),
                                            "Помилка видалення товару з обраного" + err!!.message,
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    })
                            )
                        } else {
                            // Додавання елемента до обраного
                            val fav = FavoriteItem()
                            fav.foodId = Common.foodSelected?.id.toString()
                            fav.uid = Common.currentUser?.uid.toString()
                            fav.foodName = Common.foodSelected?.name
                            fav.foodImage = Common.foodSelected?.image
                            fav.foodPrice = Common.foodSelected?.size?.get(0)?.price!!.toDouble()
                            fav.categoryId = Common.foodSelected?.categoryId.toString()
                            compositeDisposable.add(
                                favorite.addToFavorites(fav).subscribeOn(Schedulers.io())
                                    .observeOn(AndroidSchedulers.mainThread()).subscribe({
                                        Toast.makeText(
                                            requireContext(),
                                            Common.foodSelected?.name + " додано до обраного",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        favImage.setImageResource(R.drawable.ic_favorite_24)
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
        btnCart.setOnClickListener {
            val cartItem = CartItem()
            cartItem.uid = Common.currentUser?.uid.toString()
            cartItem.userEmail = Common.currentUser?.email
            cartItem.foodId = Common.foodSelected?.id.toString()
            cartItem.foodName = Common.foodSelected?.name
            cartItem.foodImage = Common.foodSelected?.image
            cartItem.foodPrice = totalPrice
            cartItem.foodQuantity = foodQuantity.text.toString().toInt()
            cartItem.categoryId = Common.foodSelected?.categoryId.toString()
            if (Common.foodSelected!!.userSelectedAddon != null) {/*for (foodAddon in Common.foodSelected!!.userSelectedAddon!!) {
                    if (foodAddon != Common.foodSelected!!.userSelectedAddon!![0]) cartItem.foodAddon += ", "
                    cartItem.foodAddon += foodAddon.name + " x" + foodAddon.userCount
                }*/
                cartItem.foodAddon = Gson().toJson(Common.foodSelected!!.userSelectedAddon)
            } else {
                cartItem.foodAddon = ""
            }
            cartItem.foodSize = Common.foodSelected?.userSelectedSize?.name.toString()
            cart.getItemWithAllOptionsInCart(
                cartItem.foodId, cartItem.uid, cartItem.foodSize, cartItem.foodAddon
            ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(object : SingleObserver<CartItem> {
                    override fun onSubscribe(d: Disposable) {
                    }

                    override fun onError(e: Throwable) {
                        if (e.message?.contains("empty") == true) {
                            compositeDisposable.add(
                                cart.insertOrReplaceAll(cartItem).subscribeOn(Schedulers.io())
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
                            cart.updateCart(t).subscribeOn(Schedulers.io())
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
                                cart.insertOrReplaceAll(cartItem).subscribeOn(Schedulers.io())
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

    var totalPrice = 0.0
    private fun calculateTotalPrice() {
        totalPrice = Common.foodSelected?.userSelectedSize?.price?.toDouble()!!
        if (Common.foodSelected!!.userSelectedAddon != null && Common.foodSelected!!.userSelectedAddon!!.size > 0) {
            for (addonModel in Common.foodSelected!!.userSelectedAddon!!) {
                totalPrice = totalPrice.plus(addonModel.price.toDouble() * addonModel.userCount)
            }
        }
        var displayPrice = totalPrice * foodQuantity.text.toString().toInt()
        displayPrice = Math.round(displayPrice * 100.0) / 100.0
        foodPrice.text = StringBuilder("").append(Common.formatPrice(displayPrice)).toString()
    }

    /*private fun addImageIngredient() {
        for (viewToRemove in viewsIngredients) {
            foodImgLayout.removeView(viewToRemove)
        }
        if (Common.foodSelected!!.userSelectedAddon != null && Common.foodSelected!!.userSelectedAddon!!.size > 0) {
            for (addonModel in Common.foodSelected!!.userSelectedAddon!!) {
                CoroutineScope(Dispatchers.Main).launch {
                    val foodImg = ImageView(requireContext())
                    foodImg.id = View.generateViewId()
                    foodImg.layoutParams = ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT
                    )
                    val imageBitmap = withContext(Dispatchers.IO) {
                        Glide.with(requireContext())
                            .asBitmap()
                            .load(addonModel.imageFill)
                            .submit()
                            .get()
                    }
                    foodImg.setImageBitmap(imageBitmap)
                    viewsIngredients.add(foodImg)
                    foodImgLayout.addView(foodImg)
                }
            }
        }
    }*/
    private fun addImageIngredient(addonModel: AddonModel, pos: Int) {
        CoroutineScope(Dispatchers.Main).launch {
            val foodImg = ImageView(requireContext())
            foodImg.id = View.generateViewId()
            foodImg.layoutParams = ViewGroup.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
            )
            val imageBitmap = withContext(Dispatchers.IO) {
                Glide.with(requireContext()).asBitmap().load(addonModel.imageFill).submit().get()
            }
            foodImg.setImageBitmap(imageBitmap)
            foodImgLayout.addView(foodImg, pos)
        }
    }

    private fun removeImageIngredient(pos: Int) {
        val viewToRemove = foodImgLayout.getChildAt(pos)
        foodImgLayout.removeView(viewToRemove)
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onAddonCategorySelected(event: AddonCategoryClick) {
        if (event.isSuccess) {
            addonRecycler.adapter =
                AddonAdapter(Common.addonCategorySelected!!.items, requireContext())
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onAddonSelected(event: AddonClick) {
        userAddonRecycler.adapter =
            UserAddonAdapter(Common.foodSelected?.userSelectedAddon!!, requireContext())
        if (Common.foodSelected?.id?.contains("constructor") == true) {
            if (event.isSuccess) {
                addImageIngredient(event.addon, event.pos)
            } else {
                removeImageIngredient(event.pos)
            }
        }
        calculateTotalPrice()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onUserAddonCountUpdate(event: UserAddonCountUpdate) {
        if (event.isSuccess) {
            addonRecycler.adapter?.notifyItemChanged(
                Common.addonCategorySelected!!.items.indexOf(event.addon)
            )
            if (Common.foodSelected?.id?.contains("constructor") == true) {
                removeImageIngredient(event.pos)
            }
        }
        calculateTotalPrice()
    }

    override fun onStart() {
        super.onStart()
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)
    }

    override fun onStop() {
        super.onStop()
        Common.foodSelected?.userSelectedAddon = null
        if (EventBus.getDefault().isRegistered(this)) EventBus.getDefault().unregister(this)
    }

    override fun onResume() {
        super.onResume()
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)
    }
}

