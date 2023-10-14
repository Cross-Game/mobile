package crossgame.android.domain.models.token

import com.google.gson.annotations.SerializedName

data class TokenResponse(
    @SerializedName("access_token")
    val access_token: String,
    @SerializedName("token_type")
    val token_type: String,
    @SerializedName("expires_in")
    val expires_in: Long,
    @SerializedName("refresh_token")
    val refresh_token: String,
    @SerializedName("scope")
    val scope: String
)

