package crossgame.android.service

import crossgame.android.domain.models.token.TokenResponse
import crossgame.android.domain.models.user.DiscordUserResponse
import okhttp3.FormBody
import retrofit2.Call
import retrofit2.http.*

interface DiscordApiService {
    @GET("users/@me")
    fun getDiscordUser(@Header("Authorization") authorization: String): Call<DiscordUserResponse>

    @FormUrlEncoded
    @POST("oauth2/token")
    fun exchangeCodeForToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("grant_type") grantType: String
    ): Call<TokenResponse>


}
