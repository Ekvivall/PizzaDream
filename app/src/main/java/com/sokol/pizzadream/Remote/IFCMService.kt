package com.sokol.pizzadream.Remote

import com.sokol.pizzadream.Model.FCMResponse
import com.sokol.pizzadream.Model.FCMSendData
import io.reactivex.Observable
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST

interface IFCMService {
    @Headers(
        "Content-Type:application/json", "Authorization:key=AAAAy5t_qR4:APA91bFaOhNCOtpPnoH0j0IHsgIEc4UeoMAbGsHVfDfpvYtC5ySWyHkmHNAgcumXfMequbo7P_TQWQn67vXrw_aO9UAmfUALURwMGx783neNy-2ojQc61vqXP6LBlP-FD8rSp5Kg8vVj"
    )
    @POST("fcm/send")
    fun sendNotification(@Body body: FCMSendData): Observable<FCMResponse>
}