package crossgame.android.service

import crossgame.android.domain.models.notifications.Notification
import retrofit2.http.GET
import retrofit2.Call

interface NotificationService {

    @GET("/notification/{userId}")
    fun listar(userId : Long): Call<List<Notification>>
}