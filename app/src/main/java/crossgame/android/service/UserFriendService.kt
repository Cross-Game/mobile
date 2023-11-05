package crossgame.android.service

import crossgame.android.domain.models.feedbacks.Feedback
import crossgame.android.domain.models.user.UserList
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Path

interface UserFriendService {
    @GET("/friends/{userId}")
    fun listarFriend(@Path("userId") userId: Long) : Call<List<UserList>>
}