package crossgame.android.service

import crossgame.android.domain.models.users.UserFriend
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface FriendsService {

    @GET("friends/{userId}")
    fun retrieveFriendsForUserById(@Path("userId") userId: Long): Call<List<UserFriend>>
}