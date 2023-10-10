package crossgame.android.service

import crossgame.android.domain.models.notifications.Notification
import retrofit2.Call
import retrofit2.http.POST

interface AutenticationUser {

    @POST("/login")
    fun registerUser(userId : Long): Call<List<Notification>>
}