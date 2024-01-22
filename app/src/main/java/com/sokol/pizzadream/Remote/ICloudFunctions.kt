package com.sokol.pizzadream.Remote

import android.database.Observable
import com.sokol.pizzadream.Model.BraintreeToken
import retrofit2.http.GET

interface ICloudFunctions {
    @GET("token")
    fun getToken(): Observable<BraintreeToken>
}