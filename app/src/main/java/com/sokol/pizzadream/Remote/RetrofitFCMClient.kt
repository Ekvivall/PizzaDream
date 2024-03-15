package com.sokol.pizzadream.Remote

import retrofit2.Retrofit
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitFCMClient {
    private var instances: Retrofit?=null
    fun getInstance(): Retrofit {
        if (instances == null)
            instances = Retrofit.Builder()
                .baseUrl("https://fcm.googleapis.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        return instances!!
    }
}