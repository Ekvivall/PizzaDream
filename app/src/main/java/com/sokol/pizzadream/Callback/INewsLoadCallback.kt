package com.sokol.pizzadream.Callback

import com.sokol.pizzadream.Model.NewsModel

interface INewsLoadCallback {
    fun onNewsLoadSuccess(newsList: List<NewsModel>)
    fun onNewsLoadFailed(message:String)
}