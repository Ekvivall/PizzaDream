package com.sokol.pizzadream.ui.newsdetail

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.sokol.pizzadream.Common.Common
import com.sokol.pizzadream.Model.NewsModel

class NewsDetailViewModel : ViewModel() {

    private var newsDetailMutableLiveData: MutableLiveData<NewsModel>? = null
    fun getNewsDetailMutableLiveData(): MutableLiveData<NewsModel> {
        if (newsDetailMutableLiveData == null) {
            newsDetailMutableLiveData = MutableLiveData()
        }
        newsDetailMutableLiveData!!.value = Common.newsSelected
        return newsDetailMutableLiveData!!
    }

}