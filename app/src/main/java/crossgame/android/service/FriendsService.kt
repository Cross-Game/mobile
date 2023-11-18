package crossgame.android.service

import crossgame.android.domain.models.friends.FriendAdd
import crossgame.android.domain.models.friends.Friends
import crossgame.android.domain.models.users.UserFriend
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.POST
import retrofit2.http.Path

interface FriendsService {

    @GET("friends/{userId}")
    fun retrieveFriendsForUserById(@Path("userId") userId: Long): Call<List<UserFriend>>

    @POST("friends/{userId}")
    fun addFriendToAnUser(@Path("userId") userId: Long, @Body friendAdd : FriendAdd): Call<Unit>
    @PATCH("friends/confirming-friend-request/{userId}/{friendUsername}")
    fun confirmFriendRequest(@Path("userId") userId: Long,@Path("friendUsername") friendUsername: String):Call<UserFriend>
    @DELETE("friends/declining-friend-request/{userId}/{friendUsername}")
    fun decliningFriendRequest(@Path("userId") userId: Long,@Path("friendUsername") friendUsername: String):Call<Void>

}