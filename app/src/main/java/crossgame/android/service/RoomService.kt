package crossgame.android.service

import crossgame.android.domain.models.rooms.Room
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface RoomService {

    @GET("/team-rooms")
    fun retrieveAll(): Call<List<Room>>

    @GET("/team-rooms/{idRoom}")
    fun retrieveRoomById(@Path("idRoom") idRoom: Long): Call<Room>

    @PUT("team-rooms/add-users/user/{userId}/{roomId}")
    fun addCommonUser(@Path("userId") userId: Long, @Path("roomId") roomId: Long): Call<Unit>

    @POST("team-rooms/{userId}")
    fun createRoom(@Path("UserId") userId: Long, @Body room: Room)
}