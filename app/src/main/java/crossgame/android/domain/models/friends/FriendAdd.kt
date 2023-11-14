package crossgame.android.domain.models.friends

import crossgame.android.domain.models.enums.FriendshipState

data class FriendAdd(
    val username: String,
    val friendUserId: Long,
    val friendshipState: FriendshipState
)
