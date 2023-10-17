package crossgame.android.service

import crossgame.android.domain.models.platforms.GameplayPlatformType
import crossgame.android.domain.models.users.UserPreference
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.PATCH
import retrofit2.http.Path

interface PlatformsService {


    @PATCH("/{userId}")
    fun updateGamePlatformsForUserById(@Path("userId") userId: Long,@Body platforms: List<GameplayPlatformType>)
}