package crossgame.android.service

import crossgame.android.domain.models.rooms.Room
import retrofit2.Call
import retrofit2.http.GET

interface RoomService {

    @GET("/team-rooms")
    fun retrieveAll(): Call<List<Room>>
}