package com.sokol.pizzadream.Adapter

import android.app.AlertDialog
import android.content.Context
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
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.sokol.pizzadream.Callback.IRecyclerItemClickListener
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Common.SpaceItemDecoration
import com.sokol.pizzadream.Database.Entities.CartItemDB
import com.sokol.pizzadream.Database.PizzaDatabase
import com.sokol.pizzadream.Database.Repositories.CartInterface
import com.sokol.pizzadream.Database.Repositories.CartRepository
import com.sokol.pizzadream.EventBus.AddonCategoryClick
import com.sokol.pizzadream.EventBus.AddonClick
import com.sokol.pizzadream.EventBus.RemoveItemsInCart
import com.sokol.pizzadream.EventBus.UpdateItemsInCart
import com.sokol.pizzadream.EventBus.UserAddonCountUpdate
import com.sokol.pizzadream.Model.AddonCategoryModel
import com.sokol.pizzadream.Model.AddonModel
import com.sokol.pizzadream.Model.CartItem
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


class CartAdapter(var items: List<CartItem>, val context: Context) :
    RecyclerView.Adapter<CartAdapter.MyViewHolder>() {
    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var foodImgLayout: ConstraintLayout = view.findViewById(R.id.img_cart_layout)

        //var foodImg: ImageView = view.findViewById(R.id.img_cart)
        var foodName: TextView = view.findViewById(R.id.txt_food_name_cart)
        var foodRemove: ImageView = view.findViewById(R.id.img_food_remove_cart)
        var foodSize: TextView = view.findViewById(R.id.txt_food_size_cart)
        var foodAddon: TextView = view.findViewById(R.id.txt_food_addon_cart)
        var foodIncrease: ImageView = view.findViewById(R.id.img_food_increase_cart)
        var foodQuantity: TextView = view.findViewById(R.id.txt_food_quantity_cart)
        var foodDecrease: ImageView = view.findViewById(R.id.img_food_decrease_cart)
        var foodPrice: TextView = view.findViewById(R.id.txt_food_price_cart)
        var foodAddonTitle: TextView = view.findViewById(R.id.txt_food_addon_cart_title)
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

    internal var compositeDisposable: CompositeDisposable = CompositeDisposable()
    internal var cart: CartInterface =
        CartRepository(PizzaDatabase.getInstance(context).getCartDAO())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val view =
            LayoutInflater.from(parent.context).inflate(R.layout.layout_cart_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    private lateinit var foodPrice: TextView
    private lateinit var addonRecycler: RecyclerView
    private lateinit var userAddonRecycler: RecyclerView
    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        if (!EventBus.getDefault().isRegistered(this)) EventBus.getDefault().register(this)
        val cartItem = items[position]
        holder.foodImgLayout.removeAllViews()
        val foodImg = ImageView(context)
        foodImg.id = View.generateViewId()
        foodImg.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT
        )
        Glide.with(context).load(cartItem.foodImage).into(foodImg)
        holder.foodImgLayout.addView(foodImg)
        holder.foodName.text = cartItem.foodName.toString()
        holder.foodSize.text = cartItem.foodSize
        val typeToken = object : TypeToken<List<AddonModel>>() {}.type
        val foodAddons = Gson().fromJson<List<AddonModel>>(cartItem.foodAddon, typeToken)
        var res = ""
        if (foodAddons != null) {
            for (foodAddon in foodAddons) {
                if (foodAddon != foodAddons[0]) res += ", "
                res += foodAddon.name + " x" + foodAddon.userCount
                if (cartItem.foodName.toString().contains("Конструктор")) {
                    CoroutineScope(Dispatchers.Main).launch {
                        val foodImg = ImageView(context)
                        foodImg.id = View.generateViewId()
                        foodImg.layoutParams = ViewGroup.LayoutParams(
                            ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT
                        )
                        val imageBitmap = withContext(Dispatchers.IO) {
                            Glide.with(context).asBitmap().load(foodAddon.imageFill).submit().get()
                        }
                        foodImg.setImageBitmap(imageBitmap)
                        holder.foodImgLayout.addView(foodImg)
                    }
                }
            }
        }
        holder.foodAddon.text = res
        if (cartItem.foodAddon == "") {
            holder.foodAddonTitle.visibility = View.GONE
        } else {
            holder.foodAddonTitle.visibility = View.VISIBLE
        }
        holder.foodQuantity.text = cartItem.foodQuantity.toString()
        holder.foodPrice.text =
            StringBuilder("").append(Common.formatPrice(cartItem.foodPrice * cartItem.foodQuantity))
                .toString()
        holder.foodIncrease.setOnClickListener {
            cartItem.foodQuantity++
            holder.foodQuantity.text = cartItem.foodQuantity.toString()
            holder.foodPrice.text =
                StringBuilder("").append(Common.formatPrice(cartItem.foodPrice * cartItem.foodQuantity))
                    .toString()
            EventBus.getDefault().postSticky(UpdateItemsInCart(CartItemDB(cartItem)))
        }
        holder.foodDecrease.setOnClickListener {
            if (cartItem.foodQuantity > 1) {
                cartItem.foodQuantity--
                holder.foodQuantity.text = cartItem.foodQuantity.toString()
                holder.foodPrice.text =
                    StringBuilder("").append(Common.formatPrice(cartItem.foodPrice * cartItem.foodQuantity))
                        .toString()
                EventBus.getDefault().postSticky(UpdateItemsInCart(CartItemDB(cartItem)))
            }
        }
        holder.foodRemove.setOnClickListener {
            notifyItemRemoved(position)
            items = items.filterIndexed { index, _ -> index != position }
            EventBus.getDefault().postSticky(RemoveItemsInCart(cartItem, items.isEmpty()))
        }
        holder.setListener(object : IRecyclerItemClickListener {
            override fun onItemClick(view: View, pos: Int) {
                FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF)
                    .child(cartItem.categoryId).child("foods").child(cartItem.foodId)
                    .addListenerForSingleValueEvent(object : ValueEventListener {
                        override fun onDataChange(snapshot: DataSnapshot) {
                            val foodModel = snapshot.getValue(FoodModel::class.java)!!
                            Common.foodSelected = foodModel
                            val addonCategoryList = ArrayList<AddonCategoryModel>()
                            FirebaseDatabase.getInstance().getReference(Common.ADDON_CATEGORY_REF)
                                .addListenerForSingleValueEvent(object : ValueEventListener {
                                    override fun onDataChange(snapshot: DataSnapshot) {
                                        for (itemSnapshot in snapshot.children) {
                                            if (itemSnapshot.key == foodModel.addon) {
                                                for (item in itemSnapshot.children) {
                                                    val model =
                                                        item.getValue(AddonCategoryModel::class.java)
                                                    addonCategoryList.add(model!!)
                                                }
                                                showUpdateDialog(
                                                    cartItem, addonCategoryList, position
                                                )
                                            }
                                        }
                                    }

                                    override fun onCancelled(error: DatabaseError) {
                                        Toast.makeText(context, error.message, Toast.LENGTH_SHORT)
                                            .show()
                                    }

                                })
                        }

                        override fun onCancelled(error: DatabaseError) {
                            Toast.makeText(context, error.message, Toast.LENGTH_SHORT).show()
                        }

                    })
            }

        })
    }

    private fun showUpdateDialog(
        cartItem: CartItem, addonCategoryList: ArrayList<AddonCategoryModel>, position: Int
    ) {
        val builder = AlertDialog.Builder(context)
        val itemView: View =
            LayoutInflater.from(context).inflate(R.layout.layout_dialog_update_cart, null)
        builder.setView(itemView)
        val btnOk = itemView.findViewById<Button>(R.id.btn_ok)
        val btnCancel = itemView.findViewById<Button>(R.id.btn_cancel)
        val foodName = itemView.findViewById<TextView>(R.id.food_name)
        foodName.text = cartItem.foodName
        foodPrice = itemView.findViewById(R.id.food_price)
        val radioGroupSize = itemView.findViewById<RadioGroup>(R.id.radio_group_size)
        val viewBeforeAddon = itemView.findViewById<View>(R.id.view_before_addon)
        val addonText = itemView.findViewById<TextView>(R.id.addon_text)
        userAddonRecycler = itemView.findViewById(R.id.user_addon_recycler)
        userAddonRecycler.setHasFixedSize(true)
        userAddonRecycler.layoutManager = LinearLayoutManager(context, RecyclerView.VERTICAL, false)
        val addonCategoryRecycler =
            itemView.findViewById<RecyclerView>(R.id.addon_category_recycler)
        addonCategoryRecycler.setHasFixedSize(true)
        addonCategoryRecycler.layoutManager =
            LinearLayoutManager(context, RecyclerView.HORIZONTAL, false)
        addonRecycler = itemView.findViewById(R.id.addon_recycler)
        addonRecycler.setHasFixedSize(true)
        val layoutManager = GridLayoutManager(context, 2)
        layoutManager.orientation = RecyclerView.VERTICAL
        addonRecycler.layoutManager = layoutManager
        addonRecycler.addItemDecoration(SpaceItemDecoration(1))
        if (addonCategoryList.isEmpty()) {
            viewBeforeAddon.visibility = View.GONE
            addonText.visibility = View.GONE
        } else {
            viewBeforeAddon.visibility = View.VISIBLE
            addonText.visibility = View.VISIBLE
            val adapter = AddonCategoryAdapter(addonCategoryList, context)
            addonCategoryRecycler.adapter = adapter
            if (cartItem.foodAddon.isNotEmpty()) {
                val addonModels: List<AddonModel> = Gson().fromJson(
                    cartItem.foodAddon, object : TypeToken<List<AddonModel>>() {}.type
                )
                Common.foodSelected!!.userSelectedAddon = addonModels.toMutableList()
                userAddonRecycler.adapter =
                    UserAddonAdapter(Common.foodSelected!!.userSelectedAddon!!, context)
            } else {
                Common.foodSelected!!.userSelectedAddon = ArrayList()
            }
            Common.addonCategorySelected = addonCategoryList[0]
            addonRecycler.adapter = AddonAdapter(Common.addonCategorySelected!!.items, context)
        }

        for (sizeModel in Common.foodSelected!!.size) {
            val radioButton = RadioButton(context)
            radioButton.setOnCheckedChangeListener { compoundButton, b ->
                if (b) {
                    Common.foodSelected!!.userSelectedSize = sizeModel
                }
                calculateTotalPrice()
            }
            val params = LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1.0f)
            radioButton.layoutParams = params
            radioButton.text = sizeModel.name
            radioButton.tag = sizeModel.price
            radioGroupSize.addView(radioButton)
            if (sizeModel.name == cartItem.foodSize) {
                radioButton.isChecked = true
            }
        }
        val dialog = builder.create()
        dialog.show()
        btnCancel.setOnClickListener { dialog.dismiss() }
        btnOk.setOnClickListener {
            //Спочатку видалення елемента
            cart.deleteCart(CartItemDB(cartItem)).subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(object : SingleObserver<Int> {
                    override fun onSuccess(t: Int) {
                        //Після успішного видалення ми оновлюємо інформацію і знову додаємо в кошик
                        if (Common.foodSelected!!.userSelectedAddon != null) cartItem.foodAddon =
                            Gson().toJson(Common.foodSelected!!.userSelectedAddon)
                        else cartItem.foodAddon = ""
                        cartItem.foodSize = Common.foodSelected?.userSelectedSize?.name.toString()
                        cartItem.foodPrice = totalPrice
                        //Вставка
                        compositeDisposable.add(cart.insertOrReplaceAll(CartItemDB(cartItem))
                            .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                            .subscribe({
                                dialog.dismiss()
                                notifyItemChanged(position)
                                EventBus.getDefault().postSticky(UpdateItemsInCart(CartItemDB(cartItem)))
                            }, { t: Throwable? ->
                                Toast.makeText(context, t!!.message, Toast.LENGTH_SHORT).show()
                            })
                        )
                    }

                    override fun onSubscribe(d: Disposable) {

                    }

                    override fun onError(e: Throwable) {
                        Toast.makeText(context, e.message, Toast.LENGTH_SHORT).show()
                    }

                })

        }
    }

    var totalPrice = 0.0
    private fun calculateTotalPrice() {
        totalPrice = Common.foodSelected!!.userSelectedSize?.price?.toDouble()!!
        if (Common.foodSelected!!.userSelectedAddon != null && Common.foodSelected!!.userSelectedAddon!!.size > 0) {
            for (addonModel in Common.foodSelected!!.userSelectedAddon!!) {
                totalPrice = totalPrice.plus(addonModel.price.toDouble() * addonModel.userCount)
            }
        }
        var displayPrice = totalPrice
        displayPrice = Math.round(displayPrice * 100.0) / 100.0
        foodPrice.text = StringBuilder("").append(Common.formatPrice(displayPrice)).toString()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onAddonCategorySelected(event: AddonCategoryClick) {
        if (event.isSuccess) {
            addonRecycler.adapter = AddonAdapter(Common.addonCategorySelected!!.items, context)
        }
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onAddonSelected(event: AddonClick) {
        userAddonRecycler.adapter =
            UserAddonAdapter(Common.foodSelected!!.userSelectedAddon!!, context)
        calculateTotalPrice()
    }

    @Subscribe(sticky = true, threadMode = ThreadMode.MAIN)
    fun onUserAddonCountUpdate(event: UserAddonCountUpdate) {
        if (event.isSuccess) {
            addonRecycler.adapter?.notifyItemChanged(
                Common.addonCategorySelected!!.items.indexOf(Common.addonCategorySelected!!.items.find { it.name == event.addon.name })
            )
        }
        calculateTotalPrice()
    }

}
