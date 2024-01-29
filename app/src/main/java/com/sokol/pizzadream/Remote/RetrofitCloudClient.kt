package com.sokol.pizzadream.Remote

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory

object RetrofitCloudClient {
    private var instances: Retrofit?=null
    fun getInstance():Retrofit {
        if (instances == null)
            instances = Retrofit.Builder()
                .baseUrl("https://us-central1-pizza-dream-bfccd.cloudfunctions.net/widget")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build()
        return instances!!
    }
}