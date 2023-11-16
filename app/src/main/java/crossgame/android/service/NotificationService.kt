package crossgame.android.service

import crossgame.android.domain.models.notifications.Notification
import retrofit2.http.GET
import retrofit2.Call
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface NotificationService {

    @GET("/notifies/{userId}")
    fun retrieveNotifications(@Path("userId") userId : Long): Call<List<Notification>>

    @POST("/notifies/{userId}")
    fun createNotification(@Path("userId") userId : Long): Call<Notification>

    @PATCH("/notifies/{userId}")
    fun negateNotification(@Path("userId") userId : Long): Call<Notification>


}