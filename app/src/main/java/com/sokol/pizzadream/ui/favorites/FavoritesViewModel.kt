package com.sokol.pizzadream.ui.favorites

import android.content.Context
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Database.Entities.FavoriteItem
import com.sokol.pizzadream.Database.PizzaDatabase
import com.sokol.pizzadream.Database.Repositories.FavoriteInterface
import com.sokol.pizzadream.Database.Repositories.FavoriteRepository
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
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe({ favoriteItems ->
                    mutableLiveDataFavoriteItems!!.value = favoriteItems
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