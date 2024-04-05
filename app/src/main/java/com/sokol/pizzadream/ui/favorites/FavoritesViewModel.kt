package com.sokol.pizzadream.ui.favorites

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Database.PizzaDatabase
import com.sokol.pizzadream.Database.Repositories.FavoriteInterface
import com.sokol.pizzadream.Database.Repositories.FavoriteRepository
import com.sokol.pizzadream.Model.FavoriteItem
import com.sokol.pizzadream.Model.FoodModel
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.schedulers.Schedulers

class FavoritesViewModel : ViewModel() {

    private val compositeDisposable: CompositeDisposable = CompositeDisposable()
    private var favoriteInterface: FavoriteInterface? = null
    private var mutableLiveDataFavoriteItems: MutableLiveData<List<FavoriteItem>>? = null
    fun getFavoriteItems() {
        compositeDisposable.addAll(
            favoriteInterface!!.getAllFavorites(Common.currentUser!!.uid!!)
                .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe({ favoriteItems ->
                    val tempList = ArrayList<FavoriteItem>()
                    var counter = 0
                    for (item in favoriteItems) {
                        val foodRef =
                            FirebaseDatabase.getInstance().getReference(Common.CATEGORY_REF)
                                .child(item.categoryId).child("foods").child(item.foodId)
                        foodRef.addListenerForSingleValueEvent(object : ValueEventListener {
                            override fun onDataChange(snapshot: DataSnapshot) {
                                val model = snapshot.getValue(FoodModel::class.java)
                                if (model != null) {
                                    val favorite = FavoriteItem()
                                    favorite.foodId = model.id.toString()
                                    favorite.foodName = model.name
                                    favorite.foodImage = model.image
                                    favorite.foodPrice = model.size[0].price.toDouble()
                                    favorite.categoryId = model.categoryId.toString()
                                    tempList.add(favorite)
                                    counter++
                                    if (counter == favoriteItems.size) {
                                        mutableLiveDataFavoriteItems!!.value = tempList
                                    }
                                } else {

                                    favoriteInterface!!.removeFromFavorites(item.foodId, item.uid)
                                        .subscribeOn(Schedulers.io())
                                        .observeOn(AndroidSchedulers.mainThread()).subscribe()
                                }
                            }

                            override fun onCancelled(error: DatabaseError) {
                            }

                        })
                    }
                }, { t: Throwable? -> mutableLiveDataFavoriteItems!!.value = null })
        )
    }

    fun getMutableLiveDataFavoriteItems(): MutableLiveData<List<FavoriteItem>> {
        if (mutableLiveDataFavoriteItems == null) {
            mutableLiveDataFavoriteItems = MutableLiveData()
        }
        getFavoriteItems()
        return mutableLiveDataFavoriteItems!!
    }

    fun initFavoriteInterface(context: Context) {
        favoriteInterface = FavoriteRepository(PizzaDatabase.getInstance(context).getFavoriteDAO())
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}