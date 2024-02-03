package com.sokol.pizzadream.Remote

import com.sokol.pizzadream.Model.BraintreeToken
import com.sokol.pizzadream.Model.BraintreeTransaction
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.HeaderMap
import retrofit2.http.POST

interface ICloudFunctions {
    @GET("token")
    fun getToken(@HeaderMap headers: Map<String, String>): Observable<BraintreeToken>

    @POST("checkout")
    @FormUrlEncoded
    fun submitPayment(
        @HeaderMap headers: Map<String, String>,
        @Field("amount") amount: Double,
        @Field("payment_method_nonce") nonce: String
    ): Observable<BraintreeTransaction>
}