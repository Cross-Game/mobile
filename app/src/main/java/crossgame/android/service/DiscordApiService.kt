package crossgame.android.service

import crossgame.android.domain.models.token.TokenResponse
import retrofit2.Call
import retrofit2.http.*

interface DiscordApiService {
    @FormUrlEncoded
    @POST("oauth2/token")
    fun trocarCodigoPorToken(
        @Field("client_id") clientId: String,
        @Field("client_secret") clientSecret: String,
        @Field("code") code: String,
        @Field("redirect_uri") redirectUri: String,
        @Field("grant_type") grantType: String
    ): Call<TokenResponse>
}
