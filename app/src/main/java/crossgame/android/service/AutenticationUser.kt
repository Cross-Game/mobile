package crossgame.android.service

import crossgame.android.domain.models.user.UserRequest
import crossgame.android.domain.models.user.UserResponse
import retrofit2.Call
import retrofit2.http.POST
import retrofit2.http.Body

interface AutenticationUser {

    @POST("/user-auth")
    fun singIn(@Body userRequest: UserRequest) :
            Call<UserResponse>
}