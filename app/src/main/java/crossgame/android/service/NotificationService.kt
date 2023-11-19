package crossgame.android.service

import crossgame.android.domain.models.notifications.NotificationRequest
import crossgame.android.domain.models.notifications.NotificationResponse
import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationService {

    @GET("/notifies/{userId}")
    fun retrieveNotifications(@Path("userId") userId : Long): Call<List<NotificationResponse>>

    @POST("/notifies/{userId}")
    fun createNotification(@Path("userId") userId : Long, @Body notification: NotificationRequest): Call<NotificationResponse>

    @PATCH("/notifies/{userId}")
    fun removeNotification(@Path("userId") userId : Long): Call<NotificationResponse>


}