package com.sokol.pizzadream.ui.news

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ServerValue
import com.google.firebase.database.ValueEventListener
import com.sokol.pizzadream.Callback.INewsLoadCallback
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Model.NewsModel

class NewsViewModel : ViewModel(), INewsLoadCallback {
    private var newsListMutableLiveData: MutableLiveData<List<NewsModel>>? = null
    private lateinit var messageError: MutableLiveData<String>
    private var  newsLoadCallbackListener: INewsLoadCallback = this
    val newsList: LiveData<List<NewsModel>>
        get() {
            if(newsListMutableLiveData == null){
                newsListMutableLiveData = MutableLiveData()
                messageError = MutableLiveData()
                loadNewsList()
            }
            return newsListMutableLiveData!!
        }

    private fun loadNewsList() {
        val tempList = ArrayList<NewsModel>()
        val newsRef = FirebaseDatabase.getInstance().getReference(Common.NEWS_REF)
        newsRef.addListenerForSingleValueEvent(object :ValueEventListener{
            override fun onDataChange(snapshot: DataSnapshot) {
                for (itemsSnapshot in snapshot.children){
                    val model = itemsSnapshot.getValue(NewsModel::class.java)
                    tempList.add(model!!)
                }
                // Сортування новин за датою в порядку спадання
                val sortedList = tempList.sortedByDescending { it.date }
                newsListMutableLiveData!!.value = sortedList
                newsLoadCallbackListener.onNewsLoadSuccess(sortedList)
            }

            override fun onCancelled(error: DatabaseError) {
                newsLoadCallbackListener.onNewsLoadFailed(error.message)
            }

        })
    }

    override fun onNewsLoadSuccess(newsList: List<NewsModel>) {
        newsListMutableLiveData?.value = newsList
    }

    override fun onNewsLoadFailed(message: String) {
        messageError.value = message
    }

}