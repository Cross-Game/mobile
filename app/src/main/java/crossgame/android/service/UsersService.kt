package crossgame.android.service

import crossgame.android.domain.models.users.BaseUser
import retrofit2.Call
import retrofit2.http.GET

interface UsersService {

    @GET("/users")
    fun GetAllUsers(): Call<List<BaseUser>>
}