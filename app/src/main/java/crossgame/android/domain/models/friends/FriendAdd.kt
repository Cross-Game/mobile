package crossgame.android.domain.models.friends

import com.google.gson.annotations.SerializedName
import crossgame.android.domain.models.enums.FriendshipState

data class FriendAdd(
    @SerializedName("username") val username: String,
    @SerializedName("friendUserId") val friendUserId: Long,
    @SerializedName("friendshipState") val friendshipState: FriendshipState
)
