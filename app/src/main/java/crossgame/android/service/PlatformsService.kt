package crossgame.android.service

import crossgame.android.domain.models.platforms.GameplayPlatformType
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface PlatformsService {

    @PATCH("/user-platforms/{userId}")
    fun updateGamePlatformsForUserById(@Path("userId") userId: Long, @Body platforms: List<String>):Call<List<String>>

    @GET("/user-platforms/{userId}")
    fun retrieveGamePlatformsForUserById(@Path("userId") userId: Long):Call<List<GameplayPlatformType>>

}