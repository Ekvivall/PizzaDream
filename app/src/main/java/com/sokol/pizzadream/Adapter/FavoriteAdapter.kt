package com.sokol.pizzadream.Adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sokol.pizzadream.Callback.IRecyclerItemClickListener
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Database.Entities.FavoriteItem
import com.sokol.pizzadream.Database.PizzaDatabase
import com.sokol.pizzadream.Database.Repositories.FavoriteInterface
import com.sokol.pizzadream.Database.Repositories.FavoriteRepository
import com.sokol.pizzadream.EventBus.FoodItemClick
import com.sokol.pizzadream.Model.FoodModel
import com.sokol.pizzadream.R
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers
import org.greenrobot.eventbus.EventBus

class FavoriteAdapter(var items: List<FavoriteItem>, val context: Context) :
    RecyclerView.Adapter<FavoriteAdapter.MyViewHolder>() {
    private val favorite: FavoriteInterface =
        FavoriteRepository(PizzaDatabase.getInstance(context).getFavoriteDAO())
    private val compositeDisposable: CompositeDisposable = CompositeDisposable()

    class MyViewHolder(view: View) : RecyclerView.ViewHolder(view), View.OnClickListener {
        var foodName: TextView = view.findViewById(R.id.food_name)
        var foodImage: ImageView = view.findViewById(R.id.food_img)
        var foodPrice: TextView = view.findViewById(R.id.food_price)
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
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.layout_favorite_item, parent, false)
        return MyViewHolder(view)
    }

    override fun getItemCount(): Int {
        return items.size
    }

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        Glide.with(context).load(items[position].foodImage).into(holder.foodImage)
        holder.foodName.text = items[position].foodName
        val totalPrice = items[position].foodPrice
        val displayPrice = Math.round(totalPrice * 100.0) / 100.0
        holder.foodPrice.text =
            StringBuilder("").append(Common.formatPrice(displayPrice)).toString()
        holder.setListener(object : IRecyclerItemClickListener {
            override fun onItemClick(view: View, pos: Int) {
                findFoodItem(pos)
            }
        })
        holder.favImage.setOnClickListener {
            compositeDisposable.add(
                favorite.removeFromFavorites(
                    items[position].foodId, Common.currentUser?.uid.toString()
                ).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread()).subscribe({
                    Toast.makeText(
                        context,
                        items[position].foodName + " видалено з обраних",
                        Toast.LENGTH_SHORT
                    ).show()
                    items = items.filterIndexed { index, _ -> index != position }
                    notifyDataSetChanged()
                }, { err: Throwable? ->
                    Toast.makeText(
                        context,
                        "Помилка видалення товару з обраного" + err!!.message,
                        Toast.LENGTH_SHORT
                    ).show()
                })
            )
        }

    }

    fun findFoodItem(position: Int) {
        val categoryRef = FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF)
        categoryRef.child(items[position].categoryId).child("foods").child(items[position].foodId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val foodModel = snapshot.getValue(FoodModel::class.java)
                    Common.foodSelected = foodModel
                    EventBus.getDefault().postSticky(FoodItemClick(true, foodModel!!))

                }

                override fun onCancelled(error: DatabaseError) {
                }
            })
    }

    fun onStop() {
        compositeDisposable.clear()
    }
}